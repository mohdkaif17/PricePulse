import os
import sys
from urllib.parse import quote_plus
from dotenv import load_dotenv

print("=" * 50)
print("     PRICEPULSE DATABASE CONNECTION CHECK")
print("=" * 50)

# Check .env file
if not os.path.exists(".env"):
    print("[ERROR] .env file not found in current directory!")
    sys.exit(1)

load_dotenv(".env")

db_host = os.getenv("DB_HOST", "localhost")
db_port = os.getenv("DB_PORT", "5432")
db_name = os.getenv("DB_NAME", "price_comparison")
db_user = os.getenv("DB_USER", "postgres")
db_password = os.getenv("DB_PASSWORD", "")

print(f"Target Host:     {db_host}:{db_port}")
print(f"Target Database: {db_name}")
print(f"Username:        {db_user}")
print("-" * 50)

try:
    from sqlalchemy import create_engine, text
except ImportError:
    print("[ERROR] SQLAlchemy is not installed in this Python environment.")
    print("Run: py -3.12 -m pip install sqlalchemy psycopg[binary] python-dotenv")
    sys.exit(1)

encoded_password = quote_plus(db_password)
url = f"postgresql+psycopg://{db_user}:{encoded_password}@{db_host}:{db_port}/{db_name}"

try:
    engine = create_engine(url, connect_args={"connect_timeout": 5})
    with engine.connect() as conn:
        version = conn.execute(text("SELECT version();")).scalar()
        tables_res = conn.execute(text("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public';")).scalar()
        
        print("[STATUS]: CONNECTED SUCCESSFULLY!  (Online)")
        print(f"PostgreSQL Version: {version.split(',')[0]}")
        print(f"Public Tables Count: {tables_res}")
        print("=" * 50)
except Exception as ex:
    print("[STATUS]: FAILED TO CONNECT! (Offline or Wrong Password)")
    print(f"Details: {ex}")
    print("=" * 50)
