-- Script bổ sung tính năng quản lý Tọa độ & Bán kính giao hàng
-- Tác giả: Antigravity

-- 1. Thêm cột latitude (Vĩ độ)
ALTER TABLE TRASUA.BRANCH ADD latitude NUMBER(10,8) NULL;

-- 2. Thêm cột longitude (Kinh độ)
ALTER TABLE TRASUA.BRANCH ADD longitude NUMBER(11,8) NULL;

-- 3. Thêm cột delivery_radius (Bán kính giao hàng bằng km)
ALTER TABLE TRASUA.BRANCH ADD delivery_radius NUMBER(5,2) DEFAULT 5.0 NULL;

-- Chú ý: Hiện tại các cột này cho phép NULL để tương thích với dữ liệu cũ.
-- Bạn có thể cập nhật các chi nhánh cũ từ giao diện quản trị.
-- Sau khi hoàn tất cập nhật 100% dữ liệu, có thể chạy tiếp lệnh sau:
-- ALTER TABLE TRASUA.BRANCH MODIFY (latitude NOT NULL, longitude NOT NULL, delivery_radius NOT NULL);
