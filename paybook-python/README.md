# PayBook – Finance & Account Management

Full-stack starter application based on the supplied ER diagram.

## Stack
- Frontend: React 18, Vite, React Router, Bootstrap 5, Axios
- Backend: Python 3.11+, FastAPI, SQLAlchemy 2, PyMySQL
- Database: MySQL 8+

## Run locally on Windows
1. Start the local MySQL service and ensure the `finance` database and `fna_user` credentials are configured as described below.
2. Double-click `start_app.bat` (or run it from Command Prompt).
3. Open http://localhost:8080/Paybook/ (API docs: http://localhost:8000/docs).

The launcher starts the backend and frontend without Docker. The backend uses the existing `backend/.venv` when available and installs the MySQL driver if needed. PostgreSQL and MySQL are not interchangeable for this application; use MySQL.

## Run with Docker
1. Install Docker Desktop.
2. In this folder run:
   ```bash
   docker compose up --build
   ```
3. Open http://localhost:8080/Paybook/ (API docs: http://localhost:8000/docs).

The API creates mapped tables on startup. MySQL data is stored in a Docker volume.

## Manual setup (optional)
### Database
Create the `finance` database and grant `fna_user` access to it in your local MySQL server. The configured user needs permission to read and write the application tables and execute the stored procedures used by the app.
The application connects using `mysql+pymysql://fna_user:fna_pswd@localhost:3306/finance`.
To override the connection, set `DATABASE_URL` in `backend/.env`. For example:
`DATABASE_URL=mysql+pymysql://fna_user:fna_pswd@localhost:3306/finance?charset=utf8mb4`

The JDBC options `allowPublicKeyRetrieval` and `useSSL` are Java-driver settings; this Python
application uses PyMySQL and configures its local connection with TLS disabled.

### Backend
```bash
cd backend
python -m venv .venv
# Windows: .venv\Scripts\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Set `VITE_API_URL=http://localhost:8000/api` in `frontend/.env` if needed.
The frontend is served under the `/Paybook/` context path.

## Included modules
- Dashboard totals and recent transactions
- Add and modify records for companies, designations, accounts, salaries, loans, transactions, and card transactions
- Searchable list views and create/edit/delete forms
- Foreign-key dropdowns for known relationships
- Automatic created_date / modified_date timestamps
- Transactions: Search Transaction and Debtor Balance
- Salaries: Search Salary
- Loans: Search Loan
- Card Transactions: Search Card Transactions (All/Credit/Debit) and Compute Card Balance
- Swagger API documentation

The `created_by` and `modified_by` fields are not editable in the UI. On create, the API assigns both to the signed-in user's ID; on update, it sets `modified_by` to that user and preserves `created_by`.

Card transaction searches use the MySQL `finance.get_cc_transaction_data` procedure. Compute Card Balance uses all transaction types; Search Card Transactions allows filtering by All, Credit, or Debit.

## Schema notes
The ER image clearly shows the primary keys and the salary/loan/transaction account, company,
designation and debtor references. The diagram's connector endpoints are not fully legible. The account table does not show a `user_id` column, so none is added.
This starter uses these inferred relationships:
- `salary_data.account_id -> account_data.account_id`
- `salary_data.company_id -> company_data.company_id`
- `salary_data.designation_id -> designation_data.designation_id`
- `loan_data.account_id -> account_data.account_id`
- `transaction_data.account_id -> account_data.account_id`
- `transaction_data.debtor_id -> user_data.user_id`

Audit fields (`created_by`, `modified_by`) are kept as integers but are not enforced as foreign keys,
because the screenshot does not unambiguously establish every connector. Likewise, the
`cc_transaction_data` table retains the columns shown in the image.

## Important production checklist
This is a runnable development starter, not a production-hardened accounting system.
Before production: add JWT/session authentication and role-based authorization, CSRF protection
if cookie auth is used, rate limiting, audit logging, backups, migrations (Alembic), HTTPS,
secrets management, and server-side financial validation. Do not store plaintext passwords:
`user_data.login_password` is retained for schema compatibility, but replace it with a secure
password hash workflow before using real accounts. Confirm inferred foreign keys against your
actual MySQL schema before importing existing data.


## Single-user login and personal data
- A single person is signed in per browser session; other `user_data` rows are counterparties.
- Sign in with an existing `user_data.email_id` and `login_password`.
- A successful login upgrades a legacy plaintext password to a salted scrypt hash.
- The API uses a 12-hour signed bearer token. Set a long random `JWT_SECRET` before deployment.
- Finance records are filtered by `created_by = logged_in_user.user_id`; the API sets `created_by`
  and `modified_by` itself and ignores values submitted by the browser.
- For `transaction_data`, `debtor_id` identifies the other person. The UI offers people from
  `user_data` excluding the signed-in user and shows the counterparty's name.

**Schema assumption requiring confirmation:** the ER diagram does not show a separate owner
column on the finance tables. This build treats `created_by` as the owner user ID. This is
correct only if your existing `created_by` values refer to `user_data.user_id`. The company
and designation rows must also have `created_by` set to the logged-in user's ID. If `created_by`
is only an audit actor or the records are shared, add a dedicated owner `user_id` column and
migrate data before production use. The existing schema has no direct salary-to-person foreign
key; salary ownership is therefore also based on `created_by`.

## Primary-key generator

The application uses `finance.id_gen` to generate IDs for tables managed through the API. The generator keys and their table mappings are:

| Generator key | Table | Primary key |
|---|---|---|
| `ACCOUNT_ID` | `account_data` | `account_id` |
| `CARD_TRANSACTION_ID` | `cc_transaction_data` | `transaction_id` |
| `COMPANY_ID` | `company_data` | `company_id` |
| `DESIGNATION_ID` | `designation_data` | `designation_id` |
| `LOAN_ID` | `loan_data` | `payment_id` |
| `SALARY_ID` | `salary_data` | `salary_id` |
| `TRANSACTION_ID` | `transaction_data` | `transaction_id` |
| `USER_ID` | `user_data` | `user_id` |

Initialize these records by running `backend/sql/id_gen.sql` after the application tables exist. The API reads the matching generator value, allocates the next value, and assigns it to new records. After a create or delete operation, the matching generator is synchronized to the maximum ID currently present in its table.

Deleted IDs are reused. For example, if ID `10` is the highest existing ID and is deleted, synchronization sets the generator value to the new maximum ID, such as `9`; the next generated ID is `10`. This behavior also means that deleting a highest ID and creating a record can result in the same ID being reused. See the synchronization statements in `backend/sql/id_gen.sql` for the SQL used by this process.
