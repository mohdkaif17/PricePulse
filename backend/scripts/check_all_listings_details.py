import os
from dotenv import load_dotenv
load_dotenv("backend/.env")
import psycopg

DB_HOST = os.getenv("DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "price_comparison")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "Kaif@123")
conn_str = f"host={DB_HOST} port={DB_PORT} dbname={DB_NAME} user={DB_USER} password={DB_PASSWORD}"

with psycopg.connect(conn_str) as conn:
    with conn.cursor() as cur:
        cur.execute("""
            SELECT l.id, COALESCE(l.custom_title, ps.name) as title, 
                   sc.id as subcat_id, sc.name as subcat_name, 
                   c.id as cat_id, c.name as cat_name
            FROM listings l
            JOIN products_and_services ps ON l.product_service_id = ps.id
            JOIN subcategories sc ON ps.subcategory_id = sc.id
            JOIN categories c ON sc.category_id = c.id
            ORDER BY l.id;
        """)
        rows = cur.fetchall()
        print(f"Total listings: {len(rows)}")
        for r in rows:
            print(f"Listing #{r[0]}: [{r[4]} - {r[5]} / {r[2]} - {r[3]}] -> '{r[1]}'")

