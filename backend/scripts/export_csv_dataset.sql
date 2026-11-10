-- Export areas
\copy (SELECT id, city_id, name, district, pincode, latitude, longitude, is_active, created_at, updated_at FROM areas ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/areas.csv' WITH CSV HEADER;

-- Export categories
\copy (SELECT id, name, slug, description, icon_key, display_order, is_active, created_at, updated_at FROM categories ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/categories.csv' WITH CSV HEADER;

-- Export subcategories
\copy (SELECT id, category_id, name, slug, description, item_type, display_order, is_active, created_at, updated_at FROM subcategories ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/subcategories.csv' WITH CSV HEADER;

-- Export providers
\copy (SELECT id, name, legal_name, business_type, phone, email, website, is_verified, is_active, created_at, updated_at FROM providers ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/providers.csv' WITH CSV HEADER;

-- Export provider_locations
\copy (SELECT id, provider_id, area_id, branch_name, address_line, latitude, longitude, contact_phone, is_primary, is_active, created_at, updated_at FROM provider_locations ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/provider_locations.csv' WITH CSV HEADER;

-- Export products_and_services
\copy (SELECT id, subcategory_id, name, identifier_code, brand_or_manufacturer, description, is_active, created_at, updated_at FROM products_and_services ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/products_and_services.csv' WITH CSV HEADER;

-- Export product_service_attributes
\copy (SELECT id, product_service_id, attributes, comparison_key_values, created_at, updated_at FROM product_service_attributes ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/product_service_attributes.csv' WITH CSV HEADER;

-- Export listings
\copy (SELECT id, user_id, provider_id, provider_location_id, product_service_id, custom_title, price_amount, price_type_id, currency, moderation_status, status, rejection_reason, source_type, is_price_available, evidence_reference, notes, is_available, is_active, created_at, updated_at FROM listings ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/listings.csv' WITH CSV HEADER;

-- Export price_observations
\copy (SELECT id, listing_id, price_type_id, price_amount, currency, source_type, submission_date, effective_date, expiry_date, verification_status, evidence_reference, notes, created_at FROM price_observations ORDER BY id) TO 'c:/Users/kaifi/Downloads/pricepulse/pricepulse/backend/data/csv/price_observations.csv' WITH CSV HEADER;

