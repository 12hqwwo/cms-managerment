-- ============================================================
-- 1C. PROC_UPDATE_BILL_STATUS
--     Muc dich: Cap nhat trang thai hoa don,
--               (vd: tu CHO_XAC_NHAN -> CHO_LAY_HANG)
--     Dau vao:
--       p_bill_id      (ID hoa don)
--       p_new_status   (Trang thai moi)
--     Dau ra:
--       p_result_msg   (Thong bao ket qua)
-- ============================================================
CREATE OR REPLACE PROCEDURE PROC_UPDATE_BILL_STATUS (
    p_bill_id     IN NUMBER,
    p_new_status  IN VARCHAR2,
    p_result_msg  OUT VARCHAR2
)
AS
    v_count NUMBER;
BEGIN
    -- Kiem tra hoa don ton tai
    SELECT COUNT(*) INTO v_count
    FROM bill WHERE id = p_bill_id;

    IF v_count = 0 THEN
        p_result_msg := 'Lỗi: Không tìm thấy hóa đơn ID = ' || p_bill_id;
        RETURN;
    END IF;

    UPDATE bill 
    SET status = p_new_status, 
        update_date = SYSTIMESTAMP 
    WHERE id = p_bill_id;
    
    COMMIT;
    p_result_msg := 'SUCCESS';
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_result_msg := 'Lỗi: ' || SQLERRM;
END PROC_UPDATE_BILL_STATUS;
/
