-- ============================================================
-- FILE: 06_oracle_procedures.sql
-- MUC DICH: Trien khai cac co che bao mat Oracle theo yeu cau
--   tu file mo ta: Nhom04_motadexuat.docx
--
-- NOI DUNG:
--   PHAN 1: Stored Procedures (PROC_CREATE_ORDER, PROC_INIT_INVENTORY)
--   PHAN 2: Functions (FN_GET_BRANCH_REVENUE, FN_CHECK_STOCK)
--   PHAN 3: VPD - Virtual Private Database (loc du lieu theo branch/customer)
--   PHAN 4: FGA - Fine-Grained Auditing (giam sat thao tac rui ro)
--   PHAN 5: Data Redaction (che SiT/Email khach hang)
--
-- HUONG DAN: Dang nhap bang SYSDBA hoac user TRASUA co quyen DBA
--   CONNECT TRASUA/"TraSua@2024"@//localhost:1521/FREEPDB1
-- Sau do chay tung PHAN (F5 hoac Run Script)
-- ============================================================


-- ============================================================
-- PHAN 1: STORED PROCEDURES
-- ============================================================

-- Tao sequence cho hoa don (neu chua co)
BEGIN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE seq_bill_code START WITH 1 INCREMENT BY 1';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -955 THEN -- -955 is "name is already used by an existing object"
            RAISE;
        END IF;
END;
/

-- -------------------------------------------------------
-- 1A. PROC_INIT_BRANCH_INVENTORY
--     Muc dich: Phan bo toan bo san pham ve mot chi nhanh
--               voi so luong mac dinh. Dung khi them chi nhanh moi.
--     Cach chay: EXEC PROC_INIT_BRANCH_INVENTORY(p_branch_id => 1, p_default_qty => 100);
-- -------------------------------------------------------
CREATE OR REPLACE PROCEDURE PROC_INIT_BRANCH_INVENTORY (
    p_branch_id     IN NUMBER,
    p_default_qty   IN NUMBER DEFAULT 100
)
AS
    v_count NUMBER;
    v_inserted NUMBER := 0;
BEGIN
    -- Kiem tra chi nhanh ton tai
    SELECT COUNT(*) INTO v_count
    FROM branch WHERE id = p_branch_id;

    IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001,
            'Chi nhanh ID = ' || p_branch_id || ' khong ton tai!');
    END IF;

    -- Insert cac san pham chua co trong kho chi nhanh nay
    INSERT INTO branch_inventory (branch_id, product_detail_id, quantity, isActive, createDate, updateDate, minQuantity)
    SELECT
        p_branch_id,
        pd.id,
        p_default_qty,
        1,
        SYSTIMESTAMP,
        SYSTIMESTAMP,
        0
    FROM product_detail pd
    WHERE NOT EXISTS (
        SELECT 1 FROM branch_inventory bi
        WHERE bi.product_detail_id = pd.id
          AND bi.branch_id = p_branch_id
    );

    v_inserted := SQL%ROWCOUNT;

    COMMIT;

    DBMS_OUTPUT.PUT_LINE('============================================');
    DBMS_OUTPUT.PUT_LINE('PROC_INIT_BRANCH_INVENTORY - Hoan thanh!');
    DBMS_OUTPUT.PUT_LINE('  Chi nhanh ID : ' || p_branch_id);
    DBMS_OUTPUT.PUT_LINE('  San pham duoc them: ' || v_inserted || ' dong');
    DBMS_OUTPUT.PUT_LINE('  So luong mac dinh : ' || p_default_qty);
    DBMS_OUTPUT.PUT_LINE('============================================');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        DBMS_OUTPUT.PUT_LINE('LOI: ' || SQLERRM);
        RAISE;
END PROC_INIT_BRANCH_INVENTORY;
/


-- -------------------------------------------------------
-- 1B. PROC_CREATE_ORDER
--     Muc dich: Tao hoa don theo luong chuan cua ung dung.
--               Kiem tra ton kho truoc khi ghi bill.
--               Tru ton kho sau khi xac nhan.
--     Dau vao:
--       p_customer_id, p_branch_id, p_payment_method_id,
--       p_billing_address, p_invoice_type,
--       p_product_detail_ids  (danh sach san pham, cach nhau dau phay)
--       p_quantities          (so luong tuong ung)
--       p_cashier_account_id  (nhan vien thu ngan, NULL neu dat online)
--     Dau ra:
--       p_bill_code           (ma hoa don duoc tao)
--       p_result_msg          (thong bao ket qua)
--     Cach chay (demo):
--       DECLARE
--         v_code VARCHAR2(50);
--         v_msg  VARCHAR2(500);
--       BEGIN
--         PROC_CREATE_ORDER(
--           p_customer_id => 1,
--           p_branch_id   => 1,
--           p_payment_method_id => 1,
--           p_billing_address => N'123 Pho Hue, Ha Noi',
--           p_invoice_type    => 'POS',
--           p_product_detail_ids => '1,2',
--           p_quantities         => '2,1',
--           p_cashier_account_id => NULL,
--           p_bill_code  => v_code,
--           p_result_msg => v_msg
--         );
--         DBMS_OUTPUT.PUT_LINE('Ma hoa don: ' || v_code);
--         DBMS_OUTPUT.PUT_LINE('Ket qua   : ' || v_msg);
--       END;
-- -------------------------------------------------------
CREATE OR REPLACE PROCEDURE PROC_CREATE_ORDER (
    p_customer_id           IN  NUMBER,
    p_branch_id             IN  NUMBER,
    p_payment_method_id     IN  NUMBER,
    p_billing_address       IN  NVARCHAR2,
    p_invoice_type          IN  VARCHAR2,
    p_product_detail_ids    IN  VARCHAR2,   -- VD: '1,3,7'
    p_quantities            IN  VARCHAR2,   -- VD: '2,1,3'
    p_cashier_account_id    IN  NUMBER DEFAULT NULL,
    p_bill_code             OUT VARCHAR2,
    p_result_msg            OUT VARCHAR2
)
AS
    v_bill_id       NUMBER;
    v_total_amount  NUMBER := 0;
    v_moment_price  NUMBER;
    v_stock_qty     NUMBER;
    v_pd_id         NUMBER;
    v_qty           NUMBER;
    v_bill_code     VARCHAR2(50);

    -- Phan tach chuoi id va quantity
    TYPE t_numlist IS TABLE OF NUMBER;
    v_pd_ids  t_numlist := t_numlist();
    v_qtys    t_numlist := t_numlist();

    v_pos     INTEGER;
    v_str     VARCHAR2(4000);
    v_idx     INTEGER;
BEGIN
    -- === Phan tach product_detail_ids ===
    v_str := p_product_detail_ids || ',';
    v_idx := 1;
    LOOP
        v_pos := INSTR(v_str, ',');
        EXIT WHEN v_pos = 0;
        v_pd_ids.EXTEND;
        v_pd_ids(v_idx) := TO_NUMBER(TRIM(SUBSTR(v_str, 1, v_pos - 1)));
        v_str := SUBSTR(v_str, v_pos + 1);
        v_idx := v_idx + 1;
    END LOOP;

    -- === Phan tach quantities ===
    v_str := p_quantities || ',';
    v_idx := 1;
    LOOP
        v_pos := INSTR(v_str, ',');
        EXIT WHEN v_pos = 0;
        v_qtys.EXTEND;
        v_qtys(v_idx) := TO_NUMBER(TRIM(SUBSTR(v_str, 1, v_pos - 1)));
        v_str := SUBSTR(v_str, v_pos + 1);
        v_idx := v_idx + 1;
    END LOOP;

    IF v_pd_ids.COUNT != v_qtys.COUNT THEN
        RAISE_APPLICATION_ERROR(-20010,
            'So san pham va so luong khong khop!');
    END IF;

    -- === Kiem tra ton kho truoc khi tao bill ===
    FOR i IN 1..v_pd_ids.COUNT LOOP
        v_pd_id := v_pd_ids(i);
        v_qty   := v_qtys(i);

        SELECT NVL(bi.quantity, 0)
        INTO   v_stock_qty
        FROM   branch_inventory bi
        WHERE  bi.branch_id = p_branch_id
          AND  bi.product_detail_id = v_pd_id
          AND  bi.isActive = 1;

        IF v_stock_qty < v_qty THEN
            RAISE_APPLICATION_ERROR(-20011,
                'San pham ID=' || v_pd_id ||
                ' khong du hang. Ton kho: ' || v_stock_qty ||
                ', Yeu cau: ' || v_qty);
        END IF;
    END LOOP;

    -- === Sinh ma hoa don ===
    BEGIN
        SELECT 'HD' || TO_CHAR(SYSDATE, 'YYYYMMDD') || LPAD(seq_bill_code.NEXTVAL, 5, '0')
        INTO v_bill_code
        FROM DUAL;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            -- Neu chua co sequence thi dung timestamp
            v_bill_code := 'HD' || TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS');
    END;

    -- === Tao ban ghi BILL ===
    INSERT INTO bill (
        code, status, invoice_type, billing_address,
        create_date, update_date,
        customer_id, payment_method_id, branch_id,
        cashier_account_id, promotion_price
    ) VALUES (
        v_bill_code, 'PENDING', p_invoice_type, p_billing_address,
        SYSTIMESTAMP, SYSTIMESTAMP,
        p_customer_id, p_payment_method_id, p_branch_id,
        p_cashier_account_id, 0
    )
    RETURNING id INTO v_bill_id;

    -- === Tao BILL_DETAIL va tru ton kho ===
    FOR i IN 1..v_pd_ids.COUNT LOOP
        v_pd_id := v_pd_ids(i);
        v_qty   := v_qtys(i);

        -- Lay gia tai thoi diem dat hang
        SELECT NVL(pd.price, 0)
        INTO   v_moment_price
        FROM   product_detail pd
        WHERE  pd.id = v_pd_id;

        -- Tao bill_detail
        INSERT INTO bill_detail (bill_id, product_detail_id, quantity, moment_price)
        VALUES (v_bill_id, v_pd_id, v_qty, v_moment_price);

        -- Tru ton kho
        UPDATE branch_inventory
        SET    quantity   = quantity - v_qty,
               updateDate = SYSTIMESTAMP
        WHERE  branch_id = p_branch_id
          AND  product_detail_id = v_pd_id;

        v_total_amount := v_total_amount + (v_moment_price * v_qty);
    END LOOP;

    -- === Cap nhat tong tien hoa don ===
    UPDATE bill SET amount = v_total_amount WHERE id = v_bill_id;

    COMMIT;

    p_bill_code  := v_bill_code;
    p_result_msg := 'Tao hoa don thanh cong! Ma: ' || v_bill_code ||
                    ' | Tong tien: ' || TO_CHAR(v_total_amount, 'FM999,999,999') || ' VND';

    DBMS_OUTPUT.PUT_LINE('============================================');
    DBMS_OUTPUT.PUT_LINE('PROC_CREATE_ORDER - Thanh cong!');
    DBMS_OUTPUT.PUT_LINE('  Ma hoa don : ' || v_bill_code);
    DBMS_OUTPUT.PUT_LINE('  Tong tien  : ' || TO_CHAR(v_total_amount, 'FM999,999,999') || ' VND');
    DBMS_OUTPUT.PUT_LINE('  Chi nhanh  : ' || p_branch_id);
    DBMS_OUTPUT.PUT_LINE('============================================');

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_bill_code  := NULL;
        p_result_msg := 'LOI: ' || SQLERRM;
        DBMS_OUTPUT.PUT_LINE('LOI tao hoa don: ' || SQLERRM);
        RAISE;
END PROC_CREATE_ORDER;
/


-- ============================================================
-- PHAN 2: FUNCTIONS
-- ============================================================

-- -------------------------------------------------------
-- 2A. FN_GET_BRANCH_REVENUE
--     Tinh doanh thu cua mot chi nhanh trong khoang thoi gian
--     Cach dung:
--       SELECT FN_GET_BRANCH_REVENUE(1, DATE '2026-06-01', DATE '2026-06-30') AS doanh_thu FROM DUAL;
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_GET_BRANCH_REVENUE (
    p_branch_id  IN NUMBER,
    p_from_date  IN DATE DEFAULT TRUNC(SYSDATE, 'MM'),
    p_to_date    IN DATE DEFAULT SYSDATE
) RETURN NUMBER
AS
    v_revenue NUMBER := 0;
BEGIN
    SELECT NVL(SUM(b.amount), 0)
    INTO   v_revenue
    FROM   bill b
    WHERE  b.branch_id = p_branch_id
      AND  b.status NOT IN ('CANCELLED', 'RETURNED')
      AND  TRUNC(b.create_date) BETWEEN p_from_date AND p_to_date;

    RETURN v_revenue;
EXCEPTION
    WHEN OTHERS THEN
        RETURN 0;
END FN_GET_BRANCH_REVENUE;
/


-- -------------------------------------------------------
-- 2B. FN_CHECK_STOCK
--     Kiem tra so luong ton kho cua mot san pham tai chi nhanh
--     Cach dung:
--       SELECT FN_CHECK_STOCK(1, 5) AS so_luong_con FROM DUAL;
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_CHECK_STOCK (
    p_branch_id         IN NUMBER,
    p_product_detail_id IN NUMBER
) RETURN NUMBER
AS
    v_qty NUMBER := 0;
BEGIN
    SELECT NVL(quantity, 0)
    INTO   v_qty
    FROM   branch_inventory
    WHERE  branch_id = p_branch_id
      AND  product_detail_id = p_product_detail_id
      AND  isActive = 1;

    RETURN v_qty;
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN 0;
    WHEN OTHERS        THEN RETURN -1;
END FN_CHECK_STOCK;
/


-- ============================================================
-- PHAN 3: VPD - Virtual Private Database
-- Loc du lieu theo branch_id (Vendor chi thay chi nhanh minh)
-- va theo customer_id (User chi thay don hang cua minh)
-- ============================================================

-- -------------------------------------------------------
-- 3A. Policy Function: loc BRANCH_INVENTORY theo branch_id cua Vendor
--     Cach hoat dong: moi lan Vendor chay SELECT tren branch_inventory,
--     Oracle tu dong them menh de WHERE branch_id = <branch cua Vendor>
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_VPD_BRANCH_INVENTORY (
    p_schema IN VARCHAR2,
    p_object IN VARCHAR2
) RETURN VARCHAR2
AS
    v_branch_id NUMBER;
    v_username  VARCHAR2(100);
    v_role      VARCHAR2(50);
BEGIN
    v_username := SYS_CONTEXT('USERENV', 'SESSION_USER');

    -- Admin khong bi loc
    v_role := 'ROLE_USER'; -- default
    BEGIN
        SELECT r.name INTO v_role
        FROM account a
        JOIN role r ON a.role_id = r.id
        WHERE a.email = SYS_CONTEXT('APEX$SESSION', 'APP_USER')
          AND ROWNUM = 1;
        
        IF v_role = 'ROLE_ADMIN' THEN
            RETURN NULL; -- Admin: khong them dieu kien, xem het
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN NULL;
    END;

    -- Vendor: loc theo branch_id
    BEGIN
        SELECT a.branch_id INTO v_branch_id
        FROM account a
        JOIN role r ON a.role_id = r.id
        WHERE a.email = SYS_CONTEXT('APEX$SESSION', 'APP_USER')
          AND r.name IN ('ROLE_VENDOR', 'ROLE_STAFF')
          AND ROWNUM = 1;

        IF v_branch_id IS NOT NULL THEN
            RETURN 'branch_id = ' || v_branch_id;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN NULL;
    END;

    -- Mac dinh: khong thay gi neu khong xac dinh duoc branch
    RETURN '1=0';
END FN_VPD_BRANCH_INVENTORY;
/


-- -------------------------------------------------------
-- 3B. Ap dung VPD len bang BRANCH_INVENTORY
-- -------------------------------------------------------
BEGIN
    -- Xoa policy cu neu co
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_RLS.DROP_POLICY(
            object_schema => ''TRASUA'',
            object_name   => ''BRANCH_INVENTORY'',
            policy_name   => ''POL_BRANCH_INVENTORY''
        );
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;';
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Loi xoa VPD Policy: ' || SQLERRM);
END;
/

BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_RLS.ADD_POLICY(
            object_schema   => ''TRASUA'',
            object_name     => ''BRANCH_INVENTORY'',
            policy_name     => ''POL_BRANCH_INVENTORY'',
            function_schema => ''TRASUA'',
            policy_function => ''FN_VPD_BRANCH_INVENTORY'',
            statement_types => ''SELECT'',
            enable          => TRUE
        );
    END;';
    DBMS_OUTPUT.PUT_LINE('VPD da duoc ap dung len bang BRANCH_INVENTORY');
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Chua the tao VPD. Vui long cap quyen: GRANT EXECUTE ON DBMS_RLS TO TRASUA;');
END;
/


-- -------------------------------------------------------
-- 3C. Policy Function: loc CART/BILL theo customer_id cua User
-- -------------------------------------------------------
CREATE OR REPLACE FUNCTION FN_VPD_CUSTOMER_DATA (
    p_schema IN VARCHAR2,
    p_object IN VARCHAR2
) RETURN VARCHAR2
AS
    v_customer_id NUMBER;
BEGIN
    -- Admin va Vendor: xem het
    BEGIN
        SELECT 1 INTO v_customer_id
        FROM account a
        JOIN role r ON a.role_id = r.id
        WHERE a.email = SYS_CONTEXT('APEX$SESSION', 'APP_USER')
          AND r.name IN ('ROLE_ADMIN', 'ROLE_VENDOR', 'ROLE_STAFF')
          AND ROWNUM = 1;
        RETURN NULL;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN NULL;
    END;

    -- User: chi thay du lieu cua minh
    BEGIN
        SELECT a.customer_id INTO v_customer_id
        FROM account a
        WHERE a.email = SYS_CONTEXT('APEX$SESSION', 'APP_USER')
          AND ROWNUM = 1;

        IF v_customer_id IS NOT NULL THEN
            RETURN 'customer_id = ' || v_customer_id;
        END IF;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN NULL;
    END;

    RETURN '1=0';
END FN_VPD_CUSTOMER_DATA;
/


-- ============================================================
-- PHAN 4: FGA - Fine-Grained Auditing
-- Giam sat cac thao tac co rui ro gian lan
-- ============================================================

-- -------------------------------------------------------
-- 4A. Ghi log khi ai UPDATE so luong ton kho (branch_inventory)
--     Bang thu cong (khong qua ung dung)
-- -------------------------------------------------------
BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_FGA.DROP_POLICY(
            object_schema => ''TRASUA'',
            object_name   => ''BRANCH_INVENTORY'',
            policy_name   => ''FGA_INVENTORY_UPDATE''
        );
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_FGA.ADD_POLICY(
            object_schema   => ''TRASUA'',
            object_name     => ''BRANCH_INVENTORY'',
            policy_name     => ''FGA_INVENTORY_UPDATE'',
            audit_condition => NULL,  
            audit_column    => ''QUANTITY'',
            statement_types => ''UPDATE'',
            audit_trail     => DBMS_FGA.DB + DBMS_FGA.EXTENDED
        );
    END;';
    DBMS_OUTPUT.PUT_LINE('FGA da duoc cai dat: giam sat UPDATE QUANTITY tren BRANCH_INVENTORY');
EXCEPTION
    WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Chua the tao FGA. Vui long cap quyen: GRANT EXECUTE ON DBMS_FGA TO TRASUA;');
END;
/


-- -------------------------------------------------------
-- 4B. Ghi log khi ai UPDATE ty le giam gia (product_discount)
-- -------------------------------------------------------
BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_FGA.DROP_POLICY(
            object_schema => ''TRASUA'',
            object_name   => ''PRODUCT_DISCOUNT'',
            policy_name   => ''FGA_DISCOUNT_UPDATE''
        );
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_FGA.ADD_POLICY(
            object_schema   => ''TRASUA'',
            object_name     => ''PRODUCT_DISCOUNT'',
            policy_name     => ''FGA_DISCOUNT_UPDATE'',
            audit_condition => NULL,
            audit_column    => ''DISCOUNTEDAMOUNT'',
            statement_types => ''UPDATE'',
            audit_trail     => DBMS_FGA.DB + DBMS_FGA.EXTENDED
        );
    END;';
    DBMS_OUTPUT.PUT_LINE('FGA da duoc cai dat: giam sat UPDATE DISCOUNTEDAMOUNT tren PRODUCT_DISCOUNT');
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/


-- -------------------------------------------------------
-- 4C. Ghi log khi tao phieu hoan tien bat thuong (bill_return)
-- -------------------------------------------------------
BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_FGA.DROP_POLICY(
            object_schema => ''TRASUA'',
            object_name   => ''BILL_RETURN'',
            policy_name   => ''FGA_BILL_RETURN_INSERT''
        );
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;';
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/

BEGIN
    EXECUTE IMMEDIATE '
    BEGIN
        DBMS_FGA.ADD_POLICY(
            object_schema   => ''TRASUA'',
            object_name     => ''BILL_RETURN'',
            policy_name     => ''FGA_BILL_RETURN_INSERT'',
            audit_condition => NULL,
            audit_column    => ''RETURNMONEY'',
            statement_types => ''INSERT'',
            audit_trail     => DBMS_FGA.DB + DBMS_FGA.EXTENDED
        );
    END;';
    DBMS_OUTPUT.PUT_LINE('FGA da duoc cai dat: giam sat INSERT tren BILL_RETURN');
EXCEPTION
    WHEN OTHERS THEN NULL;
END;
/


-- ============================================================
-- PHAN 5: DATA REDACTION
-- (Da duoc chuyen sang file 03_SECURITY_YEN.sql do Tan Yen phu trach)
-- ============================================================


-- ============================================================
-- PHAN 6: KIEM TRA VA BAO CAO KET QUA
-- Chay phan nay sau khi da chay xong tat ca cac Phan tren
-- ============================================================

-- Xem danh sach tat ca Stored Procedures da tao
SELECT object_name, object_type, status, last_ddl_time
FROM user_objects
WHERE object_type IN ('PROCEDURE', 'FUNCTION', 'PACKAGE')
ORDER BY object_type, object_name;

-- Xem cac VPD Policies da ap dung
SELECT object_owner, object_name, policy_name, function, enable
FROM all_policies
WHERE object_owner = 'TRASUA'
ORDER BY object_name;

-- Xem cac FGA Policies (LUU Y: FGA chi co tren Oracle Enterprise Edition)
-- Neu dung Oracle Free/XE thi bo qua query nay
-- SELECT object_name, policy_name, statement_types, enabled FROM user_audit_policies;
-- Thay the: kiem tra Standard Audit
SELECT username, action_name, obj_name, timestamp
FROM user_audit_trail
WHERE timestamp > SYSDATE - 1
ORDER BY timestamp DESC;

-- Xem cac Redaction Policies (LUU Y: Data Redaction chi co tren Oracle Enterprise Edition)
-- Neu dung Oracle Free/XE thi bo qua query nay
-- Kiem tra cac object da tao thay the:
SELECT object_name, object_type, status
FROM user_objects
WHERE object_type IN ('PROCEDURE', 'FUNCTION', 'TRIGGER')
ORDER BY object_type, object_name;

-- Demo chay Procedure phan bo san pham vao Chi nhanh 1
SET SERVEROUTPUT ON;
DECLARE
    v_min_branch NUMBER;
BEGIN
    SELECT MIN(id) INTO v_min_branch FROM branch;
    PROC_INIT_BRANCH_INVENTORY(
        p_branch_id   => v_min_branch,
        p_default_qty => 100
    );
END;
/

-- Demo tinh doanh thu Chi nhanh 1 thang nay
SELECT
    b.branch_name                                   AS ten_chi_nhanh,
    FN_GET_BRANCH_REVENUE(b.id)                     AS doanh_thu_thang_nay,
    FN_GET_BRANCH_REVENUE(b.id,
        TRUNC(SYSDATE)-30, SYSDATE)                 AS doanh_thu_30_ngay,
    (SELECT COUNT(*) FROM branch_inventory bi
     WHERE bi.branch_id = b.id AND bi.isActive = 1) AS tong_mat_hang
FROM branch b
ORDER BY b.id;

-- Xem ket qua kho hang chi nhanh sau khi chay Procedure
SELECT
    p.name      AS ten_san_pham,
    sp.name     AS "size",
    c.name      AS duong,
    bi.quantity AS so_luong_ton_kho,
    CASE bi.isActive
        WHEN 1 THEN 'Dang ban'
        ELSE 'Ngung ban'
    END         AS trang_thai
FROM branch_inventory bi
JOIN product_detail pd ON bi.product_detail_id = pd.id
JOIN product        p  ON pd.product_id = p.id
JOIN size_product   sp ON pd.size_id = sp.id
JOIN color          c  ON pd.color_id = c.id
JOIN branch         b  ON bi.branch_id = b.id
WHERE b.id = (SELECT MIN(id) FROM branch)
ORDER BY p.name, sp.name;
