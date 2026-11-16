-- Primary-key generator records for finance tables
-- Run this script against the finance schema after application tables exist.

INSERT INTO finance.id_gen (gen_key, gen_value) VALUES
    ('ACCOUNT_ID', COALESCE((SELECT MAX(account_id) FROM finance.account_data), 0)),
    ('CARD_TRANSACTION_ID', COALESCE((SELECT MAX(transaction_id) FROM finance.cc_transaction_data), 0)),
    ('COMPANY_ID', COALESCE((SELECT MAX(company_id) FROM finance.company_data), 0)),
    ('DESIGNATION_ID', COALESCE((SELECT MAX(designation_id) FROM finance.designation_data), 0)),
    ('LOAN_ID', COALESCE((SELECT MAX(payment_id) FROM finance.loan_data), 0)),
    ('SALARY_ID', COALESCE((SELECT MAX(salary_id) FROM finance.salary_data), 0)),
    ('TRANSACTION_ID', COALESCE((SELECT MAX(transaction_id) FROM finance.transaction_data), 0)),
    ('USER_ID', COALESCE((SELECT MAX(user_id) FROM finance.user_data), 0))
AS new_values(gen_key, gen_value)
ON DUPLICATE KEY UPDATE gen_value = new_values.gen_value;

-- Keep the generator synchronized after a record is added or deleted.
UPDATE finance.id_gen SET gen_value=(SELECT MAX(account_id) FROM finance.account_data) WHERE gen_key='ACCOUNT_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(company_id) FROM finance.company_data) WHERE gen_key='COMPANY_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(designation_id) FROM finance.designation_data) WHERE gen_key='DESIGNATION_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(payment_id) FROM finance.loan_data) WHERE gen_key='LOAN_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(salary_id) FROM finance.salary_data) WHERE gen_key='SALARY_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(transaction_id) FROM finance.transaction_data) WHERE gen_key='TRANSACTION_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(transaction_id) FROM finance.cc_transaction_data) WHERE gen_key='CARD_TRANSACTION_ID';
UPDATE finance.id_gen SET gen_value=(SELECT MAX(user_id) FROM finance.user_data) WHERE gen_key='USER_ID';
