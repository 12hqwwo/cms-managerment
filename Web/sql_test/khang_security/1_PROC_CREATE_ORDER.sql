-- ==========================================
-- 0. T?O SEQUENCE CHO M? H�A ��N
-- ==========================================
BEGIN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE SEQ_BILL_CODE START WITH 1 INCREMENT BY 1 NOCACHE';
EXCEPTION WHEN OTHERS THEN
    IF SQLCODE != -955 THEN RAISE; END IF;
END;
/
-- =================================================================
-- PROC_CREATE_ORDER.sql
-- Stored Procedure: Tạo đơn hàng (Bắt buộc sử dụng)
-- Schema: TRASUA (Oracle 12c+)
-- Mọi đặt hàng phải đi qua procedure này.
-- Không cho phép thực thi business logic Java trực tiếp.
-- =================================================================

-- RBAC: Grant quyền EXECUTE (chạy bởi DBA 1 lần)
-- GRANT EXECUTE ON PROC_CREATE_ORDER TO TRASUA;
-- REVOKE EXECUTE ON PROC_CREATE_ORDER FROM PUBLIC;

CREATE OR REPLACE PROCEDURE PROC_CREATE_ORDER (
    -- ===== INPUT: thông tin đơn hàng =====
    p_billing_address   IN NVARCHAR2,    -- địa chỉ giao hàng
    p_invoice_type      IN VARCHAR2,     -- 'ONLINE' | 'OFFLINE'
    p_payment_method_id IN NUMBER,       -- ID phương thức thanh toán
    p_customer_id       IN NUMBER,       -- NULL nếu khách vãng lai
    p_voucher_id        IN NUMBER,       -- NULL nếu không dùng voucher
    p_promotion_price   IN NUMBER,       -- số tiền giảm từ voucher
    p_order_id_vnpay    IN VARCHAR2,     -- mã VNPay (chuyển khoản), NULL nếu tiền mặt
    p_branch_id         IN NUMBER,       -- NULL nếu online không chọn chi nhánh
    -- ===== INPUT: chi tiết sản phẩm (JSON Array) =====
    -- Format: '[{"productDetailId":1,"quantity":2,"toppings":[{"name":"Trân châu","price":5000}]}]'
    p_order_details_json IN CLOB,
    -- ===== OUTPUT =====
    p_bill_id           OUT NUMBER,      -- ID hóa đơn vừa tạo
    p_bill_code         OUT VARCHAR2,    -- mã hóa đơn (HDxxx)
    p_final_amount      OUT NUMBER,      -- tổng tiền cuối cùng
    p_error_code        OUT NUMBER,      -- 0 = thành công, âm = lỗi nghiệp vụ
    p_error_msg         OUT NVARCHAR2    -- mô tả lỗi nếu có
)
IS
    -- ===== Biến nội bộ =====
    v_bill_id           NUMBER(19);
    v_bill_code         VARCHAR2(50);
    v_last_code         VARCHAR2(50);
    v_next_num          NUMBER := 1;
    v_num_part          VARCHAR2(50);
    v_total             NUMBER(19,2) := 0;
    v_final_total       NUMBER(19,2) := 0;
    v_promotion         NUMBER(19,2) := 0;

    -- ===== Biến xử lý sản phẩm =====
    v_pd_id             NUMBER(19);
    v_qty               NUMBER(10);
    v_pd_price          NUMBER(19,2);
    v_pd_qty_stock      NUMBER(10);
    v_pd_status         NUMBER(10);
    v_product_id        NUMBER(19);
    v_product_name      NVARCHAR2(255);
    v_discount_price    NUMBER(19,2);
    v_unit_price        NUMBER(19,2);
    v_topping_total     NUMBER(19,2);
    v_bill_detail_id    NUMBER(19);

    -- ===== Biến phụ =====
    v_discount_usage    NUMBER(10);
    v_pay_method_name   VARCHAR2(255);

    -- ===== Cursor: parse từng item trong JSON =====
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

    -- ==============================================================
    -- BƯỚC 1: Sinh mã hóa đơn tự động bằng SEQUENCE (HD + YYYYMMDD + SEQ)
    -- Giải quyết triệt để lỗi Race Condition khi Concurrency cao
    -- ==============================================================
    SELECT 'HD' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(SEQ_BILL_CODE.NEXTVAL, 4, '0') 
    INTO v_bill_code FROM DUAL;

    -- ==============================================================
    -- BƯỚC 2: Chuẩn hóa promotion price
    -- ==============================================================
    IF p_promotion_price IS NULL OR p_promotion_price < 0 THEN
        v_promotion := 0;
    ELSE
        v_promotion := p_promotion_price;
    END IF;

    -- ==============================================================
    -- BƯỚC 3: Kiểm tra voucher (nếu có)
    -- ==============================================================
    IF p_voucher_id IS NOT NULL THEN
        BEGIN
            SELECT maximum_usage INTO v_discount_usage
            FROM discount_code
            WHERE id = p_voucher_id;

            IF v_discount_usage <= 0 THEN
                p_error_code := -2;
                p_error_msg  := 'Mã giảm giá đã hết lượt sử dụng';
                RETURN;
            END IF;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                p_error_code := -3;
                p_error_msg  := 'Không tìm thấy voucher ID=' || p_voucher_id;
                RETURN;
        END;
    END IF;

    -- ==============================================================
    -- BƯỚC 4: Tạo bản ghi BILL
    -- Status tự động: OFFLINE → HOAN_THANH, ONLINE → CHO_XAC_NHAN
    -- ==============================================================
    INSERT INTO bill (
        amount, billing_address, code, create_date,
        invoice_type, promotion_price, return_status, status,
        update_date, customer_id, discount_code_id,
        payment_method_id, branch_id
    ) VALUES (
        0,
        p_billing_address,
        v_bill_code,
        SYSTIMESTAMP,
        p_invoice_type,
        v_promotion,
        0,
        CASE WHEN p_invoice_type = 'OFFLINE' THEN 'HOAN_THANH' ELSE 'CHO_XAC_NHAN' END,
        SYSTIMESTAMP,
        p_customer_id,
        p_voucher_id,
        p_payment_method_id,
        p_branch_id
    ) RETURNING id INTO v_bill_id;

    -- ==============================================================
    -- BƯỚC 5: Xử lý từng sản phẩm trong đơn hàng
    -- ==============================================================
    FOR rec IN c_items LOOP
        v_pd_id := rec.product_detail_id;
        v_qty   := rec.quantity;

        -- Lấy thông tin product_detail + product
        BEGIN
            SELECT pd.price, pd.quantity, p.status, p.id, p.name
            INTO   v_pd_price, v_pd_qty_stock, v_pd_status, v_product_id, v_product_name
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

        IF p_invoice_type = 'OFFLINE' THEN
            BEGIN
                SELECT quantity INTO v_pd_qty_stock
                FROM   branch_inventory
                WHERE  branch_id = p_branch_id AND product_detail_id = v_pd_id;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    ROLLBACK;
                    p_error_code := -6;
                    p_error_msg  := 'Sản phẩm ' || v_product_name || ' không có trong kho chi nhánh này';
                    RETURN;
            END;
        END IF;

        -- Kiểm tra ngừng bán (status = 2)
        IF v_pd_status = 2 THEN
            ROLLBACK;
            p_error_code := -5;
            p_error_msg  := 'Sản phẩm "' || v_product_name || '" đã ngừng bán';
            RETURN;
        END IF;

        -- Kiểm tra tồn kho đủ không
        IF v_pd_qty_stock - v_qty < 0 THEN
            ROLLBACK;
            p_error_code := -6;
            p_error_msg  := 'Sản phẩm "' || v_product_name
                         || '" chỉ còn lại ' || v_pd_qty_stock || ' sản phẩm';
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
            WHEN NO_DATA_FOUND THEN
                v_discount_price := NULL;
        END;

        -- Giá đơn vị: ưu tiên giá giảm, fallback về giá gốc
        IF v_discount_price IS NOT NULL THEN
            v_unit_price := v_discount_price;
        ELSE
            v_unit_price := v_pd_price;
        END IF;

        -- Tính tổng topping của item này
        v_topping_total := 0;
        BEGIN
            SELECT NVL(SUM(jt.topping_price), 0)
            INTO   v_topping_total
            FROM   JSON_TABLE(rec.toppings_json, '$[*]'
                       COLUMNS (topping_price NUMBER PATH '$.price')
                   ) jt
            WHERE  jt.topping_price IS NOT NULL;
        EXCEPTION
            WHEN OTHERS THEN
                v_topping_total := 0;
        END;

        v_unit_price := v_unit_price + v_topping_total;
        v_total      := v_total + (v_unit_price * v_qty);

        -- Insert BILL_DETAIL
        INSERT INTO bill_detail (moment_price, quantity, return_quantity, bill_id, product_detail_id)
        VALUES (v_unit_price, v_qty, NULL, v_bill_id, v_pd_id)
        RETURNING id INTO v_bill_detail_id;

        -- Insert BILL_DETAIL_TOPPING (từng topping của item)
        IF rec.toppings_json IS NOT NULL THEN
            INSERT INTO bill_detail_topping (topping_name, topping_price, bill_detail_id)
            SELECT jt.topping_name, jt.topping_price, v_bill_detail_id
            FROM   JSON_TABLE(rec.toppings_json, '$[*]'
                       COLUMNS (
                           topping_name  NVARCHAR2(255) PATH '$.name',
                           topping_price NUMBER(19,2)   PATH '$.price'
                       )
                   ) jt
            WHERE  jt.topping_price IS NOT NULL;
        END IF;

        -- Trừ tồn kho
        IF p_invoice_type = 'OFFLINE' THEN
            UPDATE branch_inventory
            SET    quantity = quantity - v_qty
            WHERE  branch_id = p_branch_id AND product_detail_id = v_pd_id;
        ELSE
            UPDATE product_detail
            SET    quantity = quantity - v_qty
            WHERE  id = v_pd_id;
        END IF;

    END LOOP;

    -- ==============================================================
    -- BƯỚC 6: Giảm lượt dùng voucher
    -- ==============================================================
    IF p_voucher_id IS NOT NULL THEN
        UPDATE discount_code
        SET    maximum_usage = maximum_usage - 1
        WHERE  id = p_voucher_id;
    END IF;

    -- ==============================================================
    -- BƯỚC 7: Tính tổng tiền cuối (trừ khuyến mãi)
    -- ==============================================================
    v_final_total := v_total - v_promotion;
    IF v_final_total < 0 THEN
        v_final_total := 0;
    END IF;

    UPDATE bill
    SET    amount = v_final_total
    WHERE  id = v_bill_id;

    -- ==============================================================
    -- BƯỚC 8: Tạo bản ghi PAYMENT
    -- ==============================================================
    SELECT name INTO v_pay_method_name
    FROM   payment_method
    WHERE  id = p_payment_method_id;

    IF v_pay_method_name = 'TIEN_MAT' THEN
        -- Tiền mặt: tạo payment hoàn tất ngay
        INSERT INTO payment (amount, ORDERID, ORDERSTATUS, PAYMENTDATE, STATUSEXCHANGE, bill_id)
        VALUES (
            TO_CHAR(v_final_total),
            DBMS_RANDOM.STRING('X', 8),
            '1',
            SYSTIMESTAMP,
            0,
            v_bill_id
        );
    ELSIF p_order_id_vnpay IS NOT NULL THEN
        -- Chuyển khoản VNPay: gán bill_id vào payment đã tạo trước
        UPDATE payment
        SET    bill_id = v_bill_id,
               STATUSEXCHANGE = 0
        WHERE  ORDERID = p_order_id_vnpay;
    ELSE
        -- Offline / trường hợp khác: tạo payment
        INSERT INTO payment (amount, ORDERID, ORDERSTATUS, PAYMENTDATE, STATUSEXCHANGE, bill_id)
        VALUES (
            TO_CHAR(v_final_total),
            DBMS_RANDOM.STRING('X', 8),
            '1',
            SYSTIMESTAMP,
            0,
            v_bill_id
        );
    END IF;

    -- ==============================================================
    -- BƯỚC 9: Gán OUT parameters and commit
    -- ==============================================================
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
END PROC_CREATE_ORDER;
/

-- =================================================================
-- Kiểm tra procedure đã tạo thành công
-- =================================================================
SELECT object_name, status
FROM   user_objects
WHERE  object_name = 'PROC_CREATE_ORDER';





