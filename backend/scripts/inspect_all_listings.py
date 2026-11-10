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
        # Check total categories, subcategories, products_and_services, listings
        cur.execute("SELECT count(*) FROM categories;")
        print("Total categories:", cur.fetchone()[0])
        
        cur.execute("SELECT count(*) FROM subcategories;")
        print("Total subcategories:", cur.fetchone()[0])
        
        cur.execute("SELECT count(*) FROM products_and_services;")
        print("Total products_and_services:", cur.fetchone()[0])
        
        cur.execute("SELECT count(*) FROM listings;")
        print("Total listings:", cur.fetchone()[0])

        print("\n--- LISTINGS BREAKDOWN BY CATEGORY ---")
        cur.execute("""
            SELECT c.id, c.name, count(l.id)
            FROM categories c
            LEFT JOIN subcategories sc ON sc.category_id = c.id
            LEFT JOIN products_and_services ps ON ps.subcategory_id = sc.id
            LEFT JOIN listings l ON l.product_service_id = ps.id
            GROUP BY c.id, c.name
            ORDER BY c.id;
        """)
        for row in cur.fetchall():
            print(f"Cat {row[0]}: {row[1]} -> {row[2]} listings")

        print("\n--- SAMPLE LISTINGS PER CATEGORY (First 3 for each) ---")
        cur.execute("""
            SELECT c.id as cat_id, c.name as cat_name, sc.name as subcat_name, 
                   COALESCE(l.custom_title, ps.name) as title, l.price_amount, pr.name as provider_name
            FROM listings l
            JOIN products_and_services ps ON l.product_service_id = ps.id
            JOIN subcategories sc ON ps.subcategory_id = sc.id
            JOIN categories c ON sc.category_id = c.id
            JOIN providers pr ON l.provider_id = pr.id
            ORDER BY c.id, l.id;
        """)
        all_rows = cur.fetchall()
        cur_cat = None
        count_in_cat = 0
        for r in all_rows:
            if r[0] != cur_cat:
                cur_cat = r[0]
                count_in_cat = 0
                print(f"\n[Category {r[0]}: {r[1]}]")
            if count_in_cat < 3:
                print(f"  - Sub: {r[2]} | Title: {r[3]} | Price: {r[4]} | Provider: {r[5]}")
                count_in_cat += 1


