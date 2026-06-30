-- 4. VPD ĐỊA CHỈ GIAO HÀNG (Lọc ADDRESS_SHIPPING theo customer_id)
-- Tối ưu: Bảo mật cao & Hiệu năng tốt (Best Practices cho VPD)

ALTER SESSION SET CURRENT_SCHEMA = TRASUA;

-- 1. Policy Function
CREATE OR REPLACE FUNCTION fn_vpd_address (p_schema IN VARCHAR2, p_table IN VARCHAR2) RETURN VARCHAR2 AS
    v_acc_id VARCHAR2(100);
    v_role_id VARCHAR2(100);
BEGIN
    v_acc_id := SYS_CONTEXT('ctx_trasua', 'account_id');
    v_role_id := SYS_CONTEXT('ctx_trasua', 'role_id');

    -- TH1: DBA chạy trực tiếp trên DB bằng quyền cao nhất -> Cho xem hết
    IF SYS_CONTEXT('USERENV', 'SESSION_USER') IN ('SYSTEM', 'SYS') AND v_acc_id IS NULL THEN 
        RETURN '1=1'; 
    END IF;
    IF v_acc_id IS NULL THEN RETURN '1=2'; END IF;
    
    -- Admin(1), Staff(2), Vendor(5)
    IF v_role_id IN ('1', '2', '5') THEN
        RETURN '1=1';
    END IF;

    RETURN 'customer_id = SYS_CONTEXT(''ctx_trasua'', ''customer_id'')';
END;
/

-- 2. Áp dụng Policy
BEGIN
    BEGIN 
        DBMS_RLS.DROP_POLICY('TRASUA', 'ADDRESS_SHIPPING', 'POLICY_ADDRESS'); 
    EXCEPTION 
        WHEN OTHERS THEN NULL; 
    END;
    
    DBMS_RLS.ADD_POLICY(
        object_schema   => 'TRASUA',
        object_name     => 'ADDRESS_SHIPPING',
        policy_name     => 'POLICY_ADDRESS',
        function_schema => 'TRASUA',
        policy_function => 'fn_vpd_address',
        statement_types => 'SELECT, UPDATE, DELETE',
        update_check    => TRUE -- Chặn UPDATE địa chỉ sang ID người khác
    );
END;
/
