-- 8. OLS HÓA ĐƠN (PHÂN QUYỀN TRÊN BẢNG BILL)
-- ==========================================
-- PHẦN 1: CHẠY BẰNG USER CÓ QUYỀN LBAC_DBA (VD: LBACSYS hoặc SYS)
-- ==========================================
BEGIN
    -- 1. Xóa Policy cũ nếu có (để script chạy lại nhiều lần không lỗi)
    BEGIN
        SA_SYSDBA.DROP_POLICY('BILL_OLS_POL', TRUE);
    EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 2. Tạo Policy
    SA_SYSDBA.CREATE_POLICY(
        policy_name => 'BILL_OLS_POL', 
        column_name => 'OLS_LABEL',
        default_options => 'READ_CONTROL,WRITE_CONTROL'
    );

    -- 3. Tạo Levels (Cấp bậc: 10, 20, 30)
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 10, 'STAFF', 'Staff Level');
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 20, 'MANAGER', 'Manager Level');
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 30, 'DIRECTOR', 'Director Level');

    -- 4. Tạo Labels
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 10, 'STAFF');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 20, 'MANAGER');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 30, 'DIRECTOR');

    -- 5. Gắn Policy vào bảng TRASUA.BILL
    SA_POLICY_ADMIN.APPLY_TABLE_POLICY(
        policy_name => 'BILL_OLS_POL', 
        schema_name => 'TRASUA', 
        table_name  => 'BILL'
    );

    -- 6. Phân quyền nhãn cao nhất cho User Database (TRASUA)
    -- QUAN TRỌNG: Nếu thiếu, Java gọi SA_SESSION.SET_LABEL sẽ văng lỗi ORA-12407 (Unauthorized)
    SA_USER_ADMIN.SET_USER_LABELS(
        policy_name => 'BILL_OLS_POL',
        user_name   => 'TRASUA',
        max_read_label => 'DIRECTOR'
    );
END;
/

-- Cấp quyền dùng package Session cho ứng dụng Java
GRANT EXECUTE ON SA_SESSION TO TRASUA;

-- ==========================================
-- PHẦN 2: CHẠY BẰNG USER TRASUA
-- Phân loại nhãn cho dữ liệu hiện có trong Database
-- ==========================================
-- Hóa đơn < 500k -> STAFF (Staff, Manager, Director đều thấy)
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF') WHERE amount < 500000;

-- Hóa đơn >= 500k -> MANAGER (Chỉ Manager, Director thấy)
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'MANAGER') WHERE amount >= 500000;

COMMIT;
