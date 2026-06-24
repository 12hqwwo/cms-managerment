-- ============================================================
-- PHAN 2: TAO SCHEMA (TABLES, SEQUENCES, INDEXES, FK)
-- Chay bang chinh user TRASUA (hoac prefix TRASUA. neu chay tu DBA)
-- Tuong thich: Oracle 21c / 23ai + Spring Boot 2.7.x + Hibernate 5
-- Naming strategy: PhysicalNamingStrategyStandardImpl
--   => ten cot trong DB phai khop CHINH XAC voi @Column(name=...) trong Entity
--      hoac ten field Java (neu khong co @Column)
-- ============================================================

-- ============================================================
-- LUU Y TRUOC KHI CHAY:
--   1. Dang nhap bang user TRASUA: CONNECT TRASUA/"TraSua@2024"
--   2. Tat cac bang se luu vao TS_TRASUA (DEFAULT TABLESPACE cua TRASUA)
--   3. Khong dung hibernate_sequence vi toan bo bang dung IDENTITY
-- ============================================================

-- ============================================================
-- TABLE: role
-- Entity: Role.java  | @Table(name = "role")
-- ============================================================
CREATE TABLE role (
    id          NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    create_date TIMESTAMP(6)  NULL,
    name        VARCHAR2(50)  NOT NULL,
    update_date TIMESTAMP(6)  NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: customer
-- Entity: Customer.java  | @Table(name = "customer")
-- ============================================================
CREATE TABLE customer (
    id           NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code         VARCHAR2(255)  NULL,
    email        VARCHAR2(255)  NULL,
    name         NVARCHAR2(255) NULL,
    phone_number VARCHAR2(255)  NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: branch
-- Entity: Branch.java  | @Table(name = "branch")
-- ============================================================
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

-- ============================================================
-- TABLE: account
-- Entity: Account.java  | @Table(name = "account")
-- Fields: code, birth_day, email, password, create_date,
--         update_date, is_non_locked, customer_id, role_id, branch_id
-- ============================================================
CREATE TABLE account (
    id            NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    birth_day     TIMESTAMP(6)  NULL,
    code          VARCHAR2(255) NULL,
    create_date   TIMESTAMP(6)  NULL,
    email         VARCHAR2(255) NULL,
    is_non_locked NUMBER(1)     DEFAULT 1 NOT NULL,   -- true = khong bi khoa
    password      VARCHAR2(255) NULL,
    update_date   TIMESTAMP(6)  NULL,
    customer_id   NUMBER(19)    NULL,
    role_id       NUMBER(19)    NULL,
    branch_id     NUMBER(19)    NULL,
    CONSTRAINT UK_account_code UNIQUE (code)
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: address_shipping
-- Entity: AddressShipping.java  | @Table(name = "address_shipping")
-- ============================================================
CREATE TABLE address_shipping (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    address     NVARCHAR2(150) NOT NULL,
    customer_id NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: payment_method
-- Entity: PaymentMethod.java  | @Table(name = "payment_method")
-- Ghi chu: @Id khong dung @GeneratedValue => id duoc set thu cong
-- ============================================================
CREATE TABLE payment_method (
    id     NUMBER(19)    NOT NULL,
    name   VARCHAR2(255) NULL,
    status NUMBER(10)    DEFAULT 0 NOT NULL,
    CONSTRAINT PK_payment_method PRIMARY KEY (id)
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: discount_code
-- Entity: DiscountCode.java  | @Table(name = "discount_code")
-- ============================================================
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

-- ============================================================
-- TABLE: discount_code_backup
-- (Bang luu lich su, khong co Entity tuong ung)
-- ============================================================
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

-- ============================================================
-- TABLE: bill
-- Entity: Bill.java  | @Table(name = "bill")
-- ============================================================
CREATE TABLE bill (
    id                NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount            FLOAT(53)      NULL,
    billing_address   NVARCHAR2(255) NULL,
    code              VARCHAR2(50)   NOT NULL,
    create_date       TIMESTAMP(6)   NULL,
    invoice_type      VARCHAR2(255)  NULL,
    promotion_price   FLOAT(53)      DEFAULT 0 NOT NULL,
    return_status     NUMBER(1)      NULL,
    status            VARCHAR2(255)  NULL,
    update_date       TIMESTAMP(6)   NULL,
    customer_id       NUMBER(19)     NULL,
    discount_code_id  NUMBER(19)     NULL,
    payment_method_id NUMBER(19)     NULL,
    branch_id         NUMBER(19)     NULL,
    cashier_account_id NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: brand
-- Entity: Brand.java  | @Table(name = "brand")
-- ============================================================
CREATE TABLE brand (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    status      NUMBER(10)     DEFAULT 0 NOT NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: category
-- Entity: Category.java  | @Table(name = "category")
-- ============================================================
CREATE TABLE category (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    status      NUMBER(10)     DEFAULT 0 NOT NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: material
-- Entity: Material.java  | @Table(name = "material")
-- ============================================================
CREATE TABLE material (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    status      NUMBER(10)     DEFAULT 0 NOT NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: color
-- Entity: Color.java  | @Table(name = "color")
-- ============================================================
CREATE TABLE color (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: size_product
-- Entity: Size.java  | @Table(name = "size_product")
-- Ghi chu: "SIZE" la reserved keyword trong Oracle, doi thanh size_product.
-- ============================================================
CREATE TABLE size_product (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    delete_flag NUMBER(1)      DEFAULT 0 NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: product
-- Entity: Product.java  | @Table(name = "product")
-- ============================================================
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

-- ============================================================
-- TABLE: product_detail
-- Entity: ProductDetail.java  | @Table(name = "product_detail")
-- ============================================================
CREATE TABLE product_detail (
    id         NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    barcode    VARCHAR2(255) NULL,
    price      FLOAT(53)     DEFAULT 0 NOT NULL,
    quantity   NUMBER(10)    DEFAULT 0 NOT NULL,
    color_id   NUMBER(19)    NULL,
    product_id NUMBER(19)    NULL,
    size_id    NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: image
-- Entity: Image.java  | @Table(name = "image")
-- ============================================================
CREATE TABLE image (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    create_date TIMESTAMP(6)   NULL,
    file_type   VARCHAR2(255)  NULL,
    link        VARCHAR2(255)  NULL,
    name        NVARCHAR2(255) NULL,
    update_date TIMESTAMP(6)   NULL,
    product_id  NUMBER(19)     NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: topping
-- Entity: Topping.java  | @Table(name = "topping")
-- ============================================================
CREATE TABLE topping (
    id          NUMBER(19)     GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    description NVARCHAR2(255) NULL,
    name        NVARCHAR2(255) NULL,
    price       NUMBER(19,2)   DEFAULT 0 NOT NULL,
    status      NUMBER(1)      DEFAULT 0 NOT NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: bill_detail
-- Entity: BillDetail.java  | @Table(name = "bill_detail")
-- ============================================================
CREATE TABLE bill_detail (
    id                NUMBER(19)  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    moment_price      FLOAT(53)   NULL,
    quantity          NUMBER(10)  NULL,
    return_quantity   NUMBER(10)  NULL,
    bill_id           NUMBER(19)  NULL,
    product_detail_id NUMBER(19)  NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: bill_detail_topping
-- Entity: BillDetailTopping.java  | @Table(name = "bill_detail_topping")
-- ============================================================
CREATE TABLE bill_detail_topping (
    id             NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    topping_name   VARCHAR2(255) NULL,
    topping_price  FLOAT(53)     NULL,
    bill_detail_id NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: bill_return
-- Entity: BillReturn.java  | @Table(name = "bill_return")
-- Fields (PhysicalNamingStrategyStandardImpl map truc tiep):
--   code, returnReason, returnDate, percentFeeExchange,
--   returnMoney, isCancel, returnStatus, bill_id (FK)
-- ============================================================
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

-- ============================================================
-- TABLE: branch_inventory
-- Entity: BranchInventory.java  | @Table(name = "branch_inventory")
-- Fields: branch_id, product_detail_id, quantity,
--         minQuantity, maxQuantity, isActive, createDate, updateDate
-- ============================================================
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

-- ============================================================
-- TABLE: cart
-- Entity: Cart.java  | @Table(name = "cart")
-- ============================================================
CREATE TABLE cart (
    id                NUMBER(19)   GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    create_date       TIMESTAMP(6) NULL,
    quantity          NUMBER(10)   DEFAULT 0 NOT NULL,
    update_date       TIMESTAMP(6) NULL,
    account_id        NUMBER(19)   NULL,
    product_detail_id NUMBER(19)   NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: chat_message
-- Entity: ChatMessageEntity.java  | @Table(name = "chat_message")
-- Fields: content, sender, room_id, sender_id, receiver_id,
--         seen, create_date, created_at
-- ============================================================
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

-- ============================================================
-- TABLE: payment
-- Entity: Payment.java  | @Table(name = "payment")
-- Fields (map truc tiep tu ten field Java):
--   orderId, amount, orderStatus, paymentDate, statusExchange, bill_id (FK)
-- ============================================================
CREATE TABLE payment (
    id              NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    amount          VARCHAR2(255) NULL,
    orderId         VARCHAR2(255) NULL,
    orderStatus     VARCHAR2(255) NULL,
    paymentDate     TIMESTAMP(6)  NULL,
    statusExchange  NUMBER(10)    NULL,
    bill_id         NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: product_discount
-- Entity: ProductDiscount.java  | @Table(name = "product_discount")
-- Fields (map truc tiep tu ten field Java):
--   discountedAmount, startDate, endDate, closed, product_detail_id (FK)
-- ============================================================
CREATE TABLE product_discount (
    id                 NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    closed             NUMBER(1)     DEFAULT 0 NOT NULL,
    discountedAmount   FLOAT(53)     NULL,
    endDate            TIMESTAMP(6)  NULL,
    startDate          TIMESTAMP(6)  NULL,
    product_detail_id  NUMBER(19)    NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: return_detail
-- Entity: ReturnDetail.java  | @Table(name = "return_detail")
-- Fields (map truc tiep tu ten field Java):
--   quantityReturn, momentPriceRefund, return_id (FK), product_detail_id (FK)
-- ============================================================
CREATE TABLE return_detail (
    id                  NUMBER(19)  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    momentPriceRefund   FLOAT(53)   NULL,
    quantityReturn      NUMBER(10)  NULL,
    return_id           NUMBER(19)  NULL,
    product_detail_id   NUMBER(19)  NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- TABLE: verification_code
-- Entity: VerificationCode.java  | @Table(name = "verification_code")
-- ============================================================
CREATE TABLE verification_code (
    id          NUMBER(19)    GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR2(6)   NOT NULL,
    expiry_time TIMESTAMP(6)  NOT NULL,
    account_id  NUMBER(19)    NOT NULL
) TABLESPACE TS_TRASUA;

-- ============================================================
-- INDEXES (kem TABLESPACE tuong minh)
-- ============================================================

CREATE INDEX idx_account_customer
    ON account (customer_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_account_role
    ON account (role_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_account_branch
    ON account (branch_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_branch_inventory_branch
    ON branch_inventory (branch_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_branch_inventory_product
    ON branch_inventory (product_detail_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_bill_customer
    ON bill (customer_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_bill_detail_bill
    ON bill_detail (bill_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_product_detail_product
    ON product_detail (product_id)
    TABLESPACE TS_TRASUA;

CREATE INDEX idx_verification_account
    ON verification_code (account_id)
    TABLESPACE TS_TRASUA;

-- ============================================================
-- FOREIGN KEY CONSTRAINTS
-- ============================================================

-- account
ALTER TABLE account ADD CONSTRAINT fk_account_customer
    FOREIGN KEY (customer_id) REFERENCES customer (id) ON DELETE SET NULL;

ALTER TABLE account ADD CONSTRAINT fk_account_role
    FOREIGN KEY (role_id) REFERENCES role (id);

ALTER TABLE account ADD CONSTRAINT fk_account_branch
    FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE SET NULL;

-- address_shipping
ALTER TABLE address_shipping ADD CONSTRAINT fk_addr_shipping_customer
    FOREIGN KEY (customer_id) REFERENCES customer (id);

-- bill
ALTER TABLE bill ADD CONSTRAINT fk_bill_branch
    FOREIGN KEY (branch_id) REFERENCES branch (id);

ALTER TABLE bill ADD CONSTRAINT fk_bill_customer
    FOREIGN KEY (customer_id) REFERENCES customer (id);

ALTER TABLE bill ADD CONSTRAINT fk_bill_payment_method
    FOREIGN KEY (payment_method_id) REFERENCES payment_method (id);

ALTER TABLE bill ADD CONSTRAINT fk_bill_discount_code
    FOREIGN KEY (discount_code_id) REFERENCES discount_code (id);

-- bill_detail
ALTER TABLE bill_detail ADD CONSTRAINT fk_bill_detail_product_detail
    FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);

ALTER TABLE bill_detail ADD CONSTRAINT fk_bill_detail_bill
    FOREIGN KEY (bill_id) REFERENCES bill (id);

-- bill_detail_topping
ALTER TABLE bill_detail_topping ADD CONSTRAINT fk_bdt_bill_detail
    FOREIGN KEY (bill_detail_id) REFERENCES bill_detail (id);

-- bill_return
ALTER TABLE bill_return ADD CONSTRAINT fk_bill_return_bill
    FOREIGN KEY (bill_id) REFERENCES bill (id);

-- branch_inventory
ALTER TABLE branch_inventory ADD CONSTRAINT fk_branch_inv_branch
    FOREIGN KEY (branch_id) REFERENCES branch (id) ON DELETE CASCADE;

ALTER TABLE branch_inventory ADD CONSTRAINT fk_branch_inv_product
    FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);

-- cart
ALTER TABLE cart ADD CONSTRAINT fk_cart_account
    FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE cart ADD CONSTRAINT fk_cart_product_detail
    FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);

-- image
ALTER TABLE image ADD CONSTRAINT fk_image_product
    FOREIGN KEY (product_id) REFERENCES product (id);

-- payment
ALTER TABLE payment ADD CONSTRAINT fk_payment_bill
    FOREIGN KEY (bill_id) REFERENCES bill (id);

-- product
ALTER TABLE product ADD CONSTRAINT fk_product_brand
    FOREIGN KEY (brand_id) REFERENCES brand (id);

ALTER TABLE product ADD CONSTRAINT fk_product_category
    FOREIGN KEY (category_id) REFERENCES category (id);

ALTER TABLE product ADD CONSTRAINT fk_product_material
    FOREIGN KEY (material_id) REFERENCES material (id);

ALTER TABLE product ADD CONSTRAINT fk_product_color
    FOREIGN KEY (color_id) REFERENCES color (id);

-- product_detail
ALTER TABLE product_detail ADD CONSTRAINT fk_pd_product
    FOREIGN KEY (product_id) REFERENCES product (id);

ALTER TABLE product_detail ADD CONSTRAINT fk_pd_color
    FOREIGN KEY (color_id) REFERENCES color (id);

ALTER TABLE product_detail ADD CONSTRAINT fk_pd_size
    FOREIGN KEY (size_id) REFERENCES size_product (id);

-- product_discount
ALTER TABLE product_discount ADD CONSTRAINT fk_pdiscount_product_detail
    FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);

-- return_detail
ALTER TABLE return_detail ADD CONSTRAINT fk_return_detail_bill_return
    FOREIGN KEY (return_id) REFERENCES bill_return (id);

ALTER TABLE return_detail ADD CONSTRAINT fk_return_detail_product
    FOREIGN KEY (product_detail_id) REFERENCES product_detail (id);

-- verification_code
ALTER TABLE verification_code ADD CONSTRAINT fk_verification_account
    FOREIGN KEY (account_id) REFERENCES account (id);

COMMIT;

-- ============================================================
-- Kiem tra nhanh sau khi chay xong:
--   SELECT table_name, tablespace_name
--   FROM user_tables
--   ORDER BY table_name;
--
--   SELECT constraint_name, constraint_type, table_name, status
--   FROM user_constraints
--   ORDER BY table_name, constraint_type;
-- ============================================================

-- ============================================================
-- END OF SCRIPT
-- ============================================================
ALTER TABLE bill ADD cashier_account_id NUMBER(19) NULL;
