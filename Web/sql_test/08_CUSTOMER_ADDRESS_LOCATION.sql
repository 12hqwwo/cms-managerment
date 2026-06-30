-- Thêm cột latitude và longitude vào bảng ADDRESS_SHIPPING
ALTER TABLE TRASUA.ADDRESS_SHIPPING ADD (latitude NUMBER(10,6), longitude NUMBER(10,6));

-- Lệnh update các record cũ (tùy chọn)
-- UPDATE TRASUA.ADDRESS_SHIPPING SET latitude = 10.850146, longitude = 106.771661 WHERE latitude IS NULL;
