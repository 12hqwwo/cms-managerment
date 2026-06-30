-- 5. VPD TRẠNG THÁI ĐƠN HÀNG (Lọc BILL theo customer_id)
-- Tối ưu: Bảo mật cao & Hiệu năng tốt (Best Practices cho VPD)
-- LƯU Ý: File này chỉ áp dụng VPD cho OrderStatus (user thường xem đơn của mình).
--         Bảng BILL còn có OLS (file 8) cho phân quyền cấp quản lý (STAFF/MANAGER/DIRECTOR).
--         Hai cơ chế hoạt động cùng nhau không xung đột.

ALTER SESSION SET CURRENT_SCHEMA = TRASUA;

-- 1. Policy Function
CREATE OR REPLACE FUNCTION fn_vpd_bill (p_schema IN VARCHAR2, p_table IN VARCHAR2) RETURN VARCHAR2 AS
    v_acc_id VARCHAR2(100);
    v_role_id VARCHAR2(100);
BEGIN
    v_acc_id := SYS_CONTEXT('ctx_trasua', 'account_id');
    v_role_id := SYS_CONTEXT('ctx_trasua', 'role_id');

    -- TH1: DBA chạy trực tiếp trên DB bằng quyền cao nhất -> Cho xem hết
    IF SYS_CONTEXT('USERENV', 'SESSION_USER') IN ('SYSTEM', 'SYS') AND v_acc_id IS NULL THEN 
        RETURN '1=1'; 
    END IF;

    -- TH2: Ứng dụng chưa login -> Block toàn bộ.
    IF v_acc_id IS NULL THEN
        RETURN '1=2';
    END IF;
    
    -- TH3: Admin(1) hoặc Vendor(5) -> Xem toàn bộ
    IF v_role_id IN ('1', '5') THEN
        RETURN '1=1';
    END IF;
    
    -- TH4: Staff(2) -> Xem theo chi nhánh của mình
    IF v_role_id = '2' THEN
        RETURN 'branch_id = SYS_CONTEXT(''ctx_trasua'', ''branch_id'')';
    END IF;

    -- TH5: Customer (3, 4) -> Xem theo customer_id
    RETURN 'customer_id = SYS_CONTEXT(''ctx_trasua'', ''customer_id'')';
END;
/

-- 2. Áp dụng Policy
BEGIN
    BEGIN
        DBMS_RLS.DROP_POLICY('TRASUA', 'BILL', 'POLICY_BILL_STATUS');
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;

    DBMS_RLS.ADD_POLICY(
        object_schema   => 'TRASUA',
        object_name     => 'BILL',
        policy_name     => 'POLICY_BILL_STATUS',
        function_schema => 'TRASUA',
        policy_function => 'fn_vpd_bill',
        statement_types => 'SELECT, UPDATE, DELETE',
        update_check    => TRUE -- Chặn UPDATE bill_id sang customer khác
    );
END;
/
