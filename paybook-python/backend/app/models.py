from sqlalchemy import (
    Column, Integer, BigInteger, String, Numeric, Date, DateTime, ForeignKey, Text, Index
)
from sqlalchemy.sql import func
from .database import Base

class User(Base):
    __tablename__ = "user_data"
    user_id = Column(Integer, primary_key=True, index=True)
    acc_status = Column(String(30))
    comm_address = Column(Text)
    date_of_birth = Column(Date)
    email_id = Column(String(255), unique=True, index=True)
    first_name = Column(String(100), nullable=False)
    gender = Column(String(30))
    last_name = Column(String(100))
    login_count = Column(Integer, default=0)
    login_password = Column(String(255))  # Store a password hash, never plaintext.
    middle_name = Column(String(100))
    mobile_no = Column(String(30))
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())

class Company(Base):
    __tablename__ = "company_data"
    company_id = Column(Integer, primary_key=True, index=True)
    company_name = Column(String(200), nullable=False)
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class Designation(Base):
    __tablename__ = "designation_data"
    designation_id = Column(Integer, primary_key=True, index=True)
    designation_name = Column(String(200), nullable=False)
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class Account(Base):
    __tablename__ = "account_data"
    account_id = Column(Integer, primary_key=True, index=True)
    account_no = Column(String(100), nullable=False, index=True)
    bank_name = Column(String(150))
    ifsc = Column(String(20))
    account_type = Column(String(50))
    loan_amount = Column(Numeric(14, 2), default=0)
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class Salary(Base):
    __tablename__ = "salary_data"
    salary_id = Column(Integer, primary_key=True, index=True)
    amount = Column(Numeric(14, 2), nullable=False)
    account_id = Column(Integer, ForeignKey("account_data.account_id"), nullable=False)
    company_id = Column(Integer, ForeignKey("company_data.company_id"), nullable=False)
    designation_id = Column(Integer, ForeignKey("designation_data.designation_id"), nullable=False)
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class Loan(Base):
    __tablename__ = "loan_data"
    payment_id = Column(Integer, primary_key=True, index=True)
    account_id = Column(Integer, ForeignKey("account_data.account_id"), nullable=False)
    principal = Column(Numeric(14, 2), nullable=False)
    emi = Column(Numeric(14, 2), nullable=False)
    interest = Column(Numeric(8, 4))
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class Transaction(Base):
    __tablename__ = "transaction_data"
    transaction_id = Column(Integer, primary_key=True, index=True)
    amount = Column(Numeric(14, 2), nullable=False)
    remarks = Column(Text)
    transaction_type = Column(String(50), nullable=False)
    account_id = Column(Integer, ForeignKey("account_data.account_id"), nullable=False)
    debtor_id = Column(Integer, ForeignKey("user_data.user_id"), nullable=True)
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class CreditCardTransaction(Base):
    __tablename__ = "cc_transaction_data"
    transaction_id = Column(Integer, primary_key=True, index=True)
    bank_code = Column(String(50))
    amount = Column(Numeric(14, 2), nullable=False)
    remarks = Column(Text)
    transaction_type = Column(String(50))
    created_date = Column(DateTime(timezone=True), server_default=func.now())
    created_by = Column(Integer)
    modified_date = Column(DateTime(timezone=True), onupdate=func.now())
    modified_by = Column(Integer)

class IdGen(Base):
    __tablename__ = "id_gen"
    gen_key = Column(String(100), primary_key=True)
    gen_value = Column(BigInteger, nullable=False, default=0)

Index("ix_transaction_date_type", Transaction.created_date, Transaction.transaction_type)
Index("ix_salary_company", Salary.company_id, Salary.designation_id)
