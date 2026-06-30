
-- ==============================================================================
-- 6. Procedure: PROC_INVENTORY_REQUISITION
-- Xử lý duyệt yêu cầu cấp phát hàng hóa (Admin duyệt)
-- ==============================================================================
CREATE OR REPLACE PROCEDURE TRASUA.PROC_INVENTORY_REQUISITION (
    p_request_id IN NUMBER,
    p_status IN VARCHAR2, -- 'APPROVED' hoặc 'REJECTED' hoặc 'PARTIAL_APPROVED'
    p_error_msg OUT VARCHAR2
) 
AS
    v_branch_id NUMBER;
    v_old_status VARCHAR2(20);
    v_product_id NUMBER;
    v_req_qty NUMBER;
    v_appr_qty NUMBER;
    
    CURSOR c_req_details IS 
        SELECT id, product_detail_id, requested_quantity, approved_quantity 
        FROM TRASUA.inventory_request_detail
        WHERE request_id = p_request_id;
BEGIN
    p_error_msg := 'SUCCESS';

    -- 1. Lấy thông tin phiếu
    SELECT COUNT(*) INTO v_product_id FROM TRASUA.inventory_request WHERE id = p_request_id;
    IF v_product_id = 0 THEN
        p_error_msg := 'Request ID does not exist.';
        RETURN;
    END IF;

    SELECT branch_id, status INTO v_branch_id, v_old_status
    FROM TRASUA.inventory_request
    WHERE id = p_request_id FOR UPDATE;

    -- 2. Kiểm tra trạng thái
    IF v_old_status != 'PENDING' THEN
        p_error_msg := 'Only PENDING request can be approved or rejected.';
        RETURN;
    END IF;

    -- 3. Xử lý theo trạng thái duyệt
    IF p_status = 'REJECTED' THEN
        UPDATE TRASUA.inventory_request SET status = 'REJECTED', updated_at = SYSTIMESTAMP WHERE id = p_request_id;
    ELSIF p_status = 'APPROVED' OR p_status = 'PARTIAL_APPROVED' THEN
        -- Duyệt qua từng sản phẩm trong phiếu
        FOR r_detail IN c_req_details LOOP
            v_product_id := r_detail.product_detail_id;
            v_appr_qty := r_detail.approved_quantity;
            v_req_qty := r_detail.requested_quantity;
            
            IF p_status = 'APPROVED' THEN
                v_appr_qty := v_req_qty; -- Nếu APPROVED toàn bộ, lấy nguyên số lượng yêu cầu
                UPDATE TRASUA.inventory_request_detail 
                SET approved_quantity = v_req_qty 
                WHERE id = r_detail.id;
            END IF;

            IF v_appr_qty > 0 THEN
                -- Trừ kho tổng (product_detail)
                UPDATE TRASUA.product_detail
                SET quantity = quantity - v_appr_qty
                WHERE id = v_product_id;
                
                -- Cập nhật hoặc Thêm mới vào kho chi nhánh (branch_inventory)
                UPDATE TRASUA.branch_inventory
                SET quantity = quantity + v_appr_qty, updateDate = SYSTIMESTAMP
                WHERE branch_id = v_branch_id AND product_detail_id = v_product_id;
                
                IF SQL%ROWCOUNT = 0 THEN
                    INSERT INTO TRASUA.branch_inventory (branch_id, product_detail_id, quantity, isActive, createDate, updateDate)
                    VALUES (v_branch_id, v_product_id, v_appr_qty, 1, SYSTIMESTAMP, SYSTIMESTAMP);
                END IF;
            END IF;
        END LOOP;
        
        -- Cập nhật trạng thái phiếu
        UPDATE TRASUA.inventory_request SET status = p_status, updated_at = SYSTIMESTAMP WHERE id = p_request_id;
    ELSE
        p_error_msg := 'Invalid status. Must be APPROVED, REJECTED or PARTIAL_APPROVED.';
        RETURN;
    END IF;

    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        p_error_msg := 'ERROR: ' || SQLERRM;
END;
/
SHOW ERRORS PROCEDURE TRASUA.PROC_INVENTORY_REQUISITION;  
EXIT; 
