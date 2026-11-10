"""
PricePulse - Master Dataset CSV Validation & Repeatable Importer
Phase 13 Implementation

Validates and imports Bengaluru Master Dataset CSVs:
- areas.csv
- categories.csv
- subcategories.csv
- providers.csv
- provider_locations.csv
- products_and_services.csv
- product_service_attributes.csv
- listings.csv
- price_observations.csv

Features:
- Foreign key validation across parent-child entities
- Geographic coordinate bounding validation (Bengaluru bbox)
- Strict price format and numeric validation
- Idempotent upsert tracking: INSERTED, UPDATED, SKIPPED, REJECTED
- Comprehensive error reporting per rejected row
"""

import os
import sys
import csv
import json
from decimal import Decimal, InvalidOperation
from datetime import datetime
import psycopg2
from psycopg2.extras import RealDictCursor

# Bengaluru Bounding Box Validation
BENGALURU_LAT_MIN = 12.70
BENGALURU_LAT_MAX = 13.25
BENGALURU_LON_MIN = 77.40
BENGALURU_LON_MAX = 77.85

CSV_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "data", "csv")

DB_CONFIG = {
    "dbname": "price_comparison",
    "user": "postgres",
    "password": os.environ.get("PGPASSWORD", "Kaif@123"),
    "host": "localhost",
    "port": 5432
}


class DatasetImporter:
    def __init__(self, conn):
        self.conn = conn
        self.cursor = conn.cursor(cursor_factory=RealDictCursor)
        self.report = {}
        
        # In-memory FK sets populated during run
        self.city_ids = set()
        self.area_ids = set()
        self.category_ids = set()
        self.subcategory_ids = set()
        self.provider_ids = set()
        self.provider_location_ids = set()
        self.product_service_ids = set()
        self.listing_ids = set()
        self.price_type_ids = set()

    def log_result(self, table_name, total, inserted, updated, skipped, rejected):
        self.report[table_name] = {
            "total_csv_rows": total,
            "inserted": inserted,
            "updated": updated,
            "skipped": skipped,
            "rejected_count": len(rejected),
            "rejected_details": rejected
        }

    def load_existing_keys(self):
        self.cursor.execute("SELECT id FROM cities")
        self.city_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM price_types")
        self.price_type_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM areas")
        self.area_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM categories")
        self.category_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM subcategories")
        self.subcategory_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM providers")
        self.provider_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM provider_locations")
        self.provider_location_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM products_and_services")
        self.product_service_ids = {r["id"] for r in self.cursor.fetchall()}

        self.cursor.execute("SELECT id FROM listings")
        self.listing_ids = {r["id"] for r in self.cursor.fetchall()}

    def import_areas(self, file_path):
        table = "areas"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            print(f"[!] Warning: {file_path} not found.")
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    area_id = int(row["id"])
                    city_id = int(row["city_id"])
                    name = row["name"].strip()
                    pincode = row.get("pincode", "").strip()
                    lat = float(row["latitude"])
                    lon = float(row["longitude"])
                    is_active = row.get("is_active", "t").lower() in ("t", "true", "1")

                    # Validations
                    if not name:
                        rejected.append({"row": row_num, "reason": "Empty area name"})
                        continue
                    if city_id not in self.city_ids:
                        rejected.append({"row": row_num, "reason": f"Invalid city_id {city_id}"})
                        continue
                    if not (BENGALURU_LAT_MIN <= lat <= BENGALURU_LAT_MAX and BENGALURU_LON_MIN <= lon <= BENGALURU_LON_MAX):
                        rejected.append({"row": row_num, "reason": f"Coordinates ({lat}, {lon}) outside Bengaluru boundary"})
                        continue

                    # Check existing
                    self.cursor.execute("SELECT name, pincode, latitude, longitude, is_active FROM areas WHERE id = %s", (area_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if (existing["name"] == name and existing["pincode"] == pincode and
                                float(existing["latitude"]) == lat and float(existing["longitude"]) == lon and
                                existing["is_active"] == is_active):
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE areas 
                                SET name = %s, pincode = %s, latitude = %s, longitude = %s,
                                    centroid = ST_SetSRID(ST_MakePoint(%s, %s), 4326),
                                    is_active = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (name, pincode, lat, lon, lon, lat, is_active, area_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO areas (id, city_id, name, district, pincode, latitude, longitude, centroid, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, 'Bengaluru Urban', %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s), 4326), %s, NOW(), NOW())
                        """, (area_id, city_id, name, pincode, lat, lon, lon, lat, is_active))
                        inserted += 1

                    self.area_ids.add(area_id)

                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_categories(self, file_path):
        table = "categories"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    cat_id = int(row["id"])
                    name = row["name"].strip()
                    slug = row["slug"].strip()
                    desc = row.get("description", "").strip()
                    icon_key = row.get("icon_key", "").strip()
                    order = int(row.get("display_order", 0))
                    is_active = row.get("is_active", "t").lower() in ("t", "true", "1")

                    if not name or not slug:
                        rejected.append({"row": row_num, "reason": "Missing category name or slug"})
                        continue

                    self.cursor.execute("SELECT name, slug, display_order, is_active FROM categories WHERE id = %s", (cat_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if existing["name"] == name and existing["slug"] == slug and existing["display_order"] == order:
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE categories 
                                SET name = %s, slug = %s, description = %s, icon_key = %s, display_order = %s, is_active = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (name, slug, desc, icon_key, order, is_active, cat_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO categories (id, name, slug, description, icon_key, display_order, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                        """, (cat_id, name, slug, desc, icon_key, order, is_active))
                        inserted += 1

                    self.category_ids.add(cat_id)
                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_subcategories(self, file_path):
        table = "subcategories"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    sub_id = int(row["id"])
                    cat_id = int(row["category_id"])
                    name = row["name"].strip()
                    slug = row["slug"].strip()
                    item_type = row.get("item_type", "product").strip()
                    order = int(row.get("display_order", 0))
                    is_active = row.get("is_active", "t").lower() in ("t", "true", "1")

                    if not name or not slug:
                        rejected.append({"row": row_num, "reason": "Missing name or slug"})
                        continue
                    if cat_id not in self.category_ids:
                        rejected.append({"row": row_num, "reason": f"Category ID {cat_id} does not exist"})
                        continue

                    self.cursor.execute("SELECT name, slug, category_id, item_type FROM subcategories WHERE id = %s", (sub_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if existing["name"] == name and existing["category_id"] == cat_id and existing["item_type"] == item_type:
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE subcategories 
                                SET category_id = %s, name = %s, slug = %s, item_type = %s, display_order = %s, is_active = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (cat_id, name, slug, item_type, order, is_active, sub_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO subcategories (id, category_id, name, slug, item_type, display_order, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                        """, (sub_id, cat_id, name, slug, item_type, order, is_active))
                        inserted += 1

                    self.subcategory_ids.add(sub_id)
                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_providers(self, file_path):
        table = "providers"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    prov_id = int(row["id"])
                    name = row["name"].strip()
                    legal_name = row.get("legal_name", "").strip() or name
                    b_type = row.get("business_type", "store").strip()
                    phone = row.get("phone", "").strip()
                    email = row.get("email", "").strip()
                    website = row.get("website", "").strip()
                    is_verified = row.get("is_verified", "f").lower() in ("t", "true", "1")
                    is_active = row.get("is_active", "t").lower() in ("t", "true", "1")

                    if not name:
                        rejected.append({"row": row_num, "reason": "Provider name is required"})
                        continue

                    self.cursor.execute("SELECT name, phone, email, is_verified FROM providers WHERE id = %s", (prov_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if existing["name"] == name and existing["phone"] == phone and existing["is_verified"] == is_verified:
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE providers 
                                SET name = %s, legal_name = %s, business_type = %s, phone = %s, email = %s, website = %s, is_verified = %s, is_active = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (name, legal_name, b_type, phone, email, website, is_verified, is_active, prov_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO providers (id, name, legal_name, business_type, phone, email, website, is_verified, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                        """, (prov_id, name, legal_name, b_type, phone, email, website, is_verified, is_active))
                        inserted += 1

                    self.provider_ids.add(prov_id)
                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_provider_locations(self, file_path):
        table = "provider_locations"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    loc_id = int(row["id"])
                    prov_id = int(row["provider_id"])
                    area_id = int(row["area_id"])
                    branch = row.get("branch_name", "").strip()
                    address = row.get("address_line", "").strip()
                    lat = float(row["latitude"])
                    lon = float(row["longitude"])
                    phone = row.get("contact_phone", "").strip()
                    is_primary = row.get("is_primary", "t").lower() in ("t", "true", "1")
                    is_active = row.get("is_active", "t").lower() in ("t", "true", "1")

                    if prov_id not in self.provider_ids:
                        rejected.append({"row": row_num, "reason": f"Provider ID {prov_id} not found"})
                        continue
                    if area_id not in self.area_ids:
                        rejected.append({"row": row_num, "reason": f"Area ID {area_id} not found"})
                        continue
                    if not (BENGALURU_LAT_MIN <= lat <= BENGALURU_LAT_MAX and BENGALURU_LON_MIN <= lon <= BENGALURU_LON_MAX):
                        rejected.append({"row": row_num, "reason": f"Coordinates ({lat}, {lon}) outside Bengaluru boundary"})
                        continue

                    self.cursor.execute("SELECT provider_id, area_id, latitude, longitude FROM provider_locations WHERE id = %s", (loc_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if existing["provider_id"] == prov_id and existing["area_id"] == area_id and float(existing["latitude"]) == lat:
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE provider_locations
                                SET provider_id = %s, area_id = %s, branch_name = %s, address_line = %s,
                                    latitude = %s, longitude = %s, location = ST_SetSRID(ST_MakePoint(%s, %s), 4326),
                                    contact_phone = %s, is_primary = %s, is_active = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (prov_id, area_id, branch, address, lat, lon, lon, lat, phone, is_primary, is_active, loc_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO provider_locations (id, provider_id, area_id, branch_name, address_line, latitude, longitude, location, contact_phone, is_primary, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, ST_SetSRID(ST_MakePoint(%s, %s), 4326), %s, %s, %s, NOW(), NOW())
                        """, (loc_id, prov_id, area_id, branch, address, lat, lon, lon, lat, phone, is_primary, is_active))
                        inserted += 1

                    self.provider_location_ids.add(loc_id)
                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_products_and_services(self, file_path):
        table = "products_and_services"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    prod_id = int(row["id"])
                    sub_id = int(row["subcategory_id"])
                    name = row["name"].strip()
                    code = row.get("identifier_code", "").strip()
                    brand = row.get("brand_or_manufacturer", "").strip()
                    desc = row.get("description", "").strip()
                    is_active = row.get("is_active", "t").lower() in ("t", "true", "1")

                    if not name:
                        rejected.append({"row": row_num, "reason": "Name is mandatory"})
                        continue
                    if sub_id not in self.subcategory_ids:
                        rejected.append({"row": row_num, "reason": f"Subcategory ID {sub_id} not found"})
                        continue

                    self.cursor.execute("SELECT name, subcategory_id, identifier_code FROM products_and_services WHERE id = %s", (prod_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if existing["name"] == name and existing["subcategory_id"] == sub_id:
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE products_and_services
                                SET subcategory_id = %s, name = %s, identifier_code = %s, brand_or_manufacturer = %s, description = %s, is_active = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (sub_id, name, code, brand, desc, is_active, prod_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO products_and_services (id, subcategory_id, name, identifier_code, brand_or_manufacturer, description, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, NOW(), NOW())
                        """, (prod_id, sub_id, name, code, brand, desc, is_active))
                        inserted += 1

                    self.product_service_ids.add(prod_id)
                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_product_service_attributes(self, file_path):
        table = "product_service_attributes"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    attr_id = int(row["id"])
                    prod_id = int(row["product_service_id"])
                    attrs_raw = row.get("attributes", "{}")
                    comp_raw = row.get("comparison_key_values", "{}")

                    if prod_id not in self.product_service_ids:
                        rejected.append({"row": row_num, "reason": f"ProductService ID {prod_id} not found"})
                        continue

                    attrs_json = json.loads(attrs_raw) if isinstance(attrs_raw, str) else attrs_raw
                    comp_json = json.loads(comp_raw) if isinstance(comp_raw, str) else comp_raw

                    self.cursor.execute("SELECT product_service_id FROM product_service_attributes WHERE id = %s", (attr_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        self.cursor.execute("""
                            UPDATE product_service_attributes
                            SET product_service_id = %s, attributes = %s, comparison_key_values = %s, updated_at = NOW()
                            WHERE id = %s
                        """, (prod_id, json.dumps(attrs_json), json.dumps(comp_json), attr_id))
                        updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO product_service_attributes (id, product_service_id, attributes, comparison_key_values, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, NOW(), NOW())
                        """, (attr_id, prod_id, json.dumps(attrs_json), json.dumps(comp_json)))
                        inserted += 1

                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_listings(self, file_path):
        table = "listings"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    listing_id = int(row["id"])
                    user_id = int(row["user_id"])
                    prov_id = int(row["provider_id"]) if row.get("provider_id") else None
                    loc_id = int(row["provider_location_id"]) if row.get("provider_location_id") else None
                    ps_id = int(row["product_service_id"]) if row.get("product_service_id") else None
                    title = row.get("custom_title", "").strip()
                    price_type_id = int(row["price_type_id"]) if row.get("price_type_id") else 1
                    status = row.get("status", "active").strip().lower()
                    source_type = row.get("source_type", "PUBLICLY_SOURCED").strip()
                    is_price_avail = row.get("is_price_available", "t").lower() in ("t", "true", "1")
                    notes = row.get("notes", "").strip()

                    price_val = None
                    if is_price_avail and row.get("price_amount"):
                        try:
                            price_val = Decimal(row["price_amount"])
                            if price_val < 0:
                                rejected.append({"row": row_num, "reason": f"Negative price {price_val}"})
                                continue
                        except InvalidOperation:
                            rejected.append({"row": row_num, "reason": f"Invalid price format '{row['price_amount']}'"})
                            continue

                    # FK validations
                    if prov_id and prov_id not in self.provider_ids:
                        rejected.append({"row": row_num, "reason": f"Provider ID {prov_id} not found"})
                        continue
                    if loc_id and loc_id not in self.provider_location_ids:
                        rejected.append({"row": row_num, "reason": f"Location ID {loc_id} not found"})
                        continue
                    if ps_id and ps_id not in self.product_service_ids:
                        rejected.append({"row": row_num, "reason": f"ProductService ID {ps_id} not found"})
                        continue

                    self.cursor.execute("SELECT custom_title, price_amount, status, is_price_available FROM listings WHERE id = %s", (listing_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        if existing["status"] == status and existing["is_price_available"] == is_price_avail:
                            skipped += 1
                        else:
                            self.cursor.execute("""
                                UPDATE listings
                                SET custom_title = %s, price_amount = %s, status = %s, moderation_status = %s,
                                    is_price_available = %s, source_type = %s, notes = %s, updated_at = NOW()
                                WHERE id = %s
                            """, (title, price_val, status, "approved" if status == "active" else status, is_price_avail, source_type, notes, listing_id))
                            updated += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO listings (id, user_id, provider_id, provider_location_id, product_service_id, custom_title, price_amount, price_type_id, currency, moderation_status, status, source_type, is_price_available, notes, is_available, is_active, created_at, updated_at)
                            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, 'INR', %s, %s, %s, %s, %s, true, true, NOW(), NOW())
                        """, (listing_id, user_id, prov_id, loc_id, ps_id, title, price_val, price_type_id, "approved" if status == "active" else status, status, source_type, is_price_avail, notes))
                        inserted += 1

                    self.listing_ids.add(listing_id)
                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def import_price_observations(self, file_path):
        table = "price_observations"
        total = inserted = updated = skipped = 0
        rejected = []

        if not os.path.exists(file_path):
            return

        with open(file_path, "r", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row_num, row in enumerate(reader, start=2):
                total += 1
                try:
                    obs_id = int(row["id"])
                    listing_id = int(row["listing_id"])
                    pt_id = int(row["price_type_id"]) if row.get("price_type_id") else 1
                    source_type = row.get("source_type", "PUBLICLY_SOURCED").strip()
                    v_status = row.get("verification_status", "unverified").strip()

                    price_val = None
                    if row.get("price_amount"):
                        try:
                            price_val = Decimal(row["price_amount"])
                        except InvalidOperation:
                            rejected.append({"row": row_num, "reason": f"Invalid price {row['price_amount']}"})
                            continue

                    if listing_id not in self.listing_ids:
                        rejected.append({"row": row_num, "reason": f"Listing ID {listing_id} not found"})
                        continue

                    self.cursor.execute("SELECT listing_id, price_amount FROM price_observations WHERE id = %s", (obs_id,))
                    existing = self.cursor.fetchone()

                    if existing:
                        skipped += 1
                    else:
                        self.cursor.execute("""
                            INSERT INTO price_observations (id, listing_id, price_type_id, price_amount, currency, source_type, submission_date, effective_date, verification_status, created_at)
                            VALUES (%s, %s, %s, %s, 'INR', %s, NOW(), NOW(), %s, NOW())
                        """, (obs_id, listing_id, pt_id, price_val, source_type, v_status))
                        inserted += 1

                except Exception as e:
                    rejected.append({"row": row_num, "reason": str(e)})

        self.log_result(table, total, inserted, updated, skipped, rejected)

    def run_all(self):
        print("=================================================================")
        print("PricePulse - Bengaluru Master Dataset CSV Import & Validation")
        print("=================================================================")
        self.load_existing_keys()

        self.import_areas(os.path.join(CSV_DIR, "areas.csv"))
        self.import_categories(os.path.join(CSV_DIR, "categories.csv"))
        self.import_subcategories(os.path.join(CSV_DIR, "subcategories.csv"))
        self.import_providers(os.path.join(CSV_DIR, "providers.csv"))
        self.import_provider_locations(os.path.join(CSV_DIR, "provider_locations.csv"))
        self.import_products_and_services(os.path.join(CSV_DIR, "products_and_services.csv"))
        self.import_product_service_attributes(os.path.join(CSV_DIR, "product_service_attributes.csv"))
        self.import_listings(os.path.join(CSV_DIR, "listings.csv"))
        self.import_price_observations(os.path.join(CSV_DIR, "price_observations.csv"))

        self.conn.commit()

        print("\n--- IMPORT SUMMARY REPORT ---")
        for table, stats in self.report.items():
            print(f"Table: {table:<28} | Rows: {stats['total_csv_rows']:<3} | Inserted: {stats['inserted']:<3} | Updated: {stats['updated']:<3} | Skipped: {stats['skipped']:<3} | Rejected: {stats['rejected_count']:<3}")
            if stats["rejected_details"]:
                for r in stats["rejected_details"][:3]:
                    print(f"   -> [Row {r['row']}] {r['reason']}")
        print("=================================================================\n")


if __name__ == "__main__":
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        importer = DatasetImporter(conn)
        importer.run_all()
        conn.close()
    except Exception as ex:
        print(f"[FATAL ERROR] {ex}", file=sys.stderr)
        sys.exit(1)

