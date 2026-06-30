-- 3. VPD GIỎ HÀNG (Lọc CART theo customer_id)
-- Tối ưu: Bảo mật cao & Hiệu năng tốt (Best Practices cho VPD)

ALTER SESSION SET CURRENT_SCHEMA = TRASUA;

-- 1. Tạo Context
CREATE OR REPLACE CONTEXT ctx_trasua USING TRASUA.pkg_vpd_security;

-- 2. Package quản lý Context
CREATE OR REPLACE PACKAGE pkg_vpd_security AS
    PROCEDURE set_account_id(p_account_id IN NUMBER);
END pkg_vpd_security;
/

CREATE OR REPLACE PACKAGE BODY pkg_vpd_security AS
    PROCEDURE set_account_id(p_account_id IN NUMBER) IS
        v_customer_id account.customer_id%TYPE;
    BEGIN
        -- Clear context cũ trước khi set mới tránh rò rỉ dữ liệu (Best practice)
        DBMS_SESSION.CLEAR_CONTEXT('ctx_trasua', 'account_id');
        DBMS_SESSION.CLEAR_CONTEXT('ctx_trasua', 'customer_id');

        IF p_account_id IS NOT NULL THEN
            DBMS_SESSION.SET_CONTEXT('ctx_trasua', 'account_id', p_account_id);
            BEGIN
                SELECT customer_id INTO v_customer_id FROM account WHERE id = p_account_id;
                IF v_customer_id IS NOT NULL THEN
                    DBMS_SESSION.SET_CONTEXT('ctx_trasua', 'customer_id', v_customer_id);
                END IF;
            EXCEPTION 
                WHEN NO_DATA_FOUND THEN NULL; 
            END;
        END IF;
    END set_account_id;
END pkg_vpd_security;
/

-- 3. Policy Function
CREATE OR REPLACE FUNCTION fn_vpd_cart (p_schema IN VARCHAR2, p_table IN VARCHAR2) RETURN VARCHAR2 AS
    v_acc_id VARCHAR2(100);
BEGIN
    v_acc_id := SYS_CONTEXT('ctx_trasua', 'account_id');

    -- TH1: DBA/Owner chạy trực tiếp trên DB -> Cho xem hết
    IF SYS_CONTEXT('USERENV', 'SESSION_USER') IN ('SYSTEM', 'SYS', 'TRASUA') AND v_acc_id IS NULL THEN 
        RETURN '1=1'; 
    END IF;

    -- TH2: Ứng dụng chưa login -> Block toàn bộ (1=2) tránh rò rỉ dữ liệu. Code cũ dùng 1=1 rất nguy hiểm.
    IF v_acc_id IS NULL THEN 
        RETURN '1=2'; 
    END IF;

    -- TH3: User hợp lệ. 
    -- Tối ưu hiệu năng (Shared Pool): Đưa thẳng SYS_CONTEXT vào chuỗi thay vì nối chuỗi (||). Tránh Hard Parse.
    RETURN 'account_id IN (SELECT id FROM account WHERE customer_id = SYS_CONTEXT(''ctx_trasua'', ''customer_id''))';
END;
/

-- 4. Áp dụng Policy
BEGIN
    BEGIN 
        DBMS_RLS.DROP_POLICY('TRASUA', 'CART', 'POLICY_CART'); 
    EXCEPTION 
        WHEN OTHERS THEN NULL; 
    END;
    
    DBMS_RLS.ADD_POLICY(
        object_schema   => 'TRASUA',
        object_name     => 'CART',
        policy_name     => 'POLICY_CART',
        function_schema => 'TRASUA',
        policy_function => 'fn_vpd_cart',
        statement_types => 'SELECT, UPDATE, DELETE',
        update_check    => TRUE -- Chặn UPDATE dữ liệu sang quyền người khác
    );
END;
/
