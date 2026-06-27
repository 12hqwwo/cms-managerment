-- =================================================================
-- 9_PROC_UPDATE_BILL_STATUS.sql
-- Stored Procedure: Cập nhật trạng thái hóa đơn
-- Được gọi bởi BillServiceImpl.updateStatus() trong Java
-- Schema: TRASUA (Oracle 12c+)
-- RBAC: GRANT EXECUTE ON PROC_UPDATE_BILL_STATUS TO TRASUA;
-- =================================================================

CREATE OR REPLACE PROCEDURE PROC_UPDATE_BILL_STATUS (
    p_bill_id    IN  NUMBER,      -- ID hóa đơn cần cập nhật
    p_new_status IN  VARCHAR2,    -- Trạng thái mới: CHO_XAC_NHAN | CHO_LAY_HANG | CHO_GIAO_HANG | HOAN_THANH | HUY | TRA_HANG
    p_result_msg OUT VARCHAR2     -- 'SUCCESS' hoặc mô tả lỗi
)
IS
    v_count NUMBER;
BEGIN
    -- Kiểm tra bill tồn tại
    SELECT COUNT(*) INTO v_count FROM bill WHERE id = p_bill_id;
    IF v_count = 0 THEN
        p_result_msg := 'BILL_NOT_FOUND:' || p_bill_id;
        RETURN;
    END IF;

    -- Validate trạng thái hợp lệ
    IF p_new_status NOT IN ('CHO_XAC_NHAN','CHO_LAY_HANG','CHO_GIAO_HANG','HOAN_THANH','HUY','TRA_HANG') THEN
        p_result_msg := 'INVALID_STATUS:' || p_new_status;
        RETURN;
    END IF;

    -- Cập nhật trạng thái
    UPDATE bill
    SET    status      = p_new_status,
           update_date = SYSTIMESTAMP
    WHERE  id = p_bill_id;

    COMMIT;
    p_result_msg := 'SUCCESS';

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_msg := SQLERRM;
END PROC_UPDATE_BILL_STATUS;
/

-- Kiểm tra sau khi tạo
SELECT object_name, status FROM user_objects WHERE object_name = 'PROC_UPDATE_BILL_STATUS';
