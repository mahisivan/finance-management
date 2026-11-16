from decimal import Decimal
from datetime import date, datetime, timedelta, timezone
from typing import Any
from fastapi import FastAPI, Depends, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.orm import Session
from sqlalchemy import select, func, desc, or_, text, case
from sqlalchemy.inspection import inspect
from .database import Base, engine, get_db
from . import models
from .security import get_current_user, verify_password, hash_password, create_token

Base.metadata.create_all(bind=engine)
app = FastAPI(title="PayBook Finance API", version="1.1.0",
              description="Single-user login finance and account management API.")
import os
app.add_middleware(CORSMiddleware,
    allow_origins=os.getenv(
        "CORS_ORIGINS", "http://localhost:8080,http://127.0.0.1:8080"
    ).split(","),
    allow_credentials=True, allow_methods=["*"], allow_headers=["*"])

RESOURCES = {
    "companies": models.Company, "designations": models.Designation,
    "accounts": models.Account, "salaries": models.Salary, "loans": models.Loan,
    "transactions": models.Transaction, "credit-card-transactions": models.CreditCardTransaction,
}
DATE_RESOURCES = {"salaries", "loans", "transactions", "credit-card-transactions"}
PRIMARY_KEYS = {k: inspect(m).primary_key[0].name for k,m in RESOURCES.items()}
ID_GEN_KEYS = {
    "companies": ("COMPANY_ID", "company_data", "company_id"),
    "designations": ("DESIGNATION_ID", "designation_data", "designation_id"),
    "accounts": ("ACCOUNT_ID", "account_data", "account_id"),
    "salaries": ("SALARY_ID", "salary_data", "salary_id"),
    "loans": ("LOAN_ID", "loan_data", "payment_id"),
    "transactions": ("TRANSACTION_ID", "transaction_data", "transaction_id"),
    "credit-card-transactions": ("CARD_TRANSACTION_ID", "cc_transaction_data", "transaction_id"),
}


def synchronize_id_gen(db: Session, resource: str):
    """Set a resource's generator to the current maximum primary key."""
    gen_key, table_name, primary_key = ID_GEN_KEYS[resource]
    statement = text(
        f"UPDATE finance.id_gen "
        f"SET gen_value=COALESCE((SELECT MAX({primary_key}) FROM finance.{table_name}), 0) "
        "WHERE gen_key=:gen_key"
    )
    db.execute(statement, {"gen_key": gen_key})


def allocate_primary_key(db: Session, resource: str):
    gen_key, _, primary_key = ID_GEN_KEYS[resource]
    row = db.execute(
        select(models.IdGen).where(models.IdGen.gen_key == gen_key).with_for_update()
    ).scalar_one_or_none()
    if row is None:
        synchronize_id_gen(db, resource)
        db.flush()
        row = db.execute(select(models.IdGen).where(models.IdGen.gen_key == gen_key)).scalar_one_or_none()
        if row is None:
            raise HTTPException(500, f"ID generator {gen_key} is not configured.")
    next_value = (row.gen_value or 0) + 1
    row.gen_value = next_value
    db.flush()
    return primary_key, next_value

def json_value(v):
    if isinstance(v, Decimal): return float(v)
    if hasattr(v, "isoformat"): return v.isoformat()
    return v

def serialize(row):
    d={prop.key:json_value(getattr(row,prop.key)) for prop in inspect(row).mapper.column_attrs}
    d.pop("login_password",None)
    return d

def schema_for(model):
    return [{"name":c.name,"type":str(c.type),
        "required":not c.nullable and not c.primary_key and c.default is None and c.server_default is None,
        "primary_key":c.primary_key,
        "read_only":c.primary_key or c.name in ("created_date","modified_date","created_by","modified_by")}
        for c in inspect(model).columns]

def owned(model,user):
    # `created_by` is the ownership field in the supplied schema.
    return select(model).where(model.created_by == user.user_id)

def normalize_transaction_type(value):
    code = str(value or "").strip()[:1].upper()
    if code not in ("C", "D"):
        raise HTTPException(422, "Transaction type must be Credit or Debit.")
    return code

def normalize_account_type(value):
    code = str(value or "").strip()[:1].upper()
    if code not in ("S", "C", "L"):
        raise HTTPException(422, "Account type must be Saving, Current, or Loan.")
    return code

def validate_positive_amount(value,field_name):
    try:
        amount=Decimal(str(value))
    except Exception:
        raise HTTPException(422,f"{field_name} must be greater than zero.")
    if not amount.is_finite() or amount <= 0:
        raise HTTPException(422,f"{field_name} must be greater than zero.")
    return amount

def validate_nonnegative_amount(value,field_name):
    try:
        amount=Decimal(str(value))
    except Exception:
        raise HTTPException(422,f"{field_name} cannot be negative.")
    if not amount.is_finite() or amount < 0:
        raise HTTPException(422,f"{field_name} cannot be negative.")
    return amount

def is_loan_part_payment(data):
    try:
        return Decimal(str(data.get("emi"))) == 0 and Decimal(str(data.get("interest") or 0)) == 0
    except Exception:
        return False

def calculate_loan_principal(emi,interest):
    try:
        principal=Decimal(str(emi))-Decimal(str(interest or 0))
    except Exception:
        raise HTTPException(422,"EMI and Interest must be valid amounts.")
    return validate_positive_amount(principal,"Principal")

def parse_created_date(value):
    if isinstance(value, str):
        value = datetime.fromisoformat(value)
    if value.tzinfo is None:
        value = value.replace(tzinfo=timezone.utc)
    return value

@app.get("/api/health")
def health(): return {"status":"ok"}

@app.post("/api/auth/login")
def login(payload:dict,db:Session=Depends(get_db)):
    email=(payload.get("email") or "").strip().lower()
    password=payload.get("password") or ""
    user=db.scalar(select(models.User).where(func.lower(models.User.email_id)==email))
    if not user or not user.login_password or not verify_password(password,user.login_password):
        raise HTTPException(401,"Invalid email or password.")
    if not user.login_password.startswith("scrypt$"):
        user.login_password=hash_password(password)
    user.login_count=(user.login_count or 0)+1
    db.commit()
    return {"access_token":create_token(user.user_id),"token_type":"bearer","user":serialize(user)}

@app.get("/api/auth/me")
def me(user:models.User=Depends(get_current_user)): return serialize(user)

@app.get("/api/resources")
def resources(user:models.User=Depends(get_current_user)):
    return [{"key":k,"label":"Card Transactions" if k == "credit-card-transactions" else k.replace("-"," ").title(),"primary_key":PRIMARY_KEYS[k],"fields":schema_for(m)}
            for k,m in RESOURCES.items()]

@app.get("/api/people")
def people(q:str|None=Query(default=None),db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    rows=db.execute(
        select(models.User.user_id, models.User.first_name, models.User.middle_name,
               models.User.last_name, models.User.email_id)
        .join(models.Transaction, models.Transaction.debtor_id == models.User.user_id)
        .where(models.User.user_id != user.user_id)
        .distinct()
        .order_by(models.User.first_name, models.User.last_name)
        .limit(300)
    ).mappings().all()
    people=[{**row, "mobile_no":None} for row in rows]
    if q:
        term=q.casefold()
        people=[person for person in people if term in " ".join(str(person.get(key) or "") for key in ("first_name","middle_name","last_name","email_id")).casefold()]
    return people

@app.get("/api/debtors/{debtor_id}/balance")
def debtor_balance(debtor_id:int,db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    if debtor_id == user.user_id:
        raise HTTPException(400,"Select another user to view debtor balance.")
    if not db.get(models.User,debtor_id):
        raise HTTPException(404,"User not found.")
    raw_connection=db.connection().connection
    cursor=raw_connection.cursor()
    try:
        cursor.execute(
            "CALL finance.get_balance_sheet_data(%s)",
            (debtor_id,),
        )
        row=cursor.fetchone()
        result=dict(zip((column[0] for column in cursor.description), row)) if row else {}
        while cursor.nextset():
            pass
    finally:
        cursor.close()

    credit_total=result.get("credit_total") or 0
    debit_total=result.get("debit_total") or 0
    return {
        "credit_total":float(credit_total),
        "debit_total":float(debit_total),
        "balance":float(debit_total-credit_total),
    }

@app.get("/api/debtors/balances")
def debtor_balances(db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    credit_total=func.sum(case(
        (models.Transaction.transaction_type == "C", models.Transaction.amount),
        else_=0,
    ))
    debit_total=func.sum(case(
        (models.Transaction.transaction_type == "D", models.Transaction.amount),
        else_=0,
    ))
    balance=debit_total-credit_total
    rows=db.execute(
        select(
            models.User.user_id,
            models.User.first_name,
            models.User.middle_name,
            models.User.last_name,
            models.User.email_id,
            credit_total.label("credit_total"),
            debit_total.label("debit_total"),
            balance.label("balance"),
        )
        .join(models.Transaction, models.Transaction.debtor_id == models.User.user_id)
        .where(models.User.user_id != user.user_id)
        .group_by(
            models.User.user_id,
            models.User.first_name,
            models.User.middle_name,
            models.User.last_name,
            models.User.email_id,
        )
        .having(balance != 0)
        .order_by(func.abs(balance).desc(), models.User.first_name, models.User.last_name)
    ).mappings().all()
    return [
        {
            **row,
            "credit_total":float(row["credit_total"] or 0),
            "debit_total":float(row["debit_total"] or 0),
            "balance":float(row["balance"]),
        }
        for row in rows
    ]

@app.get("/api/dashboard")
def dashboard(transaction_type:str=Query(default="ALL"),db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    transaction_type=transaction_type.upper()
    if transaction_type not in ("ALL","C","D"):
        raise HTTPException(422,"Transaction type must be ALL, C, or D.")
    uid=user.user_id
    def count(m): return db.scalar(select(func.count()).select_from(m).where(m.created_by==uid)) or 0
    salary=db.scalar(select(func.coalesce(func.sum(models.Salary.amount),0)).where(models.Salary.created_by==uid)) or 0
    loans=db.scalar(select(func.coalesce(func.sum(models.Loan.principal),0)).where(models.Loan.created_by==uid)) or 0
    txns=db.scalar(select(func.coalesce(func.sum(models.Transaction.amount),0)).where(models.Transaction.created_by==uid, models.Transaction.transaction_type=="D")) or 0
    recent_statement=select(models.Transaction).where(models.Transaction.created_by==uid)
    if transaction_type != "ALL":
        recent_statement=recent_statement.where(models.Transaction.transaction_type==transaction_type)
    recent=db.scalars(recent_statement.order_by(desc(models.Transaction.created_date)).limit(15)).all()
    recent_rows=[]
    for row in recent:
        d=serialize(row)
        other=db.get(models.User,row.debtor_id) if row.debtor_id else None
        d["counterparty_name"]=" ".join(filter(None,[other.first_name,other.middle_name,other.last_name])) if other else None
        recent_rows.append(d)
    return {"user":serialize(user),"companies":count(models.Company),"designations":count(models.Designation),
        "accounts":count(models.Account),"salaries":count(models.Salary),"loans":count(models.Loan),
        "transactions":count(models.Transaction),"credit_card_transactions":count(models.CreditCardTransaction),
        "total_salary":float(salary),"total_loan_principal":float(loans),
        "total_transaction_amount":float(txns),"recent_transactions":recent_rows}

@app.get("/api/transaction-details")
def transaction_details(account_id:int,debtor_id:int,transaction_type:str,
                        from_date:str,to_date:str,db:Session=Depends(get_db),
                        user:models.User=Depends(get_current_user)):
    if account_id < 0:
        raise HTTPException(422,"Account ID must be zero or a valid account ID.")
    if account_id > 0:
        account=db.scalar(owned(models.Account,user).where(models.Account.account_id==account_id))
        if not account:
            raise HTTPException(404,"Account not found.")
    debtor=db.scalar(
        select(models.User.user_id)
        .join(models.Transaction, models.Transaction.debtor_id == models.User.user_id)
        .where(models.User.user_id == debtor_id, models.User.user_id != user.user_id)
        .limit(1)
    )
    if not debtor:
        raise HTTPException(404,"Debtor not found.")
    transaction_type=transaction_type.upper()
    if transaction_type not in ("ALL","C","D"):
        raise HTTPException(422,"Transaction type must be ALL, Credit, or Debit.")
    try:
        start=date.fromisoformat(from_date)
        end=date.fromisoformat(to_date)
    except ValueError:
        raise HTTPException(422,"From Date and To Date must be valid dates.")
    if start>end:
        raise HTTPException(422,"From Date must be on or before To Date.")
    start_datetime=datetime.combine(start, datetime.min.time())
    end_datetime=datetime.combine(end + timedelta(days=1), datetime.min.time())
    statement=(
        select(
            models.Transaction.transaction_id,
            models.User.first_name,
            models.User.middle_name,
            models.User.last_name,
            models.Transaction.remarks,
            models.Transaction.transaction_type,
            models.Transaction.created_date,
            models.Account.account_no,
            models.Transaction.amount,
        )
        .join(models.User, models.User.user_id == models.Transaction.debtor_id)
        .join(models.Account, models.Account.account_id == models.Transaction.account_id)
        .where(
            models.Transaction.debtor_id == debtor_id,
            models.Transaction.created_date >= start_datetime,
            models.Transaction.created_date < end_datetime,
        )
    )
    if account_id > 0:
        statement=statement.where(models.Transaction.account_id == account_id)
    if transaction_type != "ALL":
        statement=statement.where(models.Transaction.transaction_type == transaction_type)
    rows=db.execute(statement.order_by(
        models.Transaction.created_date,
        models.Transaction.transaction_type.desc(),
    )).mappings().all()
    return [{
        "transaction_id":row["transaction_id"],
        "debtor_name":" ".join(filter(None, [row["first_name"], row["middle_name"], row["last_name"]])),
        "remarks":row["remarks"],
        "transaction_type":row["transaction_type"],
        "created_date":json_value(row["created_date"]),
        "account_no":row["account_no"],
        "credited_amount":json_value(row["amount"] if row["transaction_type"] == "C" else 0),
        "debited_amount":json_value(row["amount"] if row["transaction_type"] == "D" else 0),
    } for row in rows]

@app.get("/api/loan-details")
def loan_details(account_id:int,from_date:str,to_date:str,
                 db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    account=db.scalar(owned(models.Account,user).where(models.Account.account_id==account_id))
    if not account or str(account.account_type or "").strip().upper() != "L":
        raise HTTPException(404,"Loan account not found.")
    try:
        start=date.fromisoformat(from_date)
        end=date.fromisoformat(to_date)
    except ValueError:
        raise HTTPException(422,"From Date and To Date must be valid dates.")
    if start>end:
        raise HTTPException(422,"From Date must be on or before To Date.")
    start_datetime=datetime.combine(start, datetime.min.time())
    end_datetime=datetime.combine(end + timedelta(days=1), datetime.min.time())
    rows=db.execute(
        select(
            models.Loan.payment_id,
            models.Account.account_no,
            models.Loan.emi,
            models.Loan.principal,
            models.Loan.interest,
            models.Account.loan_amount,
            models.Loan.created_date,
        )
        .join(models.Account, models.Account.account_id == models.Loan.account_id)
        .where(
            models.Loan.account_id == account_id,
            models.Loan.created_date >= start_datetime,
            models.Loan.created_date < end_datetime,
        )
        .order_by(models.Loan.created_date.desc())
    ).mappings().all()
    return [{key:json_value(value) for key,value in row.items()} for row in rows]

@app.get("/api/salary-details")
def salary_details(company_id:int,from_date:str,to_date:str,
                   db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    company=db.scalar(owned(models.Company,user).where(models.Company.company_id==company_id))
    if not company:
        raise HTTPException(404,"Company not found.")
    try:
        start=date.fromisoformat(from_date)
        end=date.fromisoformat(to_date)
    except ValueError:
        raise HTTPException(422,"From Date and To Date must be valid dates.")
    if start>end:
        raise HTTPException(422,"From Date must be on or before To Date.")
    start_datetime=datetime.combine(start, datetime.min.time())
    end_datetime=datetime.combine(end + timedelta(days=1), datetime.min.time())
    rows=db.execute(
        select(
            func.row_number().over(order_by=models.Salary.salary_id).label("sl_no"),
            models.Salary.salary_id,
            models.Designation.designation_name,
            models.Salary.amount.label("salary_drawn"),
            models.Account.account_no,
        )
        .join(models.Company, models.Company.company_id == models.Salary.company_id)
        .join(models.Designation, models.Designation.designation_id == models.Salary.designation_id)
        .join(models.Account, models.Account.account_id == models.Salary.account_id)
        .where(
            models.Salary.company_id == company_id,
            models.Salary.created_date >= start_datetime,
            models.Salary.created_date < end_datetime,
        )
        .order_by(models.Salary.salary_id)
    ).mappings().all()
    return [{key:json_value(value) for key,value in row.items()} for row in rows]

@app.get("/api/card-bank-codes")
def card_bank_codes(db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    return db.scalars(
        select(models.CreditCardTransaction.bank_code)
        .where(models.CreditCardTransaction.bank_code.is_not(None))
        .distinct()
        .order_by(models.CreditCardTransaction.bank_code)
    ).all()

@app.get("/api/card-transaction-details")
def card_transaction_details(bank_code:str,tx_type:str,from_date:str,to_date:str,
                             db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    if not bank_code.strip():
        raise HTTPException(422,"Select a Bank Code.")
    tx_type=tx_type.strip().upper()
    if tx_type not in ("ALL", "C", "D"):
        raise HTTPException(422,"Transaction type must be ALL, Credit, or Debit.")
    try:
        start=date.fromisoformat(from_date)
        end=date.fromisoformat(to_date)
    except ValueError:
        raise HTTPException(422,"From Date and To Date must be valid dates.")
    if start>end:
        raise HTTPException(422,"From Date must be on or before To Date.")

    raw_connection=db.connection().connection
    cursor=raw_connection.cursor()
    try:
        cursor.execute(
            "CALL finance.get_cc_transaction_data(%s, %s, %s, %s)",
            (bank_code, tx_type, start.isoformat(), end.isoformat()),
        )
        columns=[column[0] for column in cursor.description]
        rows=[dict(zip(columns, row)) for row in cursor.fetchall()]
        while cursor.nextset():
            pass
    finally:
        cursor.close()
    return [{key:json_value(value) for key,value in row.items()} for row in rows]

@app.get("/api/{resource}")
def list_records(resource:str,q:str|None=Query(default=None),limit:int=Query(default=100,ge=1,le=500),
                 offset:int=Query(default=0,ge=0),db:Session=Depends(get_db),
                 user:models.User=Depends(get_current_user)):
    model=RESOURCES.get(resource)
    if not model: raise HTTPException(404,"Unknown resource")
    stmt=owned(model,user)
    if q:
        cols=[c for c in inspect(model).columns if any(t in str(c.type).lower() for t in ("char","text"))]
        if cols: stmt=stmt.where(or_(*[c.ilike(f"%{q}%") for c in cols]))
    total=db.scalar(select(func.count()).select_from(stmt.order_by(None).subquery())) or 0
    rows=db.scalars(stmt.order_by(getattr(model,PRIMARY_KEYS[resource]).asc()).limit(limit).offset(offset)).all()
    items=[serialize(r) for r in rows]
    if resource=="transactions":
        for d in items:
            p=db.get(models.User,d["debtor_id"]) if d.get("debtor_id") else None
            d["counterparty_name"]=" ".join(filter(None,[p.first_name,p.middle_name,p.last_name])) if p else None
    return {"items":items,"limit":limit,"offset":offset,"total":total}

@app.get("/api/{resource}/{record_id}")
def get_record(resource:str,record_id:int,db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    model=RESOURCES.get(resource)
    if not model: raise HTTPException(404,"Unknown resource")
    row=db.scalar(owned(model,user).where(getattr(model,PRIMARY_KEYS[resource])==record_id))
    if not row: raise HTTPException(404,"Record not found")
    return serialize(row)

@app.post("/api/{resource}",status_code=201)
def create_record(resource:str,payload:dict,db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    model=RESOURCES.get(resource)
    if not model: raise HTTPException(404,"Unknown resource")
    allowed={c.name for c in inspect(model).columns}
    data={k:v for k,v in payload.items() if k in allowed and k not in (PRIMARY_KEYS[resource],"created_by","modified_by")}
    if resource in DATE_RESOURCES:
        if "created_date" in data and data["created_date"]:
            data["created_date"]=parse_created_date(data["created_date"])
        data["modified_date"]=datetime.now(timezone.utc)
    if resource == "accounts":
        data["account_type"]=normalize_account_type(data.get("account_type"))
        if data["account_type"] == "L":
            data["loan_amount"]=validate_positive_amount(data.get("loan_amount"),"Loan Amount")
        else:
            data["loan_amount"]=0
    part_payment=resource == "loans" and is_loan_part_payment(data)
    if resource == "loans" and not part_payment and "emi" in data:
        data["principal"]=calculate_loan_principal(data["emi"],data.get("interest"))
    for field in ("amount","principal","emi"):
        if field in data:
            if field == "emi" and part_payment:
                data[field]=Decimal("0")
            else:
                data[field]=validate_positive_amount(data[field],field.replace("_"," ").title())
    if resource == "loans" and "interest" in data:
        data["interest"]=validate_nonnegative_amount(data["interest"] or 0,"Interest")
    if resource in ("transactions", "credit-card-transactions") and "transaction_type" in data:
        data["transaction_type"]=normalize_transaction_type(data["transaction_type"])
    if resource == "transactions" and str(data.get("account_id")) == "1":
        transaction_count=db.scalar(
            select(func.count(models.Transaction.remarks))
            .where(models.Transaction.account_id == 1)
        ) or 0
        data["remarks"] = f"CASH_PAYMENT_{transaction_count+1}"
    if "created_by" in allowed: data["created_by"]=user.user_id
    if "modified_by" in allowed: data["modified_by"]=user.user_id
    synchronize_id_gen(db, resource)
    primary_key_name, next_id = allocate_primary_key(db, resource)
    data[primary_key_name]=next_id
    row=model(**data);db.add(row)
    try:
        db.flush()
        synchronize_id_gen(db, resource)
        db.commit();db.refresh(row)
    except Exception as exc:
        db.rollback()
        raise HTTPException(400,f"Could not create record. Check required fields and references. {str(exc.orig) if getattr(exc,'orig',None) else ''}")
    return serialize(row)

@app.put("/api/{resource}/{record_id}")
def update_record(resource:str,record_id:int,payload:dict,db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    model=RESOURCES.get(resource)
    if not model: raise HTTPException(404,"Unknown resource")
    row=db.scalar(owned(model,user).where(getattr(model,PRIMARY_KEYS[resource])==record_id))
    if not row: raise HTTPException(404,"Record not found")
    if resource == "transactions" and row.account_id == 1:
        raise HTTPException(403,"Transactions for cash account 1 cannot be edited.")
    allowed={c.name for c in inspect(model).columns}
    part_payment=resource == "loans" and is_loan_part_payment(payload)
    for k,v in payload.items():
        if k in allowed and k not in (PRIMARY_KEYS[resource],"created_by","modified_by","modified_date","login_password"):
            if k == "transaction_type" and resource in ("transactions", "credit-card-transactions"):
                v=normalize_transaction_type(v)
            if k == "account_type" and resource == "accounts":
                v=normalize_account_type(v)
            if k == "emi" and part_payment:
                v=Decimal("0")
            elif k in ("amount","principal","emi"):
                v=validate_positive_amount(v,k.replace("_"," ").title())
            if k == "interest" and resource == "loans":
                v=validate_nonnegative_amount(v or 0,"Interest")
            if k == "loan_amount" and resource == "accounts" and row.account_type == "L":
                v=validate_positive_amount(v,"Loan Amount")
            if k == "created_date" and resource in DATE_RESOURCES and v:
                v=parse_created_date(v)
            setattr(row,k,v)
    if resource == "loans" and not part_payment:
        row.principal=calculate_loan_principal(row.emi,row.interest)
    if resource in DATE_RESOURCES:
        row.modified_date=datetime.now(timezone.utc)
    if resource == "accounts":
        if row.account_type != "L":
            row.loan_amount=0
        else:
            row.loan_amount=validate_positive_amount(row.loan_amount,"Loan Amount")
    if hasattr(row,"modified_by"): row.modified_by=user.user_id
    try: db.commit();db.refresh(row)
    except Exception as exc:
        db.rollback();raise HTTPException(400,f"Could not update record: {str(exc.orig) if getattr(exc,'orig',None) else ''}")
    return serialize(row)

@app.delete("/api/{resource}/{record_id}",status_code=204)
def delete_record(resource:str,record_id:int,db:Session=Depends(get_db),user:models.User=Depends(get_current_user)):
    model=RESOURCES.get(resource)
    if not model: raise HTTPException(404,"Unknown resource")
    row=db.scalar(owned(model,user).where(getattr(model,PRIMARY_KEYS[resource])==record_id))
    if not row: raise HTTPException(404,"Record not found")
    try:
        db.delete(row)
        db.flush()
        synchronize_id_gen(db, resource)
        db.commit()
    except Exception:
        db.rollback();raise HTTPException(409,"Record is referenced by another record and cannot be deleted.")
