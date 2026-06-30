-- 8. OLS HÓA ĐƠN (PHÂN QUYỀN TRÊN BẢNG BILL THEO COMPARTMENT)
-- ==========================================
-- PHẦN 1: CHẠY BẰNG USER CÓ QUYỀN LBAC_DBA (VD: LBACSYS hoặc SYS)
-- ==========================================
GRANT INHERIT PRIVILEGES ON USER SYS TO LBACSYS;

BEGIN
    -- 1. Xóa Policy cũ nếu có
    BEGIN
        SA_SYSDBA.DROP_POLICY('BILL_OLS_POL', TRUE);
    EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 2. Tạo Policy
    SA_SYSDBA.CREATE_POLICY(
        policy_name => 'BILL_OLS_POL', 
        column_name => 'OLS_LABEL',
        default_options => 'READ_CONTROL,WRITE_CONTROL'
    );

    -- 3. Tạo Levels (Cấp bậc: 10, 20, 30, 40)
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 10, 'CUSTOMER', 'Customer Level');
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 20, 'VENDOR', 'Vendor Level');
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 30, 'STAFF', 'Staff Level');
    SA_COMPONENTS.CREATE_LEVEL('BILL_OLS_POL', 40, 'ADMIN', 'Admin Level');

    -- 3.1 Tạo Compartments cho các chi nhánh (Dựa trên branch_id và branch_code)
    SA_COMPONENTS.CREATE_COMPARTMENT('BILL_OLS_POL', 10, 'CN008', 'Chi nhanh CN008');
    SA_COMPONENTS.CREATE_COMPARTMENT('BILL_OLS_POL', 20, 'CN009', 'Chi nhanh CN009');
    SA_COMPONENTS.CREATE_COMPARTMENT('BILL_OLS_POL', 30, 'CN011', 'Chi nhanh CN011');
    SA_COMPONENTS.CREATE_COMPARTMENT('BILL_OLS_POL', 40, 'CN012', 'Chi nhanh CN012');
    SA_COMPONENTS.CREATE_COMPARTMENT('BILL_OLS_POL', 50, 'BR_AG', 'Chi nhanh BR_AG');

    -- 4. Tạo Labels
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 10, 'CUSTOMER');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 20, 'VENDOR');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 30, 'STAFF');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 40, 'ADMIN');

    -- Tạo Label kết hợp Level và Compartment cho nhân viên từng chi nhánh
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 31, 'STAFF:CN008');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 32, 'STAFF:CN009');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 33, 'STAFF:CN011');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 34, 'STAFF:CN012');
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 35, 'STAFF:BR_AG');

    -- Tạo Label cao nhất cho Admin (Thấy tất cả các chi nhánh)
    SA_LABEL_ADMIN.CREATE_LABEL('BILL_OLS_POL', 41, 'ADMIN:CN008,CN009,CN011,CN012,BR_AG');

    -- 5. Gắn Policy vào bảng TRASUA.BILL
    SA_POLICY_ADMIN.APPLY_TABLE_POLICY(
        policy_name => 'BILL_OLS_POL', 
        schema_name => 'TRASUA', 
        table_name  => 'BILL'
    );

    -- 6. Phân quyền nhãn cao nhất cho User Database (TRASUA)
    -- Giúp user TRASUA có thể thao tác với mọi Compartment
    SA_USER_ADMIN.SET_USER_LABELS(
        policy_name => 'BILL_OLS_POL',
        user_name   => 'TRASUA',
        max_read_label => 'ADMIN:CN008,CN009,CN011,CN012,BR_AG'
    );
END;
/

-- Cấp quyền dùng package Session cho ứng dụng Java
GRANT EXECUTE ON SA_SESSION TO TRASUA;

-- ==========================================
-- PHẦN 2: CHẠY BẰNG USER TRASUA
-- Phân loại nhãn cho dữ liệu hiện có trong Database theo Chi nhánh
-- ==========================================
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF:CN008') WHERE branch_id = 8;
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF:CN009') WHERE branch_id = 9;
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF:CN011') WHERE branch_id = 11;
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF:CN012') WHERE branch_id = 12;
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF:BR_AG') WHERE branch_id = 13;

-- Các đơn hàng không thuộc chi nhánh cụ thể (online)
UPDATE TRASUA.BILL SET OLS_LABEL = CHAR_TO_LABEL('BILL_OLS_POL', 'STAFF') WHERE branch_id IS NULL;

COMMIT;


