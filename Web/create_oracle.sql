-- ==============================================================================
-- PHẦN 1: KHỞI TẠO VÙNG NHỚ (TABLESPACE), USER VÀ CẤP QUYỀN
-- (BẮT BUỘC CHẠY BẰNG TÀI KHOẢN SYS AS SYSDBA)
-- ==============================================================================

-- 1. Tạo Không Gian Lưu Trữ (Tablespace) chuyên biệt cho ứng dụng Trà Sữa
CREATE TABLESPACE TS_TRASUA
    DATAFILE 'ts_trasua01.dbf' 
    SIZE 256M
    AUTOEXTEND ON NEXT 64M MAXSIZE UNLIMITED
    SEGMENT SPACE MANAGEMENT AUTO;

-- 2. Tạo User TRASUA gắn liền với vùng nhớ mặc định vừa khởi tạo
CREATE USER TRASUA
    IDENTIFIED BY "TraSua@2024"
    DEFAULT   TABLESPACE TS_TRASUA
    TEMPORARY TABLESPACE TEMP
    QUOTA UNLIMITED ON TS_TRASUA;

-- 3. Cấp quyền kết nối và khởi tạo đối tượng dữ liệu cho User
GRANT CREATE SESSION TO TRASUA;
GRANT CREATE TABLE     TO TRASUA;
GRANT CREATE SEQUENCE  TO TRASUA;
GRANT CREATE VIEW      TO TRASUA;
GRANT CREATE PROCEDURE TO TRASUA;
GRANT CREATE TRIGGER   TO TRASUA;
GRANT CREATE TYPE      TO TRASUA;
GRANT CREATE SYNONYM   TO TRASUA;
GRANT CREATE INDEX     TO TRASUA;

-- 4. Cấp quyền bổ sung hỗ trợ cơ chế quét metadata hệ thống của Spring Data JPA & Hibernate
GRANT SELECT ON SYS.V_$SESSION TO TRASUA;
GRANT SELECT ON SYS.V_$MYSTAT  TO TRASUA;
GRANT SELECT ON SYS.ALL_SEQUENCES   TO TRASUA;
GRANT SELECT ON SYS.ALL_TABLES      TO TRASUA;
GRANT SELECT ON SYS.ALL_CONSTRAINTS TO TRASUA;

COMMIT;



-- ==============================================================================
-- PHẦN 2: ĐỊNH NGHĨA CẤU TRÚC BẢNG DỮ LIỆU (DDL)
-- (CHUYỂN SANG KẾT NỐI BẰNG USER: TRASUA ĐỂ CHẠY)
-- ==============================================================================
SET SERVEROUTPUT ON;

CREATE TABLE role (
    id          NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    create_date TIMESTAMP(6)  NULL,
    name        VARCHAR2(50)  NOT NULL,
    update_date TIMESTAMP(6)  NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE customer (
    id           NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code         VARCHAR2(255)  NULL,
    email        VARCHAR2(255)  NULL,
    name         NVARCHAR2(255) NULL,
    phone_number VARCHAR2(255)  NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE branch (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_name NVARCHAR2(255) NOT NULL,
    address     NVARCHAR2(255) NULL,
    phone       NVARCHAR2(20)  NULL,
    email       NVARCHAR2(100) NULL,
    is_active   NUMBER(1)      DEFAULT 1 NULL,
    create_date TIMESTAMP(6)   NULL,
    update_date TIMESTAMP(6)   NULL,
    branch_code NVARCHAR2(255) NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE account (
    id            NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    birth_day     TIMESTAMP(6)  NULL,
    code          VARCHAR2(255) NULL,
    create_date   TIMESTAMP(6)  NULL,
    email         VARCHAR2(255) NULL,
    is_non_locked NUMBER(1)     DEFAULT 1 NOT NULL,
    password      VARCHAR2(255) NULL,
    update_date   TIMESTAMP(6)  NULL,
    customer_id   NUMBER(19)    NULL,
    role_id       NUMBER(19)    NULL,
    branch_id     NUMBER(19)    NULL,
    CONSTRAINT UK_account_code UNIQUE (code)
) TABLESPACE TS_TRASUA;

CREATE TABLE address_shipping (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    address     NVARCHAR2(150) NOT NULL,
    customer_id NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE payment_method (
    id     NUMBER(19)    NOT NULL,
    name   VARCHAR2(255) NULL,
    status NUMBER(10)    DEFAULT 0 NOT NULL,
    CONSTRAINT PK_payment_method PRIMARY KEY (id)
) TABLESPACE TS_TRASUA;

CREATE TABLE discount_code (
    id                     NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                   NVARCHAR2(255) NULL,
    delete_flag            NUMBER(1)      DEFAULT 0 NOT NULL,
    detail                 NVARCHAR2(255) NULL,
    discount_amount        FLOAT(53)      NULL,
    end_date               TIMESTAMP(6)   NULL,
    maximum_amount         NUMBER(10)     NULL,
    maximum_usage          NUMBER(10)     NULL,
    minimum_amount_in_cart FLOAT(53)      NULL,
    percentage             NUMBER(10)     NULL,
    start_date             TIMESTAMP(6)   NULL,
    status                 NUMBER(10)     DEFAULT 0 NOT NULL,
    type                   NUMBER(10)     DEFAULT 0 NOT NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE discount_code_backup (
    id                     NUMBER(19)     NOT NULL,
    code                   NVARCHAR2(255) NULL,
    delete_flag            NUMBER(1)      DEFAULT 0 NOT NULL,
    detail                 NVARCHAR2(255) NULL,
    discount_amount        FLOAT(53)      NULL,
    end_date               TIMESTAMP(6)   NULL,
    maximum_amount         NUMBER(10)     NULL,
    maximum_usage          NUMBER(10)     NULL,
    minimum_amount_in_cart FLOAT(53)      NULL,
    percentage             NUMBER(10)     NULL,
    start_date             TIMESTAMP(6)   NULL,
    status                 NUMBER(10)     DEFAULT 0 NOT NULL,
    type                   NUMBER(10)     DEFAULT 0 NOT NULL,
    CONSTRAINT PK_discount_code_backup PRIMARY KEY (id)
) TABLESPACE TS_TRASUA;

CREATE TABLE bill (
    id                 NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount             FLOAT(53)      NULL,
    billing_address    NVARCHAR2(255) NULL,
    code               VARCHAR2(50)   NOT NULL,
    create_date        TIMESTAMP(6)   NULL,
    invoice_type       VARCHAR2(255)  NULL,
    promotion_price    FLOAT(53)      DEFAULT 0 NOT NULL,
    return_status      NUMBER(1)      NULL,
    status             VARCHAR2(255)  NULL,
    update_date        TIMESTAMP(6)   NULL,
    customer_id        NUMBER(19)     NULL,
    discount_code_id   NUMBER(19)     NULL,
    payment_method_id  NUMBER(19)     NULL,
    branch_id          NUMBER(19)     NULL,
    cashier_account_id NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE brand (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    status      NUMBER(10)     DEFAULT 0 NOT NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE category (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    status      NUMBER(10)     DEFAULT 0 NOT NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE material (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    status      NUMBER(10)     DEFAULT 0 NOT NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE color (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE size_product (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE product (
    id           NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code         VARCHAR2(255)  NULL,
    create_date  TIMESTAMP(6)   NULL,
    delete_flag  NUMBER(1)      DEFAULT 0 NOT NULL,
    describe     NVARCHAR2(255) NULL,
    gender       NUMBER(10)     DEFAULT 0 NOT NULL,
    name         NVARCHAR2(255) NULL,
    price        FLOAT(53)      DEFAULT 0 NOT NULL,
    status       NUMBER(10)     DEFAULT 0 NOT NULL,
    updated_date TIMESTAMP(6)   NULL,
    brand_id     NUMBER(19)     NULL,
    category_id  NUMBER(19)     NULL,
    material_id  NUMBER(19)     NULL,
    color_id     NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE product_detail (
    id         NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    barcode    VARCHAR2(255) NULL,
    price      FLOAT(53)     DEFAULT 0 NOT NULL,
    quantity   NUMBER(10)    DEFAULT 0 NOT NULL,
    color_id   NUMBER(19)    NULL,
    product_id NUMBER(19)    NULL,
    size_id    NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE image (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    create_date TIMESTAMP(6)   NULL,
    file_type   VARCHAR2(255)  NULL,
    link        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    update_date TIMESTAMP(6)   NULL,
    product_id  NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE topping (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description NVARCHAR2(255) NULL,
    name        NVARCHAR2(255) NULL,
    price       NUMBER(19,2)   DEFAULT 0 NOT NULL,
    status      NUMBER(1)      DEFAULT 0 NOT NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE bill_detail (
    id                NUMBER(19)  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    moment_price      FLOAT(53)   NULL,
    quantity          NUMBER(10)  NULL,
    return_quantity   NUMBER(10)  NULL,
    bill_id           NUMBER(19)  NULL,
    product_detail_id NUMBER(19)  NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE bill_detail_topping (
    id             NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topping_name   VARCHAR2(255) NULL,
    topping_price  FLOAT(53)     NULL,
    bill_detail_id NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE bill_return (
    id                   NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code                 VARCHAR2(255)  NULL,
    isCancel             NUMBER(1)      DEFAULT 0 NOT NULL,
    percentFeeExchange   NUMBER(10)     NULL,
    returnDate           TIMESTAMP(6)   NULL,
    returnMoney          FLOAT(53)      NULL,
    returnReason         NVARCHAR2(255) NULL,
    returnStatus         NUMBER(10)     DEFAULT 0 NOT NULL,
    bill_id              NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE branch_inventory (
    id                NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    branch_id         NUMBER(19)    NOT NULL,
    product_detail_id NUMBER(19)    NOT NULL,
    quantity          NUMBER(10)    DEFAULT 0 NULL,
    createDate        TIMESTAMP(6)  NULL,
    isActive          NUMBER(1)     DEFAULT 0 NOT NULL,
    maxQuantity       NUMBER(10)    NULL,
    minQuantity       NUMBER(10)    NULL,
    updateDate        TIMESTAMP(6)  NULL,
    CONSTRAINT UQ_branch_inventory UNIQUE (branch_id, product_detail_id)
) TABLESPACE TS_TRASUA;

CREATE TABLE cart (
    id                NUMBER(19)   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    create_date       TIMESTAMP(6) NULL,
    quantity          NUMBER(10)   DEFAULT 0 NOT NULL,
    update_date       TIMESTAMP(6) NULL,
    account_id        NUMBER(19)   NULL,
    product_detail_id NUMBER(19)   NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE chat_message (
    id          NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    content     CLOB          NULL,
    create_date TIMESTAMP(6)  NULL,
    created_at  TIMESTAMP(6)  NULL,
    receiver_id NUMBER(19)    NULL,
    seen        NUMBER(1)     DEFAULT 0 NOT NULL,
    sender_id   NUMBER(19)    NULL,
    room_id     VARCHAR2(255) NULL,
    sender      VARCHAR2(255) NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE payment (
    id              NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount          VARCHAR2(255) NULL,
    orderId         VARCHAR2(255) NULL,
    orderStatus     VARCHAR2(255) NULL,
    paymentDate     TIMESTAMP(6)  NULL,
    statusExchange  NUMBER(10)    NULL,
    bill_id         NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE product_discount (
    id                 NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    closed             NUMBER(1)     DEFAULT 0 NOT NULL,
    discountedAmount   FLOAT(53)     NULL,
    endDate            TIMESTAMP(6)  NULL,
    startDate          TIMESTAMP(6)  NULL,
    product_detail_id  NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE return_detail (
    id                  NUMBER(19)  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    momentPriceRefund   FLOAT(53)   NULL,
    quantityReturn      NUMBER(10)  NULL,
    return_id           NUMBER(19)  NULL,
    product_detail_id   NUMBER(19)  NULL
) TABLESPACE TS_TRASUA;

CREATE TABLE verification_code (
    id          NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(6)   NOT NULL,
    expiry_time TIMESTAMP(6)  NOT NULL,
    account_id  NUMBER(19)    NOT NULL
) TABLESPACE TS_TRASUA;


-- ============================================================
-- KHỞI TẠO CHỈ MỤC TỐI ƯU TRUY VẤN (INDEXES)
-- ============================================================
CREATE INDEX idx_account_customer ON account (customer_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_account_role ON account (role_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_account_branch ON account (branch_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_branch_inventory_branch ON branch_inventory (branch_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_branch_inventory_product ON branch_inventory (product_detail_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_bill_customer ON bill (customer_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_bill_detail_bill ON bill_detail (bill_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_product_detail_product ON product_detail (product_id) TABLESPACE TS_TRASUA;
CREATE INDEX idx_verification_account ON verification_code (account_id) TABLESPACE TS_TRASUA;


-- ============================================================
-- THIẾT LẬP RÀNG BUỘC TOÀN VẸN KHÓA NGOẠI (FOREIGN KEYS)
-- ============================================================
ALTER TABLE account ADD CONSTRAINT fk_account_customer FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE SET NULL;
ALTER TABLE account ADD CONSTRAINT fk_account_role FOREIGN KEY (role_id) REFERENCES role (id);
ALTER TABLE account ADD CONSTRAINT fk_account_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE SET NULL;
ALTER TABLE address_shipping ADD CONSTRAINT fk_addr_shipping_customer FOREIGN KEY (customer_id) REFERENCES customer (id);
ALTER TABLE bill ADD CONSTRAINT fk_bill_branch FOREIGN KEY (branch_id) REFERENCES branch (id);
ALTER TABLE bill ADD CONSTRAINT fk_bill_customer FOREIGN KEY (customer_id) REFERENCES customer (id);
ALTER TABLE bill ADD CONSTRAINT fk_bill_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_method (id);
ALTER TABLE bill ADD CONSTRAINT fk_bill_discount_code FOREIGN KEY (discount_code_id) REFERENCES discount_code (id);
ALTER TABLE bill_detail ADD CONSTRAINT fk_bill_detail_product_detail FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);
ALTER TABLE bill_detail ADD CONSTRAINT fk_bill_detail_bill FOREIGN KEY (bill_id) REFERENCES bill (id);
ALTER TABLE bill_detail_topping ADD CONSTRAINT fk_bdt_bill_detail FOREIGN KEY (bill_detail_id) REFERENCES bill_detail (id);
ALTER TABLE bill_return ADD CONSTRAINT fk_bill_return_bill FOREIGN KEY (bill_id) REFERENCES bill (id);
ALTER TABLE branch_inventory ADD CONSTRAINT fk_branch_inv_branch FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE CASCADE;
ALTER TABLE branch_inventory ADD CONSTRAINT fk_branch_inv_product FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);
ALTER TABLE cart ADD CONSTRAINT fk_cart_account FOREIGN KEY (account_id) REFERENCES account (id);
ALTER TABLE cart ADD CONSTRAINT fk_cart_product_detail FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);
ALTER TABLE image ADD CONSTRAINT fk_image_product FOREIGN KEY (product_id) REFERENCES product (id);
ALTER TABLE payment ADD CONSTRAINT fk_payment_bill FOREIGN KEY (bill_id) REFERENCES bill (id);
ALTER TABLE product ADD CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand (id);
ALTER TABLE product ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id);
ALTER TABLE product ADD CONSTRAINT fk_product_material FOREIGN KEY (material_id) REFERENCES material (id);
ALTER TABLE product ADD CONSTRAINT fk_product_color FOREIGN KEY (color_id) REFERENCES color (id);
ALTER TABLE product_detail ADD CONSTRAINT fk_pd_product FOREIGN KEY (product_id) REFERENCES product (id);
ALTER TABLE product_detail ADD CONSTRAINT fk_pd_color FOREIGN KEY (color_id) REFERENCES color (id);
ALTER TABLE product_detail ADD CONSTRAINT fk_pd_size FOREIGN KEY (size_id) REFERENCES size_product (id);
ALTER TABLE product_discount ADD CONSTRAINT fk_pdiscount_product_detail FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);
ALTER TABLE return_detail ADD CONSTRAINT fk_return_detail_bill_return FOREIGN KEY (return_id) REFERENCES bill_return (id);
ALTER TABLE return_detail ADD CONSTRAINT fk_return_detail_product FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);
ALTER TABLE verification_code ADD CONSTRAINT fk_verification_account FOREIGN KEY (account_id) REFERENCES account (id);

COMMIT;



-- ==============================================================================
-- PHẦN 3: NẠP DỮ LIỆU MẪU BAN ĐẦU (SEED DATA)
-- (CHẠY BẰNG USER: TRASUA)
-- ==============================================================================

-- 1. PHÂN QUYỀN HỆ THỐNG (ROLES)
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_STAFF');
INSERT INTO role (name) VALUES ('ROLE_VENDOR');
INSERT INTO role (name) VALUES ('ROLE_USER');
INSERT INTO role (name) VALUES ('ROLE_GUEST');

-- 2. PHƯƠNG THỨC THANH TOÁN (PAYMENT METHODS)
INSERT INTO payment_method (id, name, status) VALUES (1, 'TIEN_MAT', 1);
INSERT INTO payment_method (id, name, status) VALUES (2, 'CHUYEN_KHOAN', 1);
INSERT INTO payment_method (id, name, status) VALUES (3, 'THE', 1);

-- 3. CHI NHÁNH CỬA HÀNG (BRANCH)
INSERT INTO branch (branch_name, address, phone, email, is_active, create_date, update_date, branch_code)
VALUES (N'Chi nhánh Trà Sữa Hà Nội', N'123 Phố Huế, Hai Bà Trưng, Hà Nội', '0901234567', 'hanoi@trasua.vn', 1, SYSDATE, SYSDATE, 'CN001');
INSERT INTO branch (branch_name, address, phone, email, is_active, create_date, update_date, branch_code)
VALUES (N'Chi nhánh Trà Sữa HCM', N'456 Nguyễn Trãi, Quận 5, TP.HCM', '0902345678', 'hcm@trasua.vn', 1, SYSDATE, SYSDATE, 'CN002');

-- 4. THÔNG TIN KHÁCH HÀNG (CUSTOMER)
INSERT INTO customer (code, name, email, phone_number) VALUES ('KH0001', N'Quản Trị Viên', 'admin@trasua.vn', '0900000000');
INSERT INTO customer (code, name, email, phone_number) VALUES ('KH0002', N'Nguyễn Văn An', 'nguyenvanan@gmail.com', '0911111111');
INSERT INTO customer (code, name, email, phone_number) VALUES ('KH0003', N'Trần Thị Bình', 'tranthib@gmail.com', '0922222222');
INSERT INTO customer (code, name, email, phone_number) VALUES ('KH0004', N'Lê Văn Cường', 'levanc@gmail.com', '0933333333');

-- 5. TÀI KHOẢN ĐĂNG NHẬP (ACCOUNT) - Mật khẩu giải mã BCrypt đều là: 'Admin@123'
INSERT INTO account (code, email, password, is_non_locked, create_date, update_date, customer_id, role_id, branch_id)
VALUES ('AC0001', 'admin@trasua.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, SYSDATE, SYSDATE,
        (SELECT id FROM customer WHERE code = 'KH0001' AND ROWNUM = 1), (SELECT id FROM role WHERE name = 'ROLE_ADMIN' AND ROWNUM = 1), NULL);

INSERT INTO account (code, email, password, is_non_locked, create_date, update_date, customer_id, role_id, branch_id)
VALUES ('AC0002', 'staff@trasua.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, SYSDATE, SYSDATE,
        (SELECT id FROM customer WHERE code = 'KH0002' AND ROWNUM = 1), (SELECT id FROM role WHERE name = 'ROLE_STAFF' AND ROWNUM = 1), (SELECT id FROM branch WHERE branch_code = 'CN001' AND ROWNUM = 1));

INSERT INTO account (code, email, password, is_non_locked, create_date, update_date, customer_id, role_id, branch_id)
VALUES ('AC0003', 'user@trasua.vn', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 1, SYSDATE, SYSDATE,
        (SELECT id FROM customer WHERE code = 'KH0003' AND ROWNUM = 1), (SELECT id FROM role WHERE name = 'ROLE_USER' AND ROWNUM = 1), NULL);

-- 6. DANH MỤC SẢN PHẨM (CATEGORY)
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM001', N'Trà Sữa', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM002', N'Trà Trái Cây', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM003', N'Cà Phê', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM004', N'Trà Xanh', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM005', N'Sinh Tố', 1, 0);

-- 7. THƯƠNG HIỆU (BRAND)
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH001', N'Gong Cha', 1, 0);
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH002', N'The Alley', 1, 0);
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH003', N'Phúc Long', 1, 0);
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH004', N'Highlands Coffee', 1, 0);

-- 8. NGUYÊN LIỆU GỐC (MATERIAL)
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL001', N'Trà Đen', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL002', N'Trà Xanh Matcha', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL003', N'Trà Ô Long', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL004', N'Cà Phê Arabica', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL005', N'Trà Hoa Quả', 1, 0);

-- 9. PHÂN LOẠI PHỤC VỤ (COLOR - NÓNG/LẠNH/ĐÁ XAY)
INSERT INTO color (code, name, delete_flag) VALUES ('MK001', N'Nóng', 0);
INSERT INTO color (code, name, delete_flag) VALUES ('MK002', N'Lạnh', 0);
INSERT INTO color (code, name, delete_flag) VALUES ('MK003', N'Đá Xay', 0);

-- 10. KÍCH CỠ LY SẢN PHẨM (SIZE_PRODUCT)
INSERT INTO size_product (code, name, delete_flag) VALUES ('KH001', 'S (Nhỏ - 300ml)', 0);
INSERT INTO size_product (code, name, delete_flag) VALUES ('KH002', 'M (Vừa - 500ml)', 0);
INSERT INTO size_product (code, name, delete_flag) VALUES ('KH003', 'L (Lớn - 700ml)', 0);

-- 11. CÁC LOẠI ĐỒ ĂN KÈM (TOPPING)
INSERT INTO topping (name, description, price, status) VALUES (N'Trân Châu Đen', N'Trân châu đen dai, ngọt', 5000, 1);
INSERT INTO topping (name, description, price, status) VALUES (N'Trân Châu Trắng', N'Trân châu trắng mềm', 5000, 1);
INSERT INTO topping (name, description, price, status) VALUES (N'Pudding', N'Pudding trứng mềm mịn', 8000, 1);
INSERT INTO topping (name, description, price, status) VALUES (N'Thạch Dừa', N'Thạch dừa giòn ngon', 5000, 1);
INSERT INTO topping (name, description, price, status) VALUES (N'Kem Cheese', N'Kem phô mai béo ngậy', 10000, 1);
INSERT INTO topping (name, description, price, status) VALUES (N'Hạt Ngũ Cốc', N'Hạt ngũ cốc giòn', 7000, 1);

-- 12. DANH SÁCH SẢN PHẨM GỐC (PRODUCT)
INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date, brand_id, category_id, material_id, color_id)
VALUES ('SP0001', N'Trà Sữa Truyền Thống', SYSDATE, 0, N'Trà sữa truyền thống thơm ngon, béo ngậy', 0, 35000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH001' AND ROWNUM = 1), (SELECT id FROM category WHERE code = 'DM001' AND ROWNUM = 1), (SELECT id FROM material WHERE code = 'NL001' AND ROWNUM = 1), (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date, brand_id, category_id, material_id, color_id)
VALUES ('SP0002', N'Matcha Sữa', SYSDATE, 0, N'Matcha Nhật Bản nguyên chất pha cùng sữa tươi', 0, 45000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH001' AND ROWNUM = 1), (SELECT id FROM category WHERE code = 'DM001' AND ROWNUM = 1), (SELECT id FROM material WHERE code = 'NL002' AND ROWNUM = 1), (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date, brand_id, category_id, material_id, color_id)
VALUES ('SP0003', N'Trà Xanh Đào', SYSDATE, 0, N'Trà xanh kết hợp vị đào tươi mát', 0, 40000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH002' AND ROWNUM = 1), (SELECT id FROM category WHERE code = 'DM004' AND ROWNUM = 1), (SELECT id FROM material WHERE code = 'NL002' AND ROWNUM = 1), (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date, brand_id, category_id, material_id, color_id)
VALUES ('SP0004', N'Cà Phê Sữa Đá', SYSDATE, 0, N'Cà phê Arabica đậm đà pha cùng sữa đặc', 0, 38000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH004' AND ROWNUM = 1), (SELECT id FROM category WHERE code = 'DM003' AND ROWNUM = 1), (SELECT id FROM material WHERE code = 'NL004' AND ROWNUM = 1), (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

-- 13. BIẾN THỂ CHI TIẾT SẢN PHẨM (PRODUCT_DETAIL - KẾT HỢP SIZE X PHỤC VỤ)
-- Biến thể Trà sữa truyền thống (Size S, M, L)
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0001-LANH-S', 35000, 100, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0001'), (SELECT id FROM size_product WHERE code = 'KH001'));
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0001-LANH-M', 40000, 100, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0001'), (SELECT id FROM size_product WHERE code = 'KH002'));
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0001-LANH-L', 45000, 100, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0001'), (SELECT id FROM size_product WHERE code = 'KH003'));

-- Biến thể Matcha Sữa
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0002-LANH-S', 45000, 80, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0002'), (SELECT id FROM size_product WHERE code = 'KH001'));
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0002-LANH-M', 50000, 80, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0002'), (SELECT id FROM size_product WHERE code = 'KH002'));
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0002-LANH-L', 55000, 80, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0002'), (SELECT id FROM size_product WHERE code = 'KH003'));

-- Biến thể Trà Xanh Đào
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0003-LANH-M', 40000, 120, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0003'), (SELECT id FROM size_product WHERE code = 'KH002'));
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0003-LANH-L', 45000, 120, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0003'), (SELECT id FROM size_product WHERE code = 'KH003'));

-- Biến thể Cà Phê Sữa Đá (Lạnh / Nóng)
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0004-LANH-M', 38000, 200, (SELECT id FROM color WHERE code = 'MK002'), (SELECT id FROM product WHERE code = 'SP0004'), (SELECT id FROM size_product WHERE code = 'KH002'));
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0004-NONG-M', 38000, 150, (SELECT id FROM color WHERE code = 'MK001'), (SELECT id FROM product WHERE code = 'SP0004'), (SELECT id FROM size_product WHERE code = 'KH002'));


-- 14. MÃ KHUYẾN MÃI GIẢM GIÁ (DISCOUNT_CODE)
INSERT INTO discount_code (code, detail, type, percentage, discount_amount, minimum_amount_in_cart, maximum_amount, maximum_usage, start_date, end_date, status, delete_flag)
VALUES ('WELCOME10', N'Giảm 10% cho đơn hàng đầu tiên', 1, 10, NULL, 100000, 50000, 100, SYSDATE - 1, SYSDATE + 30, 1, 0);

INSERT INTO discount_code (code, detail, type, percentage, discount_amount, minimum_amount_in_cart, maximum_amount, maximum_usage, start_date, end_date, status, delete_flag)
VALUES ('SALE20K', N'Giảm 20.000đ cho đơn từ 150.000đ', 0, NULL, 20000, 150000, NULL, 50, SYSDATE - 1, SYSDATE + 15, 1, 0);


-- 15. ĐỊA CHỈ GIAO HÀNG MẪU (ADDRESS_SHIPPING)
INSERT INTO address_shipping (address, customer_id) VALUES (N'123 Phố Huế, Hai Bà Trưng, Hà Nội', (SELECT id FROM customer WHERE code = 'KH0002'));
INSERT INTO address_shipping (address, customer_id) VALUES (N'456 Nguyễn Trãi, Quận 5, TP.HCM', (SELECT id FROM customer WHERE code = 'KH0003'));


-- 16. PHÂN BỔ KHO HÀNG CHI NHÁNH MẪU (BRANCH_INVENTORY)
-- (Đã fix lỗi cú pháp CamelCase không dùng ngoặc kép để chạy an toàn trên Oracle PDB)
INSERT INTO branch_inventory (branch_id, product_detail_id, quantity, isActive, createDate, updateDate, minQuantity)
SELECT
    (SELECT MIN(id) FROM branch)  AS branch_id,
    pd.id                         AS product_detail_id,
    100                           AS quantity,
    1                             AS isActive,
    SYSTIMESTAMP                  AS createDate,
    SYSTIMESTAMP                  AS updateDate,
    0                             AS minQuantity
FROM product_detail pd;

COMMIT;