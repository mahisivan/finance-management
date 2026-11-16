DROP PROCEDURE IF EXISTS finance.compute_cc_balance;

CREATE PROCEDURE finance.compute_cc_balance(
    IN p_bank_code VARCHAR(255),
    IN p_from_date VARCHAR(10),
    IN p_to_date VARCHAR(10)
)
BEGIN
    SELECT
        COALESCE(SUM(CASE WHEN transaction_type = 'C' THEN amount ELSE 0 END), 0) AS credit_total,
        COALESCE(SUM(CASE WHEN transaction_type = 'D' THEN amount ELSE 0 END), 0) AS debit_total
    FROM finance.cc_transaction_data
    WHERE bank_code = p_bank_code
      AND created_date >= STR_TO_DATE(p_from_date, '%Y-%m-%d')
      AND created_date <= STR_TO_DATE(p_to_date, '%Y-%m-%d');
END;