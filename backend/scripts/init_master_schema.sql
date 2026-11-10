-- ============================================================================
-- PRICEPULSE ENTERPRISE MASTER DATABASE SCHEMA & SEED DATA
-- Target: PostgreSQL 18 with PostGIS 3.6
-- Database: price_comparison
-- ============================================================================

CREATE EXTENSION IF NOT EXISTS postgis;

-- ----------------------------------------------------------------------------
-- 1. GEOGRAPHIC & ADMINISTRATIVE MASTERS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL DEFAULT 'Bengaluru Urban',
    state VARCHAR(100) NOT NULL DEFAULT 'Karnataka',
    country VARCHAR(100) NOT NULL DEFAULT 'India',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT uq_city_name_state UNIQUE (name, state)
);

CREATE TABLE IF NOT EXISTS areas (
    id BIGSERIAL PRIMARY KEY,
    city_id BIGINT NOT NULL REFERENCES cities(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    district VARCHAR(100) NOT NULL DEFAULT 'Bengaluru Urban',
    pincode VARCHAR(10) NULL,
    latitude NUMERIC(10, 6) NOT NULL,
    longitude NUMERIC(10, 6) NOT NULL,
    centroid GEOGRAPHY(Point, 4326) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT uq_area_city_name UNIQUE (city_id, name)
);

CREATE INDEX IF NOT EXISTS idx_areas_city_id ON areas(city_id);
CREATE INDEX IF NOT EXISTS idx_areas_centroid ON areas USING GIST (centroid);

-- ----------------------------------------------------------------------------
-- 2. TAXONOMY & CLASSIFICATION MASTERS (10 Main Categories & 41 Subcategories)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT NULL,
    icon_key VARCHAR(50) NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_categories_display_order ON categories(display_order);

CREATE TABLE IF NOT EXISTS subcategories (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    description TEXT NULL,
    item_type VARCHAR(30) NOT NULL CHECK (item_type IN ('product', 'service', 'rental', 'package')),
    attribute_schema JSONB NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT uq_subcategory_category_name UNIQUE (category_id, name)
);

CREATE INDEX IF NOT EXISTS idx_subcategories_category_id ON subcategories(category_id);
CREATE INDEX IF NOT EXISTS idx_subcategories_slug ON subcategories(slug);

CREATE TABLE IF NOT EXISTS price_types (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    code VARCHAR(60) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    is_recurring BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_price_types_category_id ON price_types(category_id);

-- ----------------------------------------------------------------------------
-- 3. USER MANAGEMENT & AUTHENTICATION
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'user' CHECK (role IN ('user', 'admin')),
    phone VARCHAR(30) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

-- ----------------------------------------------------------------------------
-- 4. PROVIDERS & LOCATIONS
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    legal_name VARCHAR(250) NULL,
    business_type VARCHAR(50) NOT NULL DEFAULT 'retail' CHECK (
        business_type IN ('retail', 'hospital', 'clinic', 'hotel', 'pg_hostel', 
                          'school', 'institute', 'gym', 'service_agency', 
                          'real_estate_agency', 'independent_professional', 'restaurant')
    ),
    phone VARCHAR(30) NULL,
    email VARCHAR(150) NULL,
    website VARCHAR(255) NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_providers_name ON providers(name);

CREATE TABLE IF NOT EXISTS provider_locations (
    id BIGSERIAL PRIMARY KEY,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    area_id BIGINT NOT NULL REFERENCES areas(id) ON DELETE RESTRICT,
    branch_name VARCHAR(150) NULL,
    address_line TEXT NOT NULL,
    latitude NUMERIC(10, 6) NOT NULL,
    longitude NUMERIC(10, 6) NOT NULL,
    location GEOGRAPHY(Point, 4326) NOT NULL,
    contact_phone VARCHAR(30) NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_provider_locations_provider_id ON provider_locations(provider_id);
CREATE INDEX IF NOT EXISTS idx_provider_locations_area_id ON provider_locations(area_id);
CREATE INDEX IF NOT EXISTS idx_provider_locations_gis ON provider_locations USING GIST (location);

-- ----------------------------------------------------------------------------
-- 5. PRODUCTS & SERVICES CATALOG
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products_and_services (
    id BIGSERIAL PRIMARY KEY,
    subcategory_id BIGINT NOT NULL REFERENCES subcategories(id) ON DELETE RESTRICT,
    name VARCHAR(255) NOT NULL,
    identifier_code VARCHAR(100) NULL,
    brand_or_manufacturer VARCHAR(150) NULL,
    description TEXT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_pas_subcategory_id ON products_and_services(subcategory_id);
CREATE INDEX IF NOT EXISTS idx_pas_name ON products_and_services(name);

CREATE TABLE IF NOT EXISTS product_service_attributes (
    id BIGSERIAL PRIMARY KEY,
    product_service_id BIGINT NOT NULL UNIQUE REFERENCES products_and_services(id) ON DELETE CASCADE,
    attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    comparison_key_values JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_psa_attributes_gin ON product_service_attributes USING GIN (attributes);
CREATE INDEX IF NOT EXISTS idx_psa_comparison_gin ON product_service_attributes USING GIN (comparison_key_values);

-- ----------------------------------------------------------------------------
-- 6. USER-CONTROLLED LISTINGS & MANAGEMENT
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS listings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL REFERENCES users(id) ON DELETE SET NULL,
    provider_id BIGINT NOT NULL REFERENCES providers(id) ON DELETE CASCADE,
    provider_location_id BIGINT NOT NULL REFERENCES provider_locations(id) ON DELETE CASCADE,
    product_service_id BIGINT NOT NULL REFERENCES products_and_services(id) ON DELETE RESTRICT,
    custom_title VARCHAR(255) NULL,
    price_amount NUMERIC(14, 2) NOT NULL CHECK (price_amount >= 0),
    price_type_id BIGINT NOT NULL REFERENCES price_types(id) ON DELETE RESTRICT,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    moderation_status VARCHAR(20) NOT NULL DEFAULT 'approved' CHECK (moderation_status IN ('pending', 'approved', 'rejected')),
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    notes TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_listings_user ON listings(user_id);
CREATE INDEX IF NOT EXISTS idx_listings_location ON listings(provider_location_id);
CREATE INDEX IF NOT EXISTS idx_listings_product ON listings(product_service_id);
CREATE INDEX IF NOT EXISTS idx_listings_moderation ON listings(moderation_status);

-- ----------------------------------------------------------------------------
-- 7. PRICE OBSERVATIONS (Immutable Audit Trail)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS price_observations (
    id BIGSERIAL PRIMARY KEY,
    listing_id BIGINT NOT NULL REFERENCES listings(id) ON DELETE CASCADE,
    price_type_id BIGINT NOT NULL REFERENCES price_types(id) ON DELETE RESTRICT,
    price_amount NUMERIC(14, 2) NOT NULL CHECK (price_amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    source_type VARCHAR(40) NOT NULL DEFAULT 'user_submitted' CHECK (
        source_type IN ('provider_submitted', 'user_submitted', 'publicly_sourced', 'authorized_external_feed')
    ),
    submission_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    effective_date TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    expiry_date TIMESTAMPTZ NULL,
    verification_status VARCHAR(20) NOT NULL DEFAULT 'verified' CHECK (
        verification_status IN ('pending', 'verified', 'rejected', 'expired')
    ),
    evidence_reference VARCHAR(500) NULL,
    notes TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_price_obs_listing_id ON price_observations(listing_id);
CREATE INDEX IF NOT EXISTS idx_price_obs_effective_date ON price_observations(effective_date DESC);

-- ----------------------------------------------------------------------------
-- 8. COMPATIBILITY VIEWS
-- ----------------------------------------------------------------------------
CREATE OR REPLACE VIEW stores_view AS
SELECT 
    pl.id,
    p.name,
    pl.address_line AS address,
    pl.location,
    pl.created_at
FROM provider_locations pl
JOIN providers p ON pl.provider_id = p.id;

CREATE OR REPLACE VIEW products_view AS
SELECT 
    pas.id,
    pas.name,
    c.name AS category,
    pas.created_at
FROM products_and_services pas
JOIN subcategories sc ON pas.subcategory_id = sc.id
JOIN categories c ON sc.category_id = c.id;

