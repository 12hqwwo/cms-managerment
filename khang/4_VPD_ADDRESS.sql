-- 4. VPD ĐỊA CHỈ GIAO HÀNG (Lọc ADDRESS_SHIPPING theo customer_id)
-- Tối ưu: Bảo mật cao & Hiệu năng tốt (Best Practices cho VPD)

ALTER SESSION SET CURRENT_SCHEMA = TRASUA;

-- 1. Policy Function
CREATE OR REPLACE FUNCTION fn_vpd_address (p_schema IN VARCHAR2, p_table IN VARCHAR2) RETURN VARCHAR2 AS
    v_cus_id VARCHAR2(100);
BEGIN
    v_cus_id := SYS_CONTEXT('ctx_trasua', 'customer_id');

    -- TH1: DBA/Owner chạy trực tiếp trên DB -> Cho xem hết
    IF SYS_CONTEXT('USERENV', 'SESSION_USER') IN ('SYSTEM', 'SYS', 'TRASUA') AND v_cus_id IS NULL THEN 
        RETURN '1=1'; 
    END IF;

    -- TH2: Ứng dụng chưa login -> Block toàn bộ (1=2) tránh rò rỉ dữ liệu. Code cũ dùng 1=1 rất nguy hiểm.
    IF v_cus_id IS NULL THEN 
        RETURN '1=2'; 
    END IF;

    -- TH3: User hợp lệ. 
    -- Tối ưu hiệu năng (Shared Pool): Đưa thẳng SYS_CONTEXT vào chuỗi thay vì nối chuỗi (||). Tránh Hard Parse.
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
