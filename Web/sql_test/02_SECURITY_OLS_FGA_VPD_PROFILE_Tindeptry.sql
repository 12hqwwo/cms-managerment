-- ==============================================================================
-- TẬP LỆNH BẢO MẬT CƠ SỞ DỮ LIỆU ORACLE (Mức độ Dễ, Vừa, Khó)
-- Tác giả: Tín (Thành viên 4)
-- Yêu cầu: 
-- 1. Chạy tập lệnh này bằng user SYS hoặc user có đặc quyền DBA (SYSDBA).
-- 2. Đã kích hoạt Oracle Label Security (chopt enable ols).
-- ==============================================================================

-- Thay đổi session context thành schema của dự án (Giả sử là C##WEB_ALOTRA, bạn cần thay đổi tùy vào tên Schema thực tế)
-- ALTER SESSION SET CURRENT_SCHEMA = C##WEB_ALOTRA;


-- ==============================================================================
-- MỨC ĐỘ DỄ: THIẾT LẬP RBAC CƠ BẢN (ROLES & PRIVILEGES)
-- Mục tiêu: Quản trị Dashboard và Chi nhánh
-- ==============================================================================
PROMPT --- 1. SETUP RBAC ROLES ---

-- Tạo các Roles
CREATE ROLE ROLE_ADMIN;
CREATE ROLE ROLE_VENDOR;
CREATE ROLE ROLE_STAFF;

-- Phân quyền cho ADMIN (Full quyền trên Dashboard và Quản lý)
GRANT SELECT, INSERT, UPDATE, DELETE ON branch TO ROLE_ADMIN;
GRANT SELECT, INSERT, UPDATE, DELETE ON account TO ROLE_ADMIN;
GRANT SELECT, INSERT, UPDATE, DELETE ON bill TO ROLE_ADMIN;

-- Phân quyền cho VENDOR (Chỉ được xem/cập nhật branch của mình, quản lý bill)
GRANT SELECT ON branch TO ROLE_VENDOR;
GRANT SELECT, INSERT, UPDATE ON bill TO ROLE_VENDOR;
GRANT SELECT ON account TO ROLE_VENDOR;

-- Phân quyền cho STAFF (Chỉ xử lý đơn hàng POS)
GRANT SELECT, INSERT, UPDATE ON bill TO ROLE_STAFF;
GRANT SELECT ON branch TO ROLE_STAFF;

-- Ghi chú: Cần Grant trực tiếp ROLE này cho các Oracle User tương ứng nếu ứng dụng map User Database với User App.
-- Ví dụ: GRANT ROLE_ADMIN TO C##ADMIN_USER;


-- ==============================================================================
-- MỨC ĐỘ VỪA: GIÁM SÁT FGA (FINE-GRAINED AUDITING)
-- Mục tiêu: Giám sát mã giảm giá và giảm giá sản phẩm
-- ==============================================================================
PROMPT --- 2. SETUP FINE-GRAINED AUDITING (FGA) ---

BEGIN
    -- Xóa policy cũ nếu tồn tại để tránh lỗi
    BEGIN DBMS_FGA.DROP_POLICY(object_schema => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), object_name => 'discount_code', policy_name => 'AUDIT_DISCOUNT_CODE'); EXCEPTION WHEN OTHERS THEN NULL; END;
    BEGIN DBMS_FGA.DROP_POLICY(object_schema => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), object_name => 'product_discount', policy_name => 'AUDIT_PRODUCT_DISCOUNT'); EXCEPTION WHEN OTHERS THEN NULL; END;

    -- 1. Audit bảng discount_code (Giám sát khi có người INSERT, UPDATE, DELETE hoặc truy vấn mã giảm giá VIP)
    DBMS_FGA.ADD_POLICY(
        object_schema   => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        object_name     => 'discount_code',
        policy_name     => 'AUDIT_DISCOUNT_CODE',
        audit_condition => 'percentage >= 50', -- Chỉ giám sát các mã giảm giá lớn hơn 50%
        audit_column    => 'code, percentage',
        statement_types => 'SELECT, INSERT, UPDATE, DELETE'
    );

    -- 2. Audit bảng product_discount
    DBMS_FGA.ADD_POLICY(
        object_schema   => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        object_name     => 'product_discount',
        policy_name     => 'AUDIT_PRODUCT_DISCOUNT',
        audit_condition => NULL, -- Giám sát mọi hành động thay đổi
        statement_types => 'INSERT, UPDATE, DELETE'
    );
END;
/


-- ==============================================================================
-- MỨC ĐỘ KHÓ: TÍCH HỢP OLS & PROFILE PHỨC TẠP
-- Mục tiêu: Quản lý tài khoản (OLS), Doanh thu (VPD+OLS), Vendor (VPD+OLS), POS (Profile+OLS)
-- ==============================================================================

PROMPT --- 3.1 SETUP ORACLE PROFILES ---
-- Giới hạn session = 1 cho máy POS tại quầy
CREATE PROFILE POS_PROFILE LIMIT
    SESSIONS_PER_USER 1
    IDLE_TIME 60;

-- Gán profile này cho các user Staff/POS (Ví dụ)
-- ALTER USER c##pos_user1 PROFILE POS_PROFILE;


PROMPT --- 3.2 SETUP OLS (ORACLE LABEL SECURITY) ---

EXEC SA_SYSDBA.CREATE_POLICY(policy_name => 'ACCESS_POLICY', column_name => 'ols_label');

-- 1. Tạo Levels
EXEC SA_COMPONENTS.CREATE_LEVEL('ACCESS_POLICY', 1000, 'PUB', 'PUBLIC');
EXEC SA_COMPONENTS.CREATE_LEVEL('ACCESS_POLICY', 2000, 'CONF', 'CONFIDENTIAL');
EXEC SA_COMPONENTS.CREATE_LEVEL('ACCESS_POLICY', 3000, 'SEC', 'SECRET');

-- 2. Tạo Compartments (Bộ phận)
EXEC SA_COMPONENTS.CREATE_COMPARTMENT('ACCESS_POLICY', 100, 'ADM', 'ADMINISTRATION');
EXEC SA_COMPONENTS.CREATE_COMPARTMENT('ACCESS_POLICY', 200, 'MNG', 'MANAGEMENT');
EXEC SA_COMPONENTS.CREATE_COMPARTMENT('ACCESS_POLICY', 300, 'OPR', 'OPERATIONS');

-- 3. Tạo Groups (Theo Chi nhánh)
EXEC SA_COMPONENTS.CREATE_GROUP('ACCESS_POLICY', 10, 'GLOBAL', 'ALL BRANCHES');
EXEC SA_COMPONENTS.CREATE_GROUP('ACCESS_POLICY', 20, 'BR1', 'BRANCH 1', 'GLOBAL');
EXEC SA_COMPONENTS.CREATE_GROUP('ACCESS_POLICY', 30, 'BR2', 'BRANCH 2', 'GLOBAL');

-- A. THIẾT LẬP NHÃN CHO USER (USER AUTHORIZATION)
-- (Lưu ý: Thay thế các user C##ADMIN, C##VENDOR_BR1 bằng user Database thực tế của bạn)
-- Cấp nhãn cho Admin (Được đọc ghi mọi thứ)
-- EXEC SA_USER_ADMIN.SET_USER_LABELS('ACCESS_POLICY', 'C##ADMIN', 'SEC:ADM,MNG,OPR:GLOBAL');
-- Cấp nhãn cho Vendor Chi Nhánh 1 (Chỉ đọc/ghi dữ liệu Management & Operation của Branch 1)
-- EXEC SA_USER_ADMIN.SET_USER_LABELS('ACCESS_POLICY', 'C##VENDOR_BR1', 'SEC:MNG,OPR:BR1');
-- Cấp nhãn cho Staff Chi Nhánh 1 (Chỉ đọc/ghi dữ liệu Operation của Branch 1)
-- EXEC SA_USER_ADMIN.SET_USER_LABELS('ACCESS_POLICY', 'C##STAFF_BR1', 'CONF:OPR:BR1');


-- D. XỬ LÝ TRẠNG THÁI 'NO_CONTROL' (Bật kiểm soát thực sự)
-- Bước 1: Áp dụng Policy với NO_CONTROL để tạo cột ols_label
EXEC SA_POLICY_ADMIN.APPLY_TABLE_POLICY('ACCESS_POLICY', SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), 'account', 'NO_CONTROL');
EXEC SA_POLICY_ADMIN.APPLY_TABLE_POLICY('ACCESS_POLICY', SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), 'bill', 'NO_CONTROL');

-- Bước 2: Cập nhật nhãn mặc định cho các dữ liệu cũ (Nếu không có bước này thì dữ liệu cũ bị NULL nhãn và sẽ biến mất khi bật OLS)
UPDATE account SET ols_label = CHAR_TO_LABEL('ACCESS_POLICY', 'PUB') WHERE ols_label IS NULL;
UPDATE bill SET ols_label = CHAR_TO_LABEL('ACCESS_POLICY', 'PUB') WHERE ols_label IS NULL;
COMMIT;

-- B. TẠO HÀM TỰ ĐỘNG GÁN NHÃN KHI INSERT (LABEL FUNCTION)
CREATE OR REPLACE FUNCTION gen_bill_label (p_branch_id IN NUMBER) RETURN SA_LABEL IS
BEGIN
    IF p_branch_id = 1 THEN
        RETURN TO_SA_LABEL('ACCESS_POLICY', 'CONF:OPR:BR1');
    ELSIF p_branch_id = 2 THEN
        RETURN TO_SA_LABEL('ACCESS_POLICY', 'CONF:OPR:BR2');
    END IF;
    RETURN TO_SA_LABEL('ACCESS_POLICY', 'PUB');
END;
/

CREATE OR REPLACE FUNCTION gen_account_label (p_branch_id IN NUMBER) RETURN SA_LABEL IS
BEGIN
    IF p_branch_id = 1 THEN
        RETURN TO_SA_LABEL('ACCESS_POLICY', 'SEC:MNG:BR1');
    ELSIF p_branch_id = 2 THEN
        RETURN TO_SA_LABEL('ACCESS_POLICY', 'SEC:MNG:BR2');
    END IF;
    RETURN TO_SA_LABEL('ACCESS_POLICY', 'PUB');
END;
/

-- Bước 3: Đổi Policy sang thực thi (Có bật READ_CONTROL, WRITE_CONTROL và tự động gọi Label Function)
BEGIN
    SA_POLICY_ADMIN.REMOVE_TABLE_POLICY('ACCESS_POLICY', SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), 'account');
    SA_POLICY_ADMIN.APPLY_TABLE_POLICY(
        policy_name    => 'ACCESS_POLICY',
        schema_name    => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        table_name     => 'account',
        table_options  => 'READ_CONTROL,WRITE_CONTROL,CHECK_CONTROL',
        label_function => 'gen_account_label(:new.branch_id)'
    );

    SA_POLICY_ADMIN.REMOVE_TABLE_POLICY('ACCESS_POLICY', SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), 'bill');
    SA_POLICY_ADMIN.APPLY_TABLE_POLICY(
        policy_name    => 'ACCESS_POLICY',
        schema_name    => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        table_name     => 'bill',
        table_options  => 'READ_CONTROL,WRITE_CONTROL,CHECK_CONTROL',
        label_function => 'gen_bill_label(:new.branch_id)'
    );
END;
/


PROMPT --- 3.3 SETUP VPD (VIRTUAL PRIVATE DATABASE) ---

-- Tạo một Context package để quản lý VPD
CREATE OR REPLACE CONTEXT branch_ctx USING pkg_branch_sec;

-- Tạo Package Set Context
CREATE OR REPLACE PACKAGE pkg_branch_sec IS
    PROCEDURE set_branch_id(p_branch_id NUMBER);
END;
/
CREATE OR REPLACE PACKAGE BODY pkg_branch_sec IS
    PROCEDURE set_branch_id(p_branch_id NUMBER) IS
    BEGIN
        DBMS_SESSION.SET_CONTEXT('branch_ctx', 'branch_id', TO_CHAR(p_branch_id));
    END;
END;
/

-- C. BẢO MẬT GÓI VPD (IMPEDANCE MISMATCH VỚI JAVA)
-- Thu hồi quyền gọi hàm của tất cả người dùng (Để tránh bị gọi lụi từ user khác)
REVOKE EXECUTE ON pkg_branch_sec FROM PUBLIC;
-- Chỉ cấp quyền gọi cho User đang được Spring Boot sử dụng (Bạn sửa tên user cho phù hợp)
-- GRANT EXECUTE ON pkg_branch_sec TO C##DB_APP;

-- Tạo Policy Function cho VPD
CREATE OR REPLACE FUNCTION fn_vpd_branch_sec(p_schema IN VARCHAR2, p_table IN VARCHAR2)
RETURN VARCHAR2
IS
    v_role VARCHAR2(100);
    v_branch_id VARCHAR2(10);
BEGIN
    -- Lấy role hoặc branch id từ ứng dụng truyền vào qua Context
    v_branch_id := SYS_CONTEXT('branch_ctx', 'branch_id');
    
    -- Nếu là Admin (chưa set branch_id hoặc branch_id = 0), được xem tất cả
    IF v_branch_id IS NULL OR v_branch_id = '0' THEN
        RETURN ''; 
    ELSE
        -- Nếu là Vendor/Staff, chỉ được xem row thuộc chi nhánh của mình
        RETURN 'branch_id = ' || v_branch_id;
    END IF;
END;
/

-- Add RLS Policy cho bảng BILL (Thống kê doanh thu)
BEGIN
    BEGIN DBMS_RLS.DROP_POLICY(SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), 'bill', 'VPD_POLICY_BILL_BRANCH'); EXCEPTION WHEN OTHERS THEN NULL; END;
    
    DBMS_RLS.ADD_POLICY(
        object_schema   => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        object_name     => 'bill',
        policy_name     => 'VPD_POLICY_BILL_BRANCH',
        function_schema => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        policy_function => 'fn_vpd_branch_sec',
        statement_types => 'SELECT, UPDATE, DELETE'
    );
END;
/

-- Add RLS Policy cho bảng ACCOUNT (Tài khoản Vendor theo nhánh)
BEGIN
    BEGIN DBMS_RLS.DROP_POLICY(SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'), 'account', 'VPD_POLICY_ACCOUNT_BRANCH'); EXCEPTION WHEN OTHERS THEN NULL; END;
    
    DBMS_RLS.ADD_POLICY(
        object_schema   => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        object_name     => 'account',
        policy_name     => 'VPD_POLICY_ACCOUNT_BRANCH',
        function_schema => SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA'),
        policy_function => 'fn_vpd_branch_sec',
        statement_types => 'SELECT, UPDATE, DELETE'
    );
END;
/

PROMPT === HOÀN TẤT THIẾT LẬP BẢO MẬT OLS, FGA, VPD, PROFILE, RBAC ===
