-- ============================================================================
-- PRICEPULSE SCHEMA UPGRADE FOR PHASES 5 & 6
-- Ensures price types, source types, and nullable prices for unavailable prices.
-- ============================================================================

-- 1. Update source types on price_observations and listings
ALTER TABLE price_observations DROP CONSTRAINT IF EXISTS price_observations_source_type_check;
ALTER TABLE price_observations ADD CONSTRAINT price_observations_source_type_check CHECK (
    source_type IN (
        'VERIFIED_REAL', 
        'PROVIDER_SUBMITTED', 
        'USER_SUBMITTED', 
        'PUBLICLY_SOURCED', 
        'DEMO_DATA',
        'user_submitted',
        'provider_submitted',
        'publicly_sourced',
        'authorized_external_feed'
    )
);

-- Allow nullable price_amount when provider is known but price is unlisted/unavailable
ALTER TABLE listings ALTER COLUMN price_amount DROP NOT NULL;
ALTER TABLE listings DROP CONSTRAINT IF EXISTS listings_price_amount_check;
ALTER TABLE listings ADD CONSTRAINT listings_price_amount_check CHECK (price_amount IS NULL OR price_amount >= 0);

ALTER TABLE listings ADD COLUMN IF NOT EXISTS source_type VARCHAR(40) NOT NULL DEFAULT 'PUBLICLY_SOURCED';
ALTER TABLE listings ADD COLUMN IF NOT EXISTS is_price_available BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE listings ADD COLUMN IF NOT EXISTS evidence_reference VARCHAR(500) NULL;

ALTER TABLE price_observations ALTER COLUMN price_amount DROP NOT NULL;
ALTER TABLE price_observations DROP CONSTRAINT IF EXISTS price_observations_price_amount_check;
ALTER TABLE price_observations ADD CONSTRAINT price_observations_price_amount_check CHECK (price_amount IS NULL OR price_amount >= 0);

-- Update verification_status check
ALTER TABLE price_observations DROP CONSTRAINT IF EXISTS price_observations_verification_status_check;
ALTER TABLE price_observations ADD CONSTRAINT price_observations_verification_status_check CHECK (
    verification_status IN ('verified', 'pending', 'rejected', 'expired', 'unverified')
);

-- 2. Ensure all Phase 6 Price Types are populated
INSERT INTO price_types (id, category_id, code, name, description, is_recurring)
VALUES
-- Hotels (Category 1)
(21, 1, 'hotel_per_night_tariff',    'Per-Night Room Tariff',        'Standard room charge per night', FALSE),
(22, 1, 'hotel_weekend_tariff',      'Weekend Room Tariff',          'Peak weekend nightly rate', FALSE),
(23, 1, 'hotel_additional_charge',    'Additional Lodging Charges',   'Extra bed, occupancy or resort fee', FALSE),

-- Electronics (Category 2)
(24, 2, 'electronics_retail_price',  'Retail Price (MOP/Store)',     'Standard counter or store shelf price', FALSE),
(25, 2, 'electronics_discounted_price','Discounted Price',           'Instant selling / promo price', FALSE),
(26, 2, 'electronics_offer_price',   'Special Bundle Offer Price',   'Price after cashback, card, or exchange', FALSE),

-- Automotive (Category 3)
(27, 3, 'auto_ex_showroom_price',    'Ex-Showroom Price',            'Base vehicle cost excluding RTO & insurance', FALSE),
(28, 3, 'auto_on_road_price',        'On-Road Price',                'Total on-road registration & insurance cost', FALSE),
(29, 3, 'auto_service_charge',       'Periodic Service Charge',      'Standard periodic maintenance labor & fee', FALSE),
(30, 3, 'auto_spare_part_price',     'Spare Part Price',             'OEM component replacement cost', FALSE),

-- Healthcare (Category 4)
(31, 4, 'health_consultation_fee',   'Doctor Consultation Fee',      'Physician outpatient consultation fee', FALSE),
(32, 4, 'health_diagnostic_test_fee','Diagnostic Test Fee',          'Standard pathology or radiology test charge', FALSE),
(33, 4, 'health_procedure_fee',      'Clinical Procedure Fee',       'Dental, outpatient, or surgical procedure fee', FALSE),
(34, 4, 'health_package_price',      'Health Checkup Package Price', 'Consolidated multi-parameter preventive package', FALSE),

-- Education (Category 5)
(35, 5, 'edu_admission_fee',         'Admission / Registration Fee', 'One-time admission or registration fee', FALSE),
(36, 5, 'edu_tuition_fee',           'Tuition / Term Fee',           'Term or academic instruction tuition fee', FALSE),
(37, 5, 'edu_annual_fee',            'Annual Composite School Fee',  'Annual comprehensive academic and activity fee', FALSE),
(38, 5, 'edu_additional_charge',     'Course Materials / Lab Fee',   'Books, uniform, lab or assessment surcharge', FALSE),

-- Real Estate (Category 6)
(39, 6, 're_sale_price',             'Total Sale Price',             'Total property acquisition price', FALSE),
(40, 6, 're_monthly_rent',           'Monthly Rent',                 'Recurring monthly residential / commercial rent', TRUE),
(41, 6, 're_price_per_sqft',         'Price Per Square Foot',        'Calculated unit price per sq ft built-up area', FALSE),
(42, 6, 're_security_deposit',       'Refundable Security Deposit',  'Standard lease security deposit', FALSE),

-- Restaurants (Category 7)
(43, 7, 'food_menu_price',           'A La Carte Menu Price',        'Individual dish / beverage menu price', FALSE),
(44, 7, 'food_buffet_price',         'Buffet Session Price',         'Unlimited dining buffet rate per person', FALSE),
(45, 7, 'food_package_price',        'Meal Package / Subscription',  'Consolidated meal box or subscription package', FALSE),

-- Home Services (Category 8)
(46, 8, 'service_inspection_charge', 'Inspection / Visitation Charge','Technician diagnostic inspection fee', FALSE),
(47, 8, 'service_labour_charge',     'Labour Charge',                'Standard repair or installation labor fee', FALSE),
(48, 8, 'service_package_price',     'Service Package Price',        'Deep cleaning, AC overhaul, or turnkey service', FALSE),
(49, 8, 'service_material_cost',     'Material Cost',                'Replacement pipes, parts, or wire costs', FALSE),

-- Fitness (Category 9)
(50, 9, 'fitness_monthly_membership','Monthly Membership Fee',       'Single month fitness / gym pass', TRUE),
(51, 9, 'fitness_quarterly_membership','Quarterly Membership Fee',   '3-month fitness membership package', FALSE),
(52, 9, 'fitness_annual_membership', 'Annual Membership Fee',        '12-month full gym / studio pass', FALSE),
(53, 9, 'fitness_pt_fee',            'Personal Training Fee',        'Dedicated trainer fee per month or session batch', FALSE),

-- Shopping (Category 10)
(54, 10, 'shop_unit_price',          'Retail Unit Price',            'Standard single item shelf price', FALSE),
(55, 10, 'shop_pack_price',          'Pack / Bundle Price',          'Multi-pack, family pack, or carton price', FALSE),
(56, 10, 'shop_price_per_std_qty',   'Price per Standard Quantity',  'Normalized price (e.g. per 100g or 1 kg)', FALSE)
ON CONFLICT (code) DO NOTHING;

SELECT setval('price_types_id_seq', (SELECT COALESCE(MAX(id), 1) FROM price_types));

