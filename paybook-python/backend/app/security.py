import os, json, hmac, hashlib, base64, time, secrets
from fastapi import Depends, HTTPException
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.orm import Session
from .database import get_db
from .models import User

SECRET = os.getenv("JWT_SECRET", "CHANGE_THIS_SECRET_BEFORE_DEPLOYMENT")
bearer = HTTPBearer(auto_error=False)

def b64(data):
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()

def hash_password(password):
    salt = secrets.token_bytes(16)
    digest = hashlib.scrypt(password.encode(), salt=salt, n=2**14, r=8, p=1)
    return "scrypt$" + b64(salt) + "$" + b64(digest)

def verify_password(password, stored):
    try:
        if not stored.startswith("scrypt$"):
            return hmac.compare_digest(password, stored)  # legacy; upgraded after successful login
        _, salt_s, digest_s = stored.split("$", 2)
        salt = base64.urlsafe_b64decode(salt_s + "=" * (-len(salt_s) % 4))
        expected = base64.urlsafe_b64decode(digest_s + "=" * (-len(digest_s) % 4))
        actual = hashlib.scrypt(password.encode(), salt=salt, n=2**14, r=8, p=1)
        return hmac.compare_digest(actual, expected)
    except Exception:
        return False

def create_token(user_id):
    now = int(time.time())
    head = b64(json.dumps({"alg":"HS256","typ":"JWT"}, separators=(",",":")).encode())
    body = b64(json.dumps({"sub":str(user_id),"iat":now,"exp":now+43200}, separators=(",",":")).encode())
    message = head + "." + body
    sig = b64(hmac.new(SECRET.encode(), message.encode(), hashlib.sha256).digest())
    return message + "." + sig

def decode_token(token):
    try:
        head, body, sig = token.split(".")
        message = head + "." + body
        expected = b64(hmac.new(SECRET.encode(), message.encode(), hashlib.sha256).digest())
        if not hmac.compare_digest(sig, expected):
            raise ValueError()
        data = json.loads(base64.urlsafe_b64decode(body + "=" * (-len(body) % 4)))
        if data["exp"] < int(time.time()):
            raise ValueError()
        return int(data["sub"])
    except Exception:
        raise HTTPException(401, "Invalid or expired session. Please sign in again.")

def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(bearer),
                     db: Session = Depends(get_db)):
    if not credentials:
        raise HTTPException(401, "Please sign in.")
    user = db.get(User, decode_token(credentials.credentials))
    if not user:
        raise HTTPException(401, "User account not found.")
    return user
