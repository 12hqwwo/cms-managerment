-- ============================================================
-- PHAN 3: DU LIEU KHOI TAO (SEED DATA)
-- ============================================================

-- ============================================================
-- XÓA DỮ LIỆU CŨ (Tránh lỗi duplicate và subquery trả về nhiều dòng)
-- Xóa theo thứ tự con -> cha để không dính Foreign Key
-- ============================================================
DELETE FROM address_shipping;
DELETE FROM account;
DELETE FROM product_detail;
DELETE FROM product;
DELETE FROM topping;
DELETE FROM size_product;
DELETE FROM color;
DELETE FROM material;
DELETE FROM brand;
DELETE FROM category;
DELETE FROM customer;
DELETE FROM branch;
DELETE FROM role;
DELETE FROM payment_method;
DELETE FROM discount_code;
COMMIT;

-- ============================================================
-- 1. ROLES
-- ============================================================
INSERT INTO role (name) VALUES ('ROLE_ADMIN');
INSERT INTO role (name) VALUES ('ROLE_STAFF');
INSERT INTO role (name) VALUES ('ROLE_VENDOR');
INSERT INTO role (name) VALUES ('ROLE_USER');
INSERT INTO role (name) VALUES ('ROLE_GUEST');

-- ============================================================
-- 2. PAYMENT METHODS (id dat thu cong vi Entity khong co @GeneratedValue)
-- ============================================================
INSERT INTO payment_method (id, name, status) VALUES (1, 'TIEN_MAT', 1);
INSERT INTO payment_method (id, name, status) VALUES (2, 'CHUYEN_KHOAN', 1);
INSERT INTO payment_method (id, name, status) VALUES (3, 'THE', 1);

-- ============================================================
-- 3. BRANCH (Chi nhanh)
-- ============================================================
INSERT INTO branch (branch_name, address, phone, email, is_active, create_date, update_date, branch_code)
VALUES (N'Chi nhánh Trà Sữa Hà Nội', N'123 Phố Huế, Hai Bà Trưng, Hà Nội',
        '0901234567', 'hanoi@trasua.vn', 1, SYSDATE, SYSDATE, 'CN001');

INSERT INTO branch (branch_name, address, phone, email, is_active, create_date, update_date, branch_code)
VALUES (N'Chi nhánh Trà Sữa HCM', N'456 Nguyễn Trãi, Quận 5, TP.HCM',
        '0902345678', 'hcm@trasua.vn', 1, SYSDATE, SYSDATE, 'CN002');

-- ============================================================
-- 4. CUSTOMER (Khach hang cho ADMIN)
-- ============================================================
INSERT INTO customer (code, name, email, phone_number)
VALUES ('KH0001', N'Quản Trị Viên', 'admin@trasua.vn', '0900000000');

INSERT INTO customer (code, name, email, phone_number)
VALUES ('KH0002', N'Nguyễn Văn An', 'nguyenvanan@gmail.com', '0911111111');

INSERT INTO customer (code, name, email, phone_number)
VALUES ('KH0003', N'Trần Thị Bình', 'tranthib@gmail.com', '0922222222');

INSERT INTO customer (code, name, email, phone_number)
VALUES ('KH0004', N'Lê Văn Cường', 'levanc@gmail.com', '0933333333');

-- ============================================================
-- 5. ACCOUNT
-- Password: 'Admin@123' -> BCrypt hash
-- Su dung hash nay de dang nhap bang mat khau 'Admin@123'
-- Them ROWNUM = 1 de dam bao khong bao loi neu ban insert nhieu lan
-- ============================================================

-- ADMIN account
INSERT INTO account (code, email, password, is_non_locked, create_date, update_date, customer_id, role_id, branch_id)
VALUES ('AC0001', 'admin@trasua.vn',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        1, SYSDATE, SYSDATE,
        (SELECT id FROM customer WHERE code = 'KH0001' AND ROWNUM = 1),
        (SELECT id FROM role WHERE name = 'ROLE_ADMIN' AND ROWNUM = 1),
        NULL);

-- STAFF account (Chi nhanh HN)
INSERT INTO account (code, email, password, is_non_locked, create_date, update_date, customer_id, role_id, branch_id)
VALUES ('AC0002', 'staff@trasua.vn',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        1, SYSDATE, SYSDATE,
        (SELECT id FROM customer WHERE code = 'KH0002' AND ROWNUM = 1),
        (SELECT id FROM role WHERE name = 'ROLE_STAFF' AND ROWNUM = 1),
        (SELECT id FROM branch WHERE branch_code = 'CN001' AND ROWNUM = 1));

-- USER account
INSERT INTO account (code, email, password, is_non_locked, create_date, update_date, customer_id, role_id, branch_id)
VALUES ('AC0003', 'user@trasua.vn',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH',
        1, SYSDATE, SYSDATE,
        (SELECT id FROM customer WHERE code = 'KH0003' AND ROWNUM = 1),
        (SELECT id FROM role WHERE name = 'ROLE_USER' AND ROWNUM = 1),
        NULL);

-- ============================================================
-- 6. CATEGORY (Danh muc)
-- ============================================================
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM001', N'Trà Sữa', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM002', N'Trà Trái Cây', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM003', N'Cà Phê', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM004', N'Trà Xanh', 1, 0);
INSERT INTO category (code, name, status, delete_flag) VALUES ('DM005', N'Sinh Tố', 1, 0);

-- ============================================================
-- 7. BRAND (Thuong hieu)
-- ============================================================
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH001', N'Gong Cha', 1, 0);
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH002', N'The Alley', 1, 0);
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH003', N'Phúc Long', 1, 0);
INSERT INTO brand (code, name, status, delete_flag) VALUES ('TH004', N'Highlands Coffee', 1, 0);

-- ============================================================
-- 8. MATERIAL (Chat lieu / Nguyen lieu)
-- ============================================================
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL001', N'Trà Đen', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL002', N'Trà Xanh Matcha', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL003', N'Trà Ô Long', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL004', N'Cà Phê Arabica', 1, 0);
INSERT INTO material (code, name, status, delete_flag) VALUES ('NL005', N'Trà Hoa Quả', 1, 0);

-- ============================================================
-- 9. COLOR (Dung cho cup/ly: mau sac)
-- ============================================================
INSERT INTO color (code, name, delete_flag) VALUES ('MK001', N'Nóng', 0);
INSERT INTO color (code, name, delete_flag) VALUES ('MK002', N'Lạnh', 0);
INSERT INTO color (code, name, delete_flag) VALUES ('MK003', N'Đá Xay', 0);

-- ============================================================
-- 10. SIZE_PRODUCT (Kich co: S/M/L)
-- ============================================================
INSERT INTO size_product (code, name, delete_flag) VALUES ('KH001', 'S (Nhỏ - 300ml)', 0);
INSERT INTO size_product (code, name, delete_flag) VALUES ('KH002', 'M (Vừa - 500ml)', 0);
INSERT INTO size_product (code, name, delete_flag) VALUES ('KH003', 'L (Lớn - 700ml)', 0);

-- ============================================================
-- 11. TOPPING
-- ============================================================
INSERT INTO topping (name, description, price, status)
VALUES (N'Trân Châu Đen', N'Trân châu đen dai, ngọt', 5000, 1);

INSERT INTO topping (name, description, price, status)
VALUES (N'Trân Châu Trắng', N'Trân châu trắng mềm', 5000, 1);

INSERT INTO topping (name, description, price, status)
VALUES (N'Pudding', N'Pudding trứng mềm mịn', 8000, 1);

INSERT INTO topping (name, description, price, status)
VALUES (N'Thạch Dừa', N'Thạch dừa giòn ngon', 5000, 1);

INSERT INTO topping (name, description, price, status)
VALUES (N'Kem Cheese', N'Kem phô mai béo ngậy', 10000, 1);

INSERT INTO topping (name, description, price, status)
VALUES (N'Hạt Ngũ Cốc', N'Hạt ngũ cốc giòn', 7000, 1);

-- ============================================================
-- 12. PRODUCT (San pham)
-- ============================================================

-- SP001: Tra Sua Truyen Thong
INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date,
                     brand_id, category_id, material_id, color_id)
VALUES ('SP0001', N'Trà Sữa Truyền Thống', SYSDATE, 0,
        N'Trà sữa truyền thống thơm ngon, béo ngậy với trà đen hảo hạng',
        0, 35000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH001' AND ROWNUM = 1),
        (SELECT id FROM category WHERE code = 'DM001' AND ROWNUM = 1),
        (SELECT id FROM material WHERE code = 'NL001' AND ROWNUM = 1),
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

-- SP002: Matcha Sua
INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date,
                     brand_id, category_id, material_id, color_id)
VALUES ('SP0002', N'Matcha Sữa', SYSDATE, 0,
        N'Matcha Nhật Bản nguyên chất pha cùng sữa tươi đặc biệt',
        0, 45000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH001' AND ROWNUM = 1),
        (SELECT id FROM category WHERE code = 'DM001' AND ROWNUM = 1),
        (SELECT id FROM material WHERE code = 'NL002' AND ROWNUM = 1),
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

-- SP003: Tra Xanh Dao
INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date,
                     brand_id, category_id, material_id, color_id)
VALUES ('SP0003', N'Trà Xanh Đào', SYSDATE, 0,
        N'Trà xanh kết hợp vị đào tươi mát, thanh nhiệt giải khát',
        0, 40000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH002' AND ROWNUM = 1),
        (SELECT id FROM category WHERE code = 'DM004' AND ROWNUM = 1),
        (SELECT id FROM material WHERE code = 'NL002' AND ROWNUM = 1),
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

-- SP004: Ca Phe Sua Da
INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date,
                     brand_id, category_id, material_id, color_id)
VALUES ('SP0004', N'Cà Phê Sữa Đá', SYSDATE, 0,
        N'Cà phê Arabica đậm đà pha cùng sữa đặc Việt Nam',
        0, 38000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH004' AND ROWNUM = 1),
        (SELECT id FROM category WHERE code = 'DM003' AND ROWNUM = 1),
        (SELECT id FROM material WHERE code = 'NL004' AND ROWNUM = 1),
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1));

-- SP005: Tra O Long Sua Tuoi
INSERT INTO product (code, name, create_date, delete_flag, describe, gender, price, status, updated_date,
                     brand_id, category_id, material_id, color_id)
VALUES ('SP0005', N'Trà Ô Long Sữa Tươi', SYSDATE, 0,
        N'Trà Ô Long thượng hạng kết hợp sữa tươi nguyên kem',
        0, 50000, 1, SYSDATE,
        (SELECT id FROM brand WHERE code = 'TH002' AND ROWNUM = 1),
        (SELECT id FROM category WHERE code = 'DM001' AND ROWNUM = 1),
        (SELECT id FROM material WHERE code = 'NL003' AND ROWNUM = 1),
        (SELECT id FROM color WHERE code = 'MK001' AND ROWNUM = 1));

-- ============================================================
-- 13. PRODUCT_DETAIL (Chi tiet san pham: S/M/L x Nong/Lanh)
-- ============================================================

-- SP0001 - Tra Sua Truyen Thong
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0001-LANH-S', 35000, 100,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0001' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH001' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0001-LANH-M', 40000, 100,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0001' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH002' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0001-LANH-L', 45000, 100,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0001' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH003' AND ROWNUM = 1));

-- SP0002 - Matcha Sua
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0002-LANH-S', 45000, 80,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0002' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH001' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0002-LANH-M', 50000, 80,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0002' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH002' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0002-LANH-L', 55000, 80,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0002' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH003' AND ROWNUM = 1));

-- SP0003 - Tra Xanh Dao
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0003-LANH-M', 40000, 120,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0003' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH002' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0003-LANH-L', 45000, 120,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0003' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH003' AND ROWNUM = 1));

-- SP0004 - Ca Phe Sua Da
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0004-LANH-M', 38000, 200,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0004' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH002' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0004-NONG-M', 38000, 150,
        (SELECT id FROM color WHERE code = 'MK001' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0004' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH002' AND ROWNUM = 1));

-- SP0005 - Tra O Long
INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0005-NONG-M', 50000, 60,
        (SELECT id FROM color WHERE code = 'MK001' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0005' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH002' AND ROWNUM = 1));

INSERT INTO product_detail (barcode, price, quantity, color_id, product_id, size_id)
VALUES ('SP0005-LANH-L', 55000, 60,
        (SELECT id FROM color WHERE code = 'MK002' AND ROWNUM = 1),
        (SELECT id FROM product WHERE code = 'SP0005' AND ROWNUM = 1),
        (SELECT id FROM size_product WHERE code = 'KH003' AND ROWNUM = 1));

-- ============================================================
-- 14. DISCOUNT_CODE (Ma giam gia)
-- ============================================================
INSERT INTO discount_code (code, detail, type, percentage, discount_amount,
                            minimum_amount_in_cart, maximum_amount, maximum_usage,
                            start_date, end_date, status, delete_flag)
VALUES ('WELCOME10', N'Giảm 10% cho đơn hàng đầu tiên', 1, 10, NULL,
        100000, 50000, 100,
        SYSDATE - 1, SYSDATE + 30, 1, 0);

INSERT INTO discount_code (code, detail, type, percentage, discount_amount,
                            minimum_amount_in_cart, maximum_amount, maximum_usage,
                            start_date, end_date, status, delete_flag)
VALUES ('SALE20K', N'Giảm 20.000đ cho đơn từ 150.000đ', 0, NULL, 20000,
        150000, NULL, 50,
        SYSDATE - 1, SYSDATE + 15, 1, 0);

INSERT INTO discount_code (code, detail, type, percentage, discount_amount,
                            minimum_amount_in_cart, maximum_amount, maximum_usage,
                            start_date, end_date, status, delete_flag)
VALUES ('VIP15', N'Giảm 15% cho khách VIP', 1, 15, NULL,
        200000, 100000, 200,
        SYSDATE - 1, SYSDATE + 60, 1, 0);

-- ============================================================
-- 15. ADDRESS_SHIPPING (Dia chi giao hang)
-- ============================================================
INSERT INTO address_shipping (address, customer_id)
VALUES (N'123 Phố Huế, Hai Bà Trưng, Hà Nội',
        (SELECT id FROM customer WHERE code = 'KH0002' AND ROWNUM = 1));

INSERT INTO address_shipping (address, customer_id)
VALUES (N'456 Nguyễn Trãi, Quận 5, TP.HCM',
        (SELECT id FROM customer WHERE code = 'KH0003' AND ROWNUM = 1));

-- ============================================================
-- COMMIT
-- ============================================================
-- ============================================================
-- 15. BRANCH INVENTORY (Kho hàng)
-- ============================================================
-- Gán toàn bộ sản phẩm vào Chi nhánh 1 (Hà Nội) với số lượng 100
INSERT INTO branch_inventory (branch_id, product_detail_id, quantity, "isActive", "createDate", "updateDate", "minQuantity")
SELECT 
    (SELECT MIN(id) FROM branch),
    id, 
    100, 
    1, 
    SYSDATE, 
    SYSDATE,
    0
FROM product_detail;

COMMIT;
