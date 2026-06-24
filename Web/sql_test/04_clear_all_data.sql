-- ============================================================
-- SCRIPT XÓA TOÀN BỘ DỮ LIỆU ĐỂ LÀM SẠCH DATABASE
-- Chạy script này để xóa sạch rác / dữ liệu bị trùng lặp.
-- LƯU Ý: Chạy XONG script này thì chạy LẠI file 03_seed_data_trasua.sql ĐÚNG 1 LẦN.
-- ============================================================

DELETE FROM verification_code;
DELETE FROM return_detail;
DELETE FROM product_discount;
DELETE FROM payment;
DELETE FROM cart;
DELETE FROM branch_inventory;
DELETE FROM bill_return;
DELETE FROM bill_detail_topping;
DELETE FROM bill_detail;
DELETE FROM topping;
DELETE FROM image;
DELETE FROM product_detail;
DELETE FROM product;
DELETE FROM size_product;
DELETE FROM color;
DELETE FROM material;
DELETE FROM category;
DELETE FROM brand;
DELETE FROM bill;
DELETE FROM discount_code;
DELETE FROM payment_method;
DELETE FROM address_shipping;
DELETE FROM account;
DELETE FROM branch;
DELETE FROM customer;
DELETE FROM role;

COMMIT;

-- Sau khi chạy xong file này, toàn bộ database của bạn đã trống trơn.
-- Hãy mở file 03_seed_data_trasua.sql và chạy lại nó ĐÚNG 1 LẦN DUY NHẤT để nạp lại dữ liệu chuẩn.
