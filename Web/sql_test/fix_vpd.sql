BEGIN
  BEGIN
    DBMS_RLS.DROP_POLICY(object_schema => 'TRASUA', object_name => 'BRANCH_INVENTORY', policy_name => 'POL_BRANCH_INVENTORY');
  EXCEPTION WHEN OTHERS THEN NULL; END;

  BEGIN
    DBMS_RLS.DROP_POLICY(object_schema => 'TRASUA', object_name => 'BILL', policy_name => 'POLICY_BILL_STATUS');
  EXCEPTION WHEN OTHERS THEN NULL; END;

  BEGIN
    DBMS_RLS.DROP_POLICY(object_schema => 'TRASUA', object_name => 'BILL', policy_name => 'VPD_POLICY_BILL_BRANCH');
  EXCEPTION WHEN OTHERS THEN NULL; END;
END;
/

