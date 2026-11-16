import os
from sqlalchemy import create_engine
from sqlalchemy.orm import DeclarativeBase, sessionmaker

DATABASE_URL = os.getenv(
    "DATABASE_URL",
    "mysql+pymysql://fna_user:fna_pswd@localhost:3306/finance?charset=utf8mb4"
)
engine = create_engine(
    DATABASE_URL,
    connect_args={"ssl_disabled": True},
    pool_pre_ping=True,
)
SessionLocal = sessionmaker(bind=engine, autoflush=False, autocommit=False)

class Base(DeclarativeBase):
    pass

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()
