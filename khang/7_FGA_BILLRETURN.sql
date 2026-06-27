-- 7. FGA TRẢ HÀNG (BẢNG BILL_RETURN)
ALTER SESSION SET CURRENT_SCHEMA = TRASUA;
BEGIN
    BEGIN DBMS_FGA.DROP_POLICY('TRASUA', 'BILL_RETURN', 'AUDIT_BILL_RETURN'); EXCEPTION WHEN OTHERS THEN NULL; END;
    DBMS_FGA.ADD_POLICY(
        object_schema   => 'TRASUA',
        object_name     => 'BILL_RETURN',
        policy_name     => 'AUDIT_BILL_RETURN',
        audit_condition => '1=1', 
        audit_column    => 'RETURN_MONEY, RETURN_STATUS',
        statement_types => 'INSERT, UPDATE, DELETE',
        audit_trail     => DBMS_FGA.DB + DBMS_FGA.EXTENDED
    );
END;
/
