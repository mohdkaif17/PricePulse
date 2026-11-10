import os
import sys
from dotenv import load_dotenv

load_dotenv("backend/.env")
import psycopg

DB_HOST = os.getenv("DB_HOST", "127.0.0.1")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "price_comparison")
DB_USER = os.getenv("DB_USER", "postgres")
DB_PASSWORD = os.getenv("DB_PASSWORD", "Kaif@123")

conn_str = f"host={DB_HOST} port={DB_PORT} dbname={DB_NAME} user={DB_USER} password={DB_PASSWORD}"

def run_audit():
    with psycopg.connect(conn_str) as conn:
        with conn.cursor() as cur:
            print("=== PRODUCTS_SERVICES COLUMNS ===")
            cur.execute("""
                SELECT column_name, data_type 
                FROM information_schema.columns 
                WHERE table_name = 'products_services';
            """)
            print([r[0] for r in cur.fetchall()])

            print("\n=== SAMPLE PRODUCTS_SERVICES ===")
            cur.execute("""
                SELECT ps.id, ps.name, ps.subcategory_id, sc.name as subcat_name, sc.category_id, c.name as cat_name
                FROM products_services ps
                LEFT JOIN subcategories sc ON ps.subcategory_id = sc.id
                LEFT JOIN categories c ON sc.category_id = c.id
                LIMIT 25;
            """)
            for r in cur.fetchall():
                print(r)

            print("\n=== SAMPLE LISTINGS WITH CATEGORY VIA PRODUCT_SERVICE ===")
            cur.execute("""
                SELECT l.id, COALESCE(l.custom_title, ps.name) as title, 
                       ps.id as ps_id, ps.name as ps_name,
                       sc.id as subcat_id, sc.name as subcat_name,
                       c.id as cat_id, c.name as cat_name,
                       pr.name as provider_name
                FROM listings l
                LEFT JOIN products_services ps ON l.product_service_id = ps.id
                LEFT JOIN subcategories sc ON ps.subcategory_id = sc.id
                LEFT JOIN categories c ON sc.category_id = c.id
                LEFT JOIN providers pr ON l.provider_id = pr.id
                LIMIT 30;
            """)
            for r in cur.fetchall():
                print(r)

            print("\n=== LISTINGS PER CATEGORY IN DB ===")
            cur.execute("""
                SELECT c.id, c.name, COUNT(l.id)
                FROM categories c
                LEFT JOIN subcategories sc ON sc.category_id = c.id
                LEFT JOIN products_services ps ON ps.subcategory_id = sc.id
                LEFT JOIN listings l ON l.product_service_id = ps.id
                GROUP BY c.id, c.name
                ORDER BY c.id;
            """)
            for r in cur.fetchall():
                print(f"Cat {r[0]}: {r[1]} -> {r[2]} listings")

if __name__ == "__main__":
    run_audit()
