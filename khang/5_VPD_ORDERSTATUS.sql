-- 5. VPD TRẠNG THÁI ĐƠN HÀNG (Lọc BILL theo customer_id)
-- Tối ưu: Bảo mật cao & Hiệu năng tốt (Best Practices cho VPD)
-- LƯU Ý: File này chỉ áp dụng VPD cho OrderStatus (user thường xem đơn của mình).
--         Bảng BILL còn có OLS (file 8) cho phân quyền cấp quản lý (STAFF/MANAGER/DIRECTOR).
--         Hai cơ chế hoạt động cùng nhau không xung đột.

ALTER SESSION SET CURRENT_SCHEMA = TRASUA;

-- 1. Policy Function
CREATE OR REPLACE FUNCTION fn_vpd_bill (p_schema IN VARCHAR2, p_table IN VARCHAR2) RETURN VARCHAR2 AS
    v_cus_id VARCHAR2(100);
BEGIN
    v_cus_id := SYS_CONTEXT('ctx_trasua', 'customer_id');

    -- TH1: DBA/Owner chạy trực tiếp -> Cho xem hết
    IF SYS_CONTEXT('USERENV', 'SESSION_USER') IN ('SYSTEM', 'SYS', 'TRASUA') AND v_cus_id IS NULL THEN
        RETURN '1=1';
    END IF;

    -- TH2: Ứng dụng chưa login -> Block toàn bộ. Code cũ dùng 1=1 rất nguy hiểm.
    IF v_cus_id IS NULL THEN
        RETURN '1=2';
    END IF;

    -- TH3: User hợp lệ (Tối ưu hiệu năng: tránh Hard Parse)
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
