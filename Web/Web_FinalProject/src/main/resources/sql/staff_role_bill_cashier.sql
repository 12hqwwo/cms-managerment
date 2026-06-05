IF COL_LENGTH('bill', 'cashier_account_id') IS NULL
BEGIN
    ALTER TABLE bill ADD cashier_account_id BIGINT NULL;
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM sys.foreign_keys
    WHERE name = 'FK_bill_cashier_account'
)
BEGIN
    ALTER TABLE bill
    ADD CONSTRAINT FK_bill_cashier_account
    FOREIGN KEY (cashier_account_id) REFERENCES account(id);
END
GO

IF NOT EXISTS (
    SELECT 1
    FROM role
    WHERE name = 'ROLE_STAFF'
)
BEGIN
    INSERT INTO role(name, create_date, update_date)
    VALUES ('ROLE_STAFF', GETDATE(), GETDATE());
END
GO
