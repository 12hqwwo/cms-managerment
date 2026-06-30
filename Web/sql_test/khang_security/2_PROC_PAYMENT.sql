-- =================================================================
-- PROC_INIT_PAYMENT.sql
-- Procedure 1: Khởi tạo bản ghi Payment trước khi chuyển lên VNPay
-- Gọi TRƯỚC khi redirect sang cổng VNPay
-- Schema: TRASUA (Oracle 12c+)
-- RBAC: GRANT EXECUTE ON PROC_INIT_PAYMENT TO TRASUA;
-- =================================================================

CREATE OR REPLACE PROCEDURE PROC_INIT_PAYMENT (
    p_order_id      IN  VARCHAR2,       -- mã giao dịch ngẫu nhiên (8 ký tự)
    p_amount        IN  VARCHAR2,       -- số tiền (string, đơn vị VND)
    p_error_code    OUT NUMBER,         -- 0 = OK, khác = lỗi
    p_error_msg     OUT NVARCHAR2
)
IS
    v_exists NUMBER;
BEGIN
    p_error_code := 0;
    p_error_msg  := NULL;

    -- Kiểm tra trùng ORDERID
    SELECT COUNT(*) INTO v_exists FROM payment WHERE ORDERID = p_order_id;
    IF v_exists > 0 THEN
        p_error_code := -1;
        p_error_msg  := 'ORDERID đã tồn tại: ' || p_order_id;
        RETURN;
    END IF;

    INSERT INTO payment (amount, ORDERID, ORDERSTATUS, PAYMENTDATE, STATUSEXCHANGE, bill_id)
    VALUES (p_amount, p_order_id, '0', SYSTIMESTAMP, 0, NULL);

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_error_code := -99;
        p_error_msg  := SQLERRM;
END PROC_INIT_PAYMENT;
/

-- =================================================================
-- PROC_CONFIRM_PAYMENT.sql
-- Procedure 2: Xác nhận thanh toán VNPay thành công
-- Gọi SAU khi VNPay callback về, tạo Bill và liên kết Payment
-- RBAC: Không yêu cầu role cụ thể (callback từ VNPay server)
--       Bảo vệ bằng chữ ký HMAC ở tầng Java trước khi gọi procedure
-- =================================================================

CREATE OR REPLACE PROCEDURE PROC_CONFIRM_PAYMENT (
    -- ===== INPUT: từ VNPay callback =====
    p_order_id_vnpay    IN  VARCHAR2,   -- vnp_TxnRef (mã giao dịch VNPay)
    -- ===== INPUT: từ session (đơn hàng tạm) =====
    p_billing_address   IN  NVARCHAR2,
    p_payment_method_id IN  NUMBER,
    p_customer_id       IN  NUMBER,
    p_voucher_id        IN  NUMBER,
    p_promotion_price   IN  NUMBER,
    p_branch_id         IN  NUMBER,
    p_order_details_json IN CLOB,       -- JSON array sản phẩm + topping
    -- ===== OUTPUT =====
    p_bill_id           OUT NUMBER,
    p_bill_code         OUT VARCHAR2,
    p_final_amount      OUT NUMBER,
    p_error_code        OUT NUMBER,
    p_error_msg         OUT NVARCHAR2
)
IS
    v_bill_id           NUMBER(19);
    v_bill_code         VARCHAR2(50);
    v_last_code         VARCHAR2(50);
    v_next_num          NUMBER := 1;
    v_num_part          VARCHAR2(50);
    v_total             NUMBER(19,2) := 0;
    v_final_total       NUMBER(19,2) := 0;
    v_promotion         NUMBER(19,2) := 0;

    v_pd_id             NUMBER(19);
    v_qty               NUMBER(10);
    v_pd_price          NUMBER(19,2);
    v_pd_qty_stock      NUMBER(10);
    v_pd_status         NUMBER(10);
    v_product_name      NVARCHAR2(255);
    v_discount_price    NUMBER(19,2);
    v_unit_price        NUMBER(19,2);
    v_topping_total     NUMBER(19,2);
    v_bill_detail_id    NUMBER(19);
    v_discount_usage    NUMBER(10);
    v_payment_id        NUMBER(19);

    CURSOR c_items IS
        SELECT jt.product_detail_id,
               jt.quantity,
               jt.toppings_json
        FROM JSON_TABLE(p_order_details_json, '$[*]'
            COLUMNS (
                product_detail_id NUMBER        PATH '$.productDetailId',
                quantity          NUMBER        PATH '$.quantity',
                toppings_json     CLOB FORMAT JSON PATH '$.toppings'
            )
        ) jt;

BEGIN
    p_error_code := 0;
    p_error_msg  := NULL;

    -- ====================================================
    -- 1. Kiểm tra payment tồn tại và chưa xử lý (ORDERSTATUS='0')
    -- ====================================================
    BEGIN
        SELECT id INTO v_payment_id
        FROM   payment
        WHERE  ORDERID    = p_order_id_vnpay
          AND  ORDERSTATUS = '0';
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_error_code := -1;
            p_error_msg  := 'Giao dịch không tồn tại hoặc đã được xử lý: ' || p_order_id_vnpay;
            RETURN;
    END;

    -- ====================================================
    -- 2. Sinh mã hóa đơn
    -- ====================================================
    BEGIN
        -- Sinh mã hóa đơn mới từ Sequence
        SELECT 'HD' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(SEQ_BILL_CODE.NEXTVAL, 4, '0') 
        INTO v_bill_code FROM DUAL;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN v_next_num := 1;
    END;

    -- ====================================================
    -- 3. Promotion price
    -- ====================================================
    IF p_promotion_price IS NULL OR p_promotion_price < 0 THEN
        v_promotion := 0;
    ELSE
        v_promotion := p_promotion_price;
    END IF;

    -- ====================================================
    -- 4. Kiểm tra voucher (nếu có)
    -- ====================================================
    IF p_voucher_id IS NOT NULL THEN
        BEGIN
            SELECT maximum_usage INTO v_discount_usage
            FROM   discount_code WHERE id = p_voucher_id;
            IF v_discount_usage <= 0 THEN
                p_error_code := -2;
                p_error_msg  := 'Mã giảm giá đã hết lượt sử dụng';
                RETURN;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                p_error_code := -3;
                p_error_msg  := 'Không tìm thấy voucher';
                RETURN;
        END;
    END IF;

    -- ====================================================
    -- 5. Tạo BILL (ONLINE, status CHO_XAC_NHAN)
    -- ====================================================
    INSERT INTO bill (
        amount, billing_address, code, create_date,
        invoice_type, promotion_price, return_status, status,
        update_date, customer_id, discount_code_id,
        payment_method_id, branch_id
    ) VALUES (
        0, p_billing_address, v_bill_code, SYSTIMESTAMP,
        'ONLINE', v_promotion, 0, 'CHO_XAC_NHAN',
        SYSTIMESTAMP, p_customer_id, p_voucher_id,
        p_payment_method_id, p_branch_id
    ) RETURNING id INTO v_bill_id;

    -- ====================================================
    -- 6. Xử lý từng sản phẩm
    -- ====================================================
    FOR rec IN c_items LOOP
        v_pd_id := rec.product_detail_id;
        v_qty   := rec.quantity;

        BEGIN
            SELECT pd.price, pd.quantity, p.status, p.name
            INTO   v_pd_price, v_pd_qty_stock, v_pd_status, v_product_name
            FROM   product_detail pd
            JOIN   product p ON p.id = pd.product_id
            WHERE  pd.id = v_pd_id;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                ROLLBACK;
                p_error_code := -4;
                p_error_msg  := 'Không tìm thấy sản phẩm ID=' || v_pd_id;
                RETURN;
        END;

        IF v_pd_status = 2 THEN
            ROLLBACK;
            p_error_code := -5;
            p_error_msg  := 'Sản phẩm "' || v_product_name || '" đã ngừng bán';
            RETURN;
        END IF;

        IF v_pd_qty_stock - v_qty < 0 THEN
            ROLLBACK;
            p_error_code := -6;
            p_error_msg  := 'Sản phẩm "' || v_product_name || '" chỉ còn lại ' || v_pd_qty_stock;
            RETURN;
        END IF;

        BEGIN
            SELECT DISCOUNTEDAMOUNT INTO v_discount_price
            FROM   product_discount
            WHERE  product_detail_id = v_pd_id
              AND  closed = 0
              AND  STARTDATE <= SYSTIMESTAMP
              AND  ENDDATE   >= SYSTIMESTAMP
              AND  ROWNUM = 1;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN v_discount_price := NULL;
        END;

        v_unit_price := NVL(v_discount_price, v_pd_price);

        v_topping_total := 0;
        BEGIN
            SELECT NVL(SUM(jt.topping_price), 0) INTO v_topping_total
            FROM   JSON_TABLE(rec.toppings_json, '$[*]'
                       COLUMNS(topping_price NUMBER PATH '$.price')) jt
            WHERE  jt.topping_price IS NOT NULL;
        EXCEPTION WHEN OTHERS THEN v_topping_total := 0;
        END;

        v_unit_price := v_unit_price + v_topping_total;
        v_total      := v_total + (v_unit_price * v_qty);

        INSERT INTO bill_detail (moment_price, quantity, return_quantity, bill_id, product_detail_id)
        VALUES (v_unit_price, v_qty, NULL, v_bill_id, v_pd_id)
        RETURNING id INTO v_bill_detail_id;

        IF rec.toppings_json IS NOT NULL THEN
            INSERT INTO bill_detail_topping (topping_name, topping_price, bill_detail_id)
            SELECT jt.topping_name, jt.topping_price, v_bill_detail_id
            FROM   JSON_TABLE(rec.toppings_json, '$[*]'
                       COLUMNS(
                           topping_name  NVARCHAR2(255) PATH '$.name',
                           topping_price NUMBER(19,2)   PATH '$.price'
                       )) jt
            WHERE  jt.topping_price IS NOT NULL;
        END IF;

        UPDATE product_detail SET quantity = quantity - v_qty WHERE id = v_pd_id;
    END LOOP;

    -- ====================================================
    -- 7. Voucher + tổng tiền
    -- ====================================================
    IF p_voucher_id IS NOT NULL THEN
        UPDATE discount_code SET maximum_usage = maximum_usage - 1 WHERE id = p_voucher_id;
    END IF;

    v_final_total := GREATEST(v_total - v_promotion, 0);
    UPDATE bill SET amount = v_final_total WHERE id = v_bill_id;

    -- ====================================================
    -- 8. Liên kết Payment → Bill, đánh dấu đã thanh toán
    -- ====================================================
    UPDATE payment
    SET    bill_id      = v_bill_id,
           ORDERSTATUS = '1',
           PAYMENTDATE = SYSTIMESTAMP,
           STATUSEXCHANGE = 0
    WHERE  id = v_payment_id;

    -- ====================================================
    -- 9. OUT + COMMIT
    -- ====================================================
    p_bill_id      := v_bill_id;
    p_bill_code    := v_bill_code;
    p_final_amount := v_final_total;
    p_error_code   := 0;
    p_error_msg    := NULL;

    COMMIT;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_error_code   := -99;
        p_error_msg    := SQLERRM;
        p_bill_id      := NULL;
        p_bill_code    := NULL;
        p_final_amount := NULL;
END PROC_CONFIRM_PAYMENT;
/

-- =================================================================
-- Kiểm tra sau khi tạo
-- =================================================================
SELECT object_name, status FROM user_objects
WHERE  object_name IN ('PROC_INIT_PAYMENT', 'PROC_CONFIRM_PAYMENT');

-- =================================================================
-- RBAC: Grant execute (DBA chạy 1 lần)
-- GRANT EXECUTE ON PROC_INIT_PAYMENT    TO TRASUA;
-- GRANT EXECUTE ON PROC_CONFIRM_PAYMENT TO TRASUA;
-- REVOKE EXECUTE ON PROC_INIT_PAYMENT    FROM PUBLIC;
-- REVOKE EXECUTE ON PROC_CONFIRM_PAYMENT FROM PUBLIC;
-- =================================================================



