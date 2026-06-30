-- 6. FGA HOÀN TIỀN (Kiểm toán bảng PAYMENT)
-- Fine-Grained Auditing: Ghi log MỌI thao tác hoàn tiền nhạy cảm
-- Log xem tại: SELECT * FROM DBA_FGA_AUDIT_TRAIL WHERE policy_name = 'AUDIT_REFUND_PAYMENT';

ALTER SESSION SET CURRENT_SCHEMA = TRASUA;

BEGIN
    -- Xóa policy cũ (idempotent)
    BEGIN
        DBMS_FGA.DROP_POLICY('TRASUA', 'PAYMENT', 'AUDIT_REFUND_PAYMENT');
    EXCEPTION
        WHEN OTHERS THEN NULL;
    END;

    -- FGA: Ghi log khi UPDATE/DELETE trên cột nhạy cảm AMOUNT, STATUS_EXCHANGE
    -- audit_condition = NULL: Ghi log TẤT CẢ (không lọc điều kiện). Cũ dùng
    -- 'STATUS_EXCHANGE IS NOT NULL' có thể bỏ sót dòng vừa được reset về NULL.
    DBMS_FGA.ADD_POLICY(
        object_schema   => 'TRASUA',
        object_name     => 'PAYMENT',
        policy_name     => 'AUDIT_REFUND_PAYMENT',
        audit_condition => NULL,                        -- Bắt tất cả
        audit_column    => 'AMOUNT, STATUSEXCHANGE',    -- Chỉ khi 2 cột này bị chạm
        statement_types => 'UPDATE, DELETE',
        audit_trail     => DBMS_FGA.DB + DBMS_FGA.EXTENDED -- Lưu cả SQL text + bind var
    );
END;
/
