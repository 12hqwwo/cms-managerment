-- ============================================================
-- FILE: 05_fix_inventory.sql
-- MUC DICH: Fix loi kho hang trong (branch_inventory chua co du lieu)
-- NGUYEN NHAN: Cot trong bang branch_inventory dung camelCase
--   (isActive, createDate, updateDate, minQuantity)
--   khac voi snake_case (is_active, create_date...)
-- HUONG DAN: Chay file nay 1 LAN DUY NHAT trong SQL Developer
-- ============================================================

-- Kiem tra xem da co du lieu chua
SELECT COUNT(*) AS "So ban ghi trong branch_inventory"
FROM branch_inventory;

-- Kiem tra so product_detail hien co
SELECT COUNT(*) AS "So product_detail"
FROM product_detail;

-- INSERT toan bo product_detail vao Chi nhanh 1 (Ha Noi)
-- Chi insert nhung san pham CHUA co trong kho
INSERT INTO branch_inventory (branch_id, product_detail_id, quantity, isActive, createDate, updateDate, minQuantity)
SELECT
    (SELECT MIN(id) FROM branch)  AS branch_id,
    pd.id                         AS product_detail_id,
    100                           AS quantity,
    1                             AS isActive,
    SYSTIMESTAMP                  AS createDate,
    SYSTIMESTAMP                  AS updateDate,
    0                             AS minQuantity
FROM product_detail pd
WHERE NOT EXISTS (
    SELECT 1
    FROM branch_inventory bi
    WHERE bi.product_detail_id = pd.id
      AND bi.branch_id = (SELECT MIN(id) FROM branch)
);

COMMIT;

-- Xac nhan sau khi insert
SELECT
    bi.id          AS inventory_id,
    b.branch_name  AS chi_nhanh,
    p.name         AS ten_san_pham,
    sp.name        AS "size",
    c.name         AS duong,
    bi.quantity    AS so_luong,
    bi.isActive    AS con_kinh_doanh
FROM branch_inventory bi
JOIN branch           b  ON bi.branch_id = b.id
JOIN product_detail   pd ON bi.product_detail_id = pd.id
JOIN product          p  ON pd.product_id = p.id
JOIN size_product     sp ON pd.size_id = sp.id
JOIN color            c  ON pd.color_id = c.id
ORDER BY p.name, sp.name;

