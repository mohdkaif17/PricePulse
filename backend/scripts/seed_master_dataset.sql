-- ============================================================================
-- PRICEPULSE BENGALURU MASTER DATASET SEED SCRIPT
-- Populates 1 City, 15 Localities, 10 Categories, 41 Subcategories,
-- Price Types, Users, Providers, Provider Locations, Catalog Items, 
-- Category-Specific JSONB Attributes, Listings, and Price Observations.
-- ============================================================================

-- 1. CITY (Bengaluru)
INSERT INTO cities (id, name, district, state, country, is_active)
VALUES (1, 'Bengaluru', 'Bengaluru Urban', 'Karnataka', 'India', TRUE)
ON CONFLICT (name, state) DO UPDATE SET is_active = TRUE;

-- 2. 15 BENGALURU LOCALITIES (Exact Coordinates, PIN Codes, PostGIS Centroids)
INSERT INTO areas (id, city_id, name, district, pincode, latitude, longitude, centroid, is_active)
VALUES
(1,  1, 'Whitefield',      'Bengaluru Urban', '560066', 12.9698, 77.7500, ST_SetSRID(ST_MakePoint(77.7500, 12.9698), 4326), TRUE),
(2,  1, 'Marathahalli',    'Bengaluru Urban', '560037', 12.9591, 77.6974, ST_SetSRID(ST_MakePoint(77.6974, 12.9591), 4326), TRUE),
(3,  1, 'Electronic City', 'Bengaluru Urban', '560100', 12.8452, 77.6602, ST_SetSRID(ST_MakePoint(77.6602, 12.8452), 4326), TRUE),
(4,  1, 'Sarjapur Road',   'Bengaluru Urban', '560035', 12.9100, 77.6850, ST_SetSRID(ST_MakePoint(77.6850, 12.9100), 4326), TRUE),
(5,  1, 'Koramangala',     'Bengaluru Urban', '560034', 12.9352, 77.6245, ST_SetSRID(ST_MakePoint(77.6245, 12.9352), 4326), TRUE),
(6,  1, 'HSR Layout',      'Bengaluru Urban', '560102', 12.9121, 77.6446, ST_SetSRID(ST_MakePoint(77.6446, 12.9121), 4326), TRUE),
(7,  1, 'Indiranagar',     'Bengaluru Urban', '560038', 12.9784, 77.6408, ST_SetSRID(ST_MakePoint(77.6408, 12.9784), 4326), TRUE),
(8,  1, 'BTM Layout',      'Bengaluru Urban', '560068', 12.9166, 77.6101, ST_SetSRID(ST_MakePoint(77.6101, 12.9166), 4326), TRUE),
(9,  1, 'Jayanagar',       'Bengaluru Urban', '560041', 12.9308, 77.5838, ST_SetSRID(ST_MakePoint(77.5838, 12.9308), 4326), TRUE),
(10, 1, 'JP Nagar',        'Bengaluru Urban', '560078', 12.9063, 77.5857, ST_SetSRID(ST_MakePoint(77.5857, 12.9063), 4326), TRUE),
(11, 1, 'Hebbal',          'Bengaluru Urban', '560024', 13.0358, 77.5970, ST_SetSRID(ST_MakePoint(77.5970, 13.0358), 4326), TRUE),
(12, 1, 'Yelahanka',       'Bengaluru Urban', '560064', 13.1007, 77.5963, ST_SetSRID(ST_MakePoint(77.5963, 13.1007), 4326), TRUE),
(13, 1, 'Rajajinagar',     'Bengaluru Urban', '560010', 12.9982, 77.5530, ST_SetSRID(ST_MakePoint(77.5530, 12.9982), 4326), TRUE),
(14, 1, 'Malleshwaram',    'Bengaluru Urban', '560003', 13.0031, 77.5643, ST_SetSRID(ST_MakePoint(77.5643, 13.0031), 4326), TRUE),
(15, 1, 'Banashankari',    'Bengaluru Urban', '560050', 12.9255, 77.5468, ST_SetSRID(ST_MakePoint(77.5468, 12.9255), 4326), TRUE)
ON CONFLICT (city_id, name) DO UPDATE SET 
    pincode = EXCLUDED.pincode,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    centroid = EXCLUDED.centroid;

-- 3. 10 MAIN CATEGORIES
INSERT INTO categories (id, name, slug, description, icon_key, display_order, is_active)
VALUES
(1,  'Hotels & Accommodation',     'hotels-accommodation',     'Hotels, resorts, homestays, and PG hostels',        'ic_category_hotel',       1, TRUE),
(2,  'Electronics & Appliances',   'electronics-appliances',   'Gadgets, smartphones, laptops, TVs & appliances',  'ic_category_electronics', 2, TRUE),
(3,  'Automotive',                 'automotive',               'Cars, bikes, periodic servicing & spare parts',     'ic_category_vehicles',    3, TRUE),
(4,  'Healthcare',                 'healthcare',               'Doctors, diagnostic labs, clinics & health checks', 'ic_category_healthcare',  4, TRUE),
(5,  'Education',                  'education',                'Schools, competitive tuition & professional skilling','ic_category_education', 5, TRUE),
(6,  'Real Estate & Rentals',      'real-estate-rentals',      'Apartments, independent houses, PGs & offices',     'ic_category_real_estate', 6, TRUE),
(7,  'Restaurants & Food',         'restaurants-food',         'Dine-in restaurants, cafes, buffets & meal boxes',  'ic_category_restaurants', 7, TRUE),
(8,  'Home Services',              'home-services',            'Plumbing, electricians, AC repair & cleaning',      'ic_category_services',    8, TRUE),
(9,  'Fitness & Wellness',         'fitness-wellness',         'Gyms, personal trainers, yoga & fitness studios',   'ic_category_fitness',     9, TRUE),
(10, 'Shopping & Daily Essentials','shopping-daily-essentials','Groceries, packaged foods & household essentials',   'ic_category_shopping',   10, TRUE)
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, icon_key = EXCLUDED.icon_key;

-- 4. 41 SUBCATEGORIES
INSERT INTO subcategories (id, category_id, name, slug, item_type, display_order, is_active)
VALUES
-- 1. Hotels & Accommodation
(1,  1, 'Budget Hotels',              'hotels-budget',                'service', 1, TRUE),
(2,  1, 'Premium Hotels',             'hotels-premium',               'service', 2, TRUE),
(3,  1, 'Resorts',                    'hotels-resorts',               'service', 3, TRUE),
(4,  1, 'PG & Hostels',               'hotels-pg-hostels',            'rental',  4, TRUE),
-- 2. Electronics & Appliances
(5,  2, 'Smartphones',                'electronics-smartphones',      'product', 1, TRUE),
(6,  2, 'Laptops',                    'electronics-laptops',          'product', 2, TRUE),
(7,  2, 'Televisions',                'electronics-televisions',      'product', 3, TRUE),
(8,  2, 'Home Appliances',            'electronics-home-appliances',  'product', 4, TRUE),
(9,  2, 'Accessories',                'electronics-accessories',      'product', 5, TRUE),
-- 3. Automotive
(10, 3, 'Cars',                       'automotive-cars',              'product', 1, TRUE),
(11, 3, 'Two-Wheelers',               'automotive-two-wheelers',      'product', 2, TRUE),
(12, 3, 'Vehicle Servicing',          'automotive-servicing',         'service', 3, TRUE),
(13, 3, 'Spare Parts',                'automotive-spare-parts',       'product', 4, TRUE),
-- 4. Healthcare
(14, 4, 'Doctor Consultation',        'healthcare-consultation',      'service', 1, TRUE),
(15, 4, 'Diagnostic Tests',           'healthcare-diagnostic-tests',  'service', 2, TRUE),
(16, 4, 'Dental Treatments',          'healthcare-dental',            'service', 3, TRUE),
(17, 4, 'Health Checkup Packages',    'healthcare-checkup-packages',  'package', 4, TRUE),
-- 5. Education
(18, 5, 'School Fees',                'education-school-fees',        'service', 1, TRUE),
(19, 5, 'Coaching & Tuition',         'education-coaching',           'service', 2, TRUE),
(20, 5, 'Professional Courses',       'education-professional-courses','service',3, TRUE),
-- 6. Real Estate & Rentals
(21, 6, 'Apartments for Sale',        'realestate-apartments-sale',   'product', 1, TRUE),
(22, 6, 'Houses for Rent',            'realestate-houses-rent',       'rental',  2, TRUE),
(23, 6, 'PG Accommodation',           'realestate-pg-accommodation',  'rental',  3, TRUE),
(24, 6, 'Commercial Properties',      'realestate-commercial',        'rental',  4, TRUE),
-- 7. Restaurants & Food
(25, 7, 'Restaurants',                'food-restaurants',             'product', 1, TRUE),
(26, 7, 'Cafes',                      'food-cafes',                   'product', 2, TRUE),
(27, 7, 'Buffets',                    'food-buffets',                 'package', 3, TRUE),
(28, 7, 'Food Packages',              'food-packages',                'package', 4, TRUE),
-- 8. Home Services
(29, 8, 'Plumbing',                   'homeservices-plumbing',        'service', 1, TRUE),
(30, 8, 'Electrical Services',        'homeservices-electrical',      'service', 2, TRUE),
(31, 8, 'AC Servicing',               'homeservices-ac',              'service', 3, TRUE),
(32, 8, 'Cleaning & Pest Control',    'homeservices-cleaning-pest',   'service', 4, TRUE),
-- 9. Fitness & Wellness
(33, 9, 'Gym Memberships',            'fitness-gym-memberships',      'service', 1, TRUE),
(34, 9, 'Personal Training',          'fitness-personal-training',    'service', 2, TRUE),
(35, 9, 'Yoga & Fitness Classes',     'fitness-yoga-classes',         'service', 3, TRUE),
-- 10. Shopping & Daily Essentials
(36, 10, 'Groceries',                 'shopping-groceries',           'product', 1, TRUE),
(37, 10, 'Personal Care',             'shopping-personal-care',       'product', 2, TRUE),
(38, 10, 'Household Products',        'shopping-household',           'product', 3, TRUE)
ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, item_type = EXCLUDED.item_type;

-- 5. PRICE TYPES PER CATEGORY
INSERT INTO price_types (id, category_id, code, name, description, is_recurring)
VALUES
-- Hotels
(1,  1, 'room_tariff_standard',  'Per Night Tariff',           'Standard nightly tariff for lodging', FALSE),
(2,  1, 'monthly_rent_pg',       'Monthly Accommodation Rent', 'Monthly recurring fee for PG or hostel', TRUE),
-- Electronics
(3,  2, 'mrp',                   'Maximum Retail Price',       'Official manufacturer declared MRP', FALSE),
(4,  2, 'discounted_price',      'Selling / Deal Price',       'Best actual retail / selling price', FALSE),
-- Automotive
(5,  3, 'ex_showroom_price',     'Ex-Showroom Price',          'Base vehicle invoice price', FALSE),
(6,  3, 'service_charge',        'Periodic Service Fee',       'Labor + standard periodic check fee', FALSE),
-- Healthcare
(7,  4, 'consultation_fee',      'Doctor Consultation Fee',    'Outpatient consultation fee', FALSE),
(8,  4, 'diagnostic_test_fee',   'Diagnostic Investigation Fee','Standard test or laboratory charge', FALSE),
-- Education
(9,  5, 'annual_school_fee',     'Annual Academic Fee',        'Annual consolidated tuition & school fees', FALSE),
(10, 5, 'coaching_course_fee',   'Course Batch Fee',           'Total coaching program enrollment fee', FALSE),
-- Real Estate
(11, 6, 'monthly_rent',          'Monthly Rent',               'Recurring residential or commercial monthly rent', TRUE),
(12, 6, 'sale_price_total',      'Total Property Sale Price',  'Total property purchase price', FALSE),
-- Food
(13, 7, 'menu_price',            'Menu Price',                 'A la carte dish / beverage menu price', FALSE),
(14, 7, 'buffet_price',          'Per Person Buffet Tariff',   'Unlimited buffet cost per person', FALSE),
-- Home Services
(15, 8, 'labour_service_charge', 'Service / Labour Charge',    'Technician standard labor & repair fee', FALSE),
-- Fitness
(16, 9, 'monthly_gym_fee',       'Monthly Gym Membership',     'Standard 1-month fitness membership', TRUE),
(17, 9, 'annual_gym_fee',        'Annual Gym Membership',      'Full-year membership package', FALSE),
-- Shopping
(18, 10, 'packaged_mrp',         'Packaged Item Price',        'Retail price for standardized grocery pack', FALSE)
ON CONFLICT (code) DO NOTHING;

-- 6. INITIAL USERS (For User-Controlled Listing Management)
INSERT INTO users (id, username, email, full_name, role, phone, is_active)
VALUES
(1, 'admin', 'admin@pricepulse.in', 'PricePulse Administrator', 'admin', '+91-9876543210', TRUE),
(2, 'kaif_user', 'kaif@example.com', 'Kaif (Verified Contributor)', 'user', '+91-9888877771', TRUE)
ON CONFLICT (email) DO NOTHING;

-- 7. BENCHMARK PROVIDERS
INSERT INTO providers (id, name, legal_name, business_type, phone, email, is_verified, is_active)
VALUES
(1,  'Apex Electronics Koramangala',    'Apex Retail India Pvt Ltd',           'retail',       '080-25531001', 'contact@apexelectronics.com', TRUE, TRUE),
(2,  'Reliance Digital Indiranagar',    'Reliance Retail Limited',             'retail',       '080-41234567', 'indiranagar@reliancedigital.in', TRUE, TRUE),
(3,  'Prestige Lakeside Habitat',       'Prestige Estates Projects Ltd',       'real_estate_agency','080-25591080','sales@prestigeconstructions.com', TRUE, TRUE),
(4,  'Manipal Hospital Whitefield',     'Manipal Health Enterprises Pvt Ltd',  'hospital',     '080-61656666', 'info.whitefield@manipalhospitals.com', TRUE, TRUE),
(5,  'Apollo Clinic Koramangala',       'Apollo Health and Lifestyle Ltd',     'clinic',       '080-46688000', 'koramangala@apolloclinic.com', TRUE, TRUE),
(6,  'Pratham Motors Hebbal',           'Pratham Motors Pvt Ltd',              'retail',       '080-49123000', 'hebbal@prathammotors.com', TRUE, TRUE),
(7,  'Ginger Hotel Whitefield',         'Roots Corporation Limited (IHCL)',    'hotel',        '080-66663333', 'reservations.whitefield@gingerhotels.com', TRUE, TRUE),
(8,  'The Leela Palace Indiranagar',    'HLV Ltd',                             'hotel',        '080-25211234', 'reservations@theleela.com', TRUE, TRUE),
(9,  'Cult.fit HSR Layout',             'Curefit Healthcare Pvt Ltd',          'gym',          '080-68190000', 'support@cult.fit', TRUE, TRUE),
(10, 'Toit Brewpub Indiranagar',        'Toit Hospitality Pvt Ltd',            'restaurant',   '09019713388',  'cheers@toit.in', TRUE, TRUE),
(11, 'Barbeque Nation Koramangala',     'Barbeque Nation Hospitality Ltd',     'restaurant',   '080-60600000', 'feedback@barbequenation.com', TRUE, TRUE),
(12, 'Urban Company Bengaluru South',   'UrbanClap Technologies India Pvt Ltd','service_agency','080-45678900','help@urbancompany.com', TRUE, TRUE),
(13, 'National Public School Indiranagar','National Academy For Learning Trust','school',       '080-25280611', 'info@npsindiranagar.com', TRUE, TRUE),
(14, 'Nature''s Basket Koramangala',    'Spencer''s Retail Ltd',               'retail',       '080-41122334', 'customercare@naturesbasket.co.in', TRUE, TRUE),
(15, 'Stanza Living Electronic City',   'Dtwelve Spaces Pvt Ltd',              'pg_hostel',    '080-47184444', 'contact@stanzaliving.com', TRUE, TRUE)
ON CONFLICT DO NOTHING;

-- 8. PROVIDER LOCATIONS (Mapped to 15 Real Bengaluru Areas)
INSERT INTO provider_locations (id, provider_id, area_id, branch_name, address_line, latitude, longitude, location, contact_phone, is_primary, is_active)
VALUES
(1,  1,  5,  'Flagship Store',        '80 Feet Road, 4th Block, Koramangala',         12.9345, 77.6221, ST_SetSRID(ST_MakePoint(77.6221, 12.9345), 4326), '080-25531001', TRUE, TRUE),
(2,  2,  7,  '100ft Road Superstore', '100 Feet Rd, HAL 2nd Stage, Indiranagar',     12.9712, 77.6419, ST_SetSRID(ST_MakePoint(77.6419, 12.9712), 4326), '080-41234567', TRUE, TRUE),
(3,  3,  1,  'On-Site Sales Office',  'Varthur Main Road, Whitefield',                12.9580, 77.7420, ST_SetSRID(ST_MakePoint(77.7420, 12.9580), 4326), '080-25591080', TRUE, TRUE),
(4,  4,  1,  'Main Hospital Campus',  'EPIP Zone, Whitefield',                        12.9772, 77.7214, ST_SetSRID(ST_MakePoint(77.7214, 12.9772), 4326), '080-61656666', TRUE, TRUE),
(5,  5,  5,  'Koramangala Diagnostic','1st Block, Near Wipro Park, Koramangala',      12.9312, 77.6305, ST_SetSRID(ST_MakePoint(77.6305, 12.9312), 4326), '080-46688000', TRUE, TRUE),
(6,  6,  11, 'Hebbal Arena Showroom', 'Bellary Road, Hebbal Flyover Junction',      13.0380, 77.5950, ST_SetSRID(ST_MakePoint(77.5950, 13.0380), 4326), '080-49123000', TRUE, TRUE),
(7,  7,  1,  'Whitefield Hotel',      'ITPL Main Road, EPIP Zone, Whitefield',        12.9810, 77.7280, ST_SetSRID(ST_MakePoint(77.7280, 12.9810), 4326), '080-66663333', TRUE, TRUE),
(8,  8,  7,  'Heritage Palace Campus','23 Old Airport Road, Indiranagar Kodihalli',   12.9606, 77.6484, ST_SetSRID(ST_MakePoint(77.6484, 12.9606), 4326), '080-25211234', TRUE, TRUE),
(9,  9,  6,  'HSR Sector 2 Gym',      '27th Main Road, Sector 2, HSR Layout',         12.9115, 77.6512, ST_SetSRID(ST_MakePoint(77.6512, 12.9115), 4326), '080-68190000', TRUE, TRUE),
(10, 10, 7,  'Original Indiranagar',  '298 100 Feet Rd, Defense Colony, Indiranagar', 12.9791, 77.6406, ST_SetSRID(ST_MakePoint(77.6406, 12.9791), 4326), '09019713388',  TRUE, TRUE),
(11, 11, 5,  '80ft Road Outlet',      '80 Feet Road, 4th Block, Koramangala',         12.9360, 77.6255, ST_SetSRID(ST_MakePoint(77.6255, 12.9360), 4326), '080-60600000', TRUE, TRUE),
(12, 12, 6,  'HSR Service Hub',       '14th Main Rd, Sector 4, HSR Layout',           12.9140, 77.6380, ST_SetSRID(ST_MakePoint(77.6380, 12.9140), 4326), '080-45678900', TRUE, TRUE),
(13, 13, 7,  'Indiranagar Campus',    '12th A Main Road, HAL 2nd Stage, Indiranagar', 12.9735, 77.6450, ST_SetSRID(ST_MakePoint(77.6450, 12.9735), 4326), '080-25280611', TRUE, TRUE),
(14, 14, 5,  'Koramangala Supermarket','100 Feet Rd, 4th Block, Koramangala',        12.9330, 77.6270, ST_SetSRID(ST_MakePoint(77.6270, 12.9330), 4326), '080-41122334', TRUE, TRUE),
(15, 15, 3,  'Electronic City House', 'Phase 1, Neeladri Road, Electronic City',      12.8470, 77.6620, ST_SetSRID(ST_MakePoint(77.6620, 12.8470), 4326), '080-47184444', TRUE, TRUE)
ON CONFLICT DO NOTHING;

-- 9. PRODUCTS AND SERVICES CATALOG (Covering all 10 Main Categories & Approved Subcategories)
INSERT INTO products_and_services (id, subcategory_id, name, identifier_code, brand_or_manufacturer, description, is_active)
VALUES
-- Electronics
(1,  5,  'Samsung Galaxy S24 (128GB)',          'SM-S921BZKDINS', 'Samsung',        'Flagship 6.2-inch Dynamic AMOLED 2X, Snapdragon 8 Gen 3', TRUE),
(2,  5,  'Apple iPhone 15 (128GB)',             'A3090-128',      'Apple',          '6.1-inch Super Retina XDR, A16 Bionic, 48MP Main Camera', TRUE),
(3,  6,  'Apple MacBook Air M3 (16GB/512GB)',   'MC8K4HN/A',      'Apple',          '13.6-inch Liquid Retina Display, 8-core CPU, 10-core GPU', TRUE),
(4,  7,  'Sony Bravia 55-inch 4K Google TV',    'KD-55X74L',      'Sony',           '4K Ultra HD Smart LED Google TV with Dolby Audio', TRUE),
-- Real Estate
(5,  22, '2 BHK Apartment Monthly Rent',        'PRE-LAKE-2BHK',  'Prestige Estates','2 BHK Semi-Furnished, 1250 sqft, 2 Baths, Car Parking', TRUE),
(6,  23, 'Single Private Room PG Stay',         'STZ-EC-SNGL',    'Stanza Living',  'Private AC room with attached bath, 3 meals & WiFi included', TRUE),
(7,  21, '3 BHK Luxury Apartment for Sale',     'PRE-LAKE-3BHK-S', 'Prestige Estates','3 BHK 1780 sqft, Lake View, High Floor, Club House access', TRUE),
-- Automotive
(8,  10, 'Maruti Suzuki Swift VXi (Manual)',    'MS-SWIFT-VXI',   'Maruti Suzuki',  '1.2L Z-Series Petrol, 5-Speed Manual, Dual Airbags', TRUE),
(9,  11, 'Honda Activa 6G (Standard)',          'HND-ACT-6G-STD', 'Honda',          '109.51cc BS6 Engine, PGM-FI, Combi-Brake System', TRUE),
(10, 12, 'Comprehensive Car Periodic Service',  'PRT-SVC-10K',    'Pratham Motors', 'Engine oil, filter replacement, brake inspection, 40-point check', TRUE),
-- Healthcare
(11, 14, 'Cardiology Doctor Consultation',      'MNP-DOC-CARD',   'Manipal Hospital','Senior Consultant Cardiologist OP Consultation', TRUE),
(12, 15, 'Lipid Profile Blood Test',            'APL-LAB-LIPID',  'Apollo Clinic',  'Total Cholesterol, HDL, LDL, Triglycerides, VLDL automated assay', TRUE),
(13, 17, 'Comprehensive Master Health Checkup', 'MNP-MHC-PKG',    'Manipal Hospital','64 clinical parameters including ECG, Echo, CBC, USG Abdomen', TRUE),
-- Hotels & Accommodation
(14, 1,  'Standard AC Double Room (Per Night)', 'GNG-BLR-STD',    'Ginger Hotels',  'Queen bed, workspace, tea/coffee maker, ensuite bathroom', TRUE),
(15, 2,  'Royal Premier Suite with Breakfast',  'LEELA-ROYAL-STE','The Leela Palace','Luxury 800 sqft suite with balcony, access to Royal Club lounge', TRUE),
-- Fitness & Wellness
(16, 33, 'Cultpass Elite (12-Month Unlimited)', 'CULT-ELT-12M',   'Cult.fit',       'Unlimited access to all Cult centers, gym floors, and group classes', TRUE),
(17, 34, '1-on-1 Personal Training (12 Sessions)','CULT-PT-12S',  'Cult.fit',       'Dedicated certified personal fitness coach, diet & biometric plan', TRUE),
-- Restaurants & Food
(18, 27, 'Unlimited Weekend Barbeque Dinner',   'BBQ-WKD-DIN',    'Barbeque Nation','Live grill skewers on table, multi-cuisine buffet, dessert counter', TRUE),
(19, 25, 'Signature Tint-In-Brew Beer Pitcher', 'TOIT-PIT-BRW',   'Toit Brewpub',   'Freshly crafted 1.5L artisanal Belgian Witbier pitcher', TRUE),
-- Home Services
(20, 31, 'Split AC Deep Jet Servicing',         'UC-AC-JET-CLN',  'Urban Company',  'High-pressure water jet indoor & outdoor cleaning, gas pressure test', TRUE),
(21, 29, 'Plumbing Repair & Leak Arrest',       'UC-PLUMB-REP',   'Urban Company',  'Tap, flush valve, or pipeline repair by verified plumber', TRUE),
-- Education
(22, 18, 'Grade 10 Annual Comprehensive Fee',   'NPS-GR10-ANN',   'NPS Indiranagar','Tuition, STEM laboratory, computer lab & library fees for 1 academic year', TRUE),
-- Shopping & Daily Essentials
(23, 36, 'India Gate Basmati Rice Classic (5 kg)','IG-BASM-5KG',  'India Gate',     'Aged long grain premium fragrant basmati rice', TRUE),
(24, 38, 'Surf Excel Matic Top Load Detergent (2 kg)','SX-MATIC-2KG','Surf Excel', 'Enzymatic active stain removal detergent powder for washing machines', TRUE)
ON CONFLICT DO NOTHING;

-- 10. CATEGORY-SPECIFIC ATTRIBUTES (Structured JSONB matching Phase 3 specs)
INSERT INTO product_service_attributes (product_service_id, attributes, comparison_key_values)
VALUES
(1, '{"brand": "Samsung", "model": "Galaxy S24", "ram_gb": 8, "storage": "128GB", "screen_size": "6.2 inches", "processor": "Snapdragon 8 Gen 3", "battery_mah": 4000, "5g": true, "warranty_months": 12}',
    '{"category": "smartphones", "brand": "Samsung", "storage": "128GB", "ram": "8GB"}'),
(2, '{"brand": "Apple", "model": "iPhone 15", "ram_gb": 6, "storage": "128GB", "screen_size": "6.1 inches", "processor": "A16 Bionic", "5g": true, "warranty_months": 12}',
    '{"category": "smartphones", "brand": "Apple", "storage": "128GB"}'),
(3, '{"brand": "Apple", "model": "MacBook Air", "processor": "Apple M3", "ram_gb": 16, "storage": "512GB SSD", "display": "13.6 Liquid Retina"}',
    '{"category": "laptops", "processor": "M3", "ram": "16GB", "storage": "512GB"}'),
(4, '{"brand": "Sony", "model": "KD-55X74L", "screen_size_inches": 55, "resolution": "4K Ultra HD", "display_type": "LED", "refresh_rate_hz": 60, "smart_os": "Google TV"}',
    '{"category": "televisions", "screen_size": 55, "resolution": "4K"}'),
(5, '{"property_name": "Prestige Lakeside", "property_type": "Apartment", "bedrooms": 2, "bathrooms": 2, "built_up_sqft": 1250, "furnishing": "Semi-Furnished", "gated_community": true, "parking_slots": 1, "facing": "East"}',
    '{"property_type": "Apartment", "bedrooms": 2, "furnishing": "Semi-Furnished"}'),
(6, '{"property_type": "PG", "occupancy": "Single Private", "ac_available": true, "meals_included": "Breakfast, Lunch, Dinner", "attached_washroom": true, "wifi": true, "power_backup": true}',
    '{"occupancy": "Single", "ac": true, "food": true}'),
(7, '{"property_name": "Prestige Lakeside", "property_type": "Apartment", "bedrooms": 3, "built_up_sqft": 1780, "sale_classification": "Resale / Primary", "furnishing": "Unfurnished"}',
    '{"property_type": "Apartment", "bedrooms": 3}'),
(8, '{"manufacturer": "Maruti Suzuki", "model": "Swift", "variant": "VXi", "model_year": 2026, "fuel_type": "Petrol", "transmission": "5-Speed Manual", "engine_cc": 1197, "mileage_kmpl": 24.8}',
    '{"manufacturer": "Maruti Suzuki", "model": "Swift", "variant": "VXi", "transmission": "Manual"}'),
(9, '{"manufacturer": "Honda", "model": "Activa 6G", "variant": "Standard", "model_year": 2026, "fuel_type": "Petrol", "engine_cc": 109.51, "braking": "Combi-Brake System"}',
    '{"model": "Activa 6G", "variant": "Standard"}'),
(10, '{"service_type": "Periodic Maintenance", "interval_km": 10000, "work_scope": "Engine oil change, oil filter replace, brake clean, coolant topup, 40-pt inspection", "parts_included": true}',
    '{"service_type": "Periodic", "interval": "10000km"}'),
(11, '{"specialty": "Cardiology", "doctor_qualification": "MD, DM (Cardiology)", "doctor_experience_years": 18, "consultation_mode": "In-Person OP", "validity_days": 7}',
    '{"specialty": "Cardiology", "mode": "In-Person"}'),
(12, '{"test_name": "Lipid Profile", "specimen": "Blood", "fasting_required": true, "fasting_hours": 12, "turnaround_hours": 6, "parameters_count": 5}',
    '{"test_code": "LIPID", "fasting": true}'),
(13, '{"package_name": "Master Health Checkup", "parameters_included": 64, "fasting_required": true, "components": ["Complete Blood Count", "Lipid Profile", "Liver Function", "Kidney Function", "ECG", "Chest X-Ray", "Physician Consultation"]}',
    '{"package_tier": "Master", "parameters": 64}'),
(14, '{"property_name": "Ginger Hotel Whitefield", "room_type": "Standard AC Double", "occupancy_adults": 2, "ac_available": true, "wifi_included": true, "meal_plan": "Room Only", "checkin": "14:00", "checkout": "12:00"}',
    '{"room_type": "Standard AC", "occupancy": 2, "meal_plan": "Room Only"}'),
(15, '{"property_name": "The Leela Palace", "room_type": "Royal Premier Suite", "occupancy_adults": 2, "ac_available": true, "meal_plan": "Breakfast Included", "lounge_access": true, "area_sqft": 800}',
    '{"room_type": "Suite", "meal_plan": "Breakfast Included"}'),
(16, '{"gym_name": "Cult.fit", "membership_type": "Cultpass Elite", "duration_months": 12, "all_center_access": true, "group_classes_included": true, "trainer_on_floor": true}',
    '{"duration_months": 12, "access": "All Centers"}'),
(17, '{"gym_name": "Cult.fit", "training_type": "1-on-1 Personal Training", "sessions_count": 12, "duration_weeks": 4, "trainer_certified": true}',
    '{"training": "1-on-1 PT", "sessions": 12}'),
(18, '{"restaurant_name": "Barbeque Nation", "meal_type": "Unlimited Dinner Buffet", "session": "Weekend Dinner", "dietary": "Veg & Non-Veg", "live_grill": true}',
    '{"format": "Buffet", "session": "Weekend Dinner"}'),
(19, '{"restaurant_name": "Toit", "beverage_name": "Craft Beer Pitcher", "volume_litres": 1.5, "alcohol_type": "Wheat Beer (Belgian Witbier)", "serves": 3}',
    '{"item": "Craft Beer Pitcher", "volume": "1.5L"}'),
(20, '{"service_provider": "Urban Company", "service_type": "AC Servicing", "work_scope": "Foam + Power Jet deep cleaning of indoor & outdoor unit, gas check", "duration_hours": 1.5, "materials_included": true, "warranty_days": 30}',
    '{"service": "AC Jet Cleaning"}'),
(21, '{"service_provider": "Urban Company", "service_type": "Plumbing", "work_scope": "Leak fix, tap replacement, pipe sealant", "duration_hours": 1, "materials_included": false, "warranty_days": 30}',
    '{"service": "Plumbing Leak Fix"}'),
(22, '{"institution_name": "National Public School", "course_grade": "Grade 10", "board": "CBSE", "academic_year": "2026-2027", "fee_type": "Annual Consolidated"}',
    '{"board": "CBSE", "grade": 10}'),
(23, '{"brand": "India Gate", "product_name": "Basmati Rice Classic", "net_quantity": 5, "unit_of_measurement": "kg", "pack_type": "Bag", "grain_type": "Extra Long Aged Grain"}',
    '{"brand": "India Gate", "quantity_kg": 5}'),
(24, '{"brand": "Surf Excel", "product_name": "Matic Top Load Detergent", "net_quantity": 2, "unit_of_measurement": "kg", "pack_type": "Pouch"}',
    '{"brand": "Surf Excel", "quantity_kg": 2}')
ON CONFLICT (product_service_id) DO UPDATE SET 
    attributes = EXCLUDED.attributes,
    comparison_key_values = EXCLUDED.comparison_key_values;

-- 11. LISTINGS & PRICE OBSERVATIONS (Real Benchmark Market Data)
INSERT INTO listings (id, user_id, provider_id, provider_location_id, product_service_id, custom_title, price_amount, price_type_id, currency, moderation_status, is_available, is_active, notes)
VALUES
(1,  2, 1,  1,  1,  'Samsung Galaxy S24 128GB Onyx Black In Stock',    74999.00, 4, 'INR', 'approved', TRUE, TRUE, 'Card discount ₹5000 extra on HDFC'),
(2,  2, 2,  2,  1,  'Samsung Galaxy S24 128GB Special Store Offer',     76499.00, 4, 'INR', 'approved', TRUE, TRUE, 'Includes 1-year accidental warranty'),
(3,  2, 2,  2,  2,  'Apple iPhone 15 128GB Blue',                       69999.00, 4, 'INR', 'approved', TRUE, TRUE, 'Instant bank cashback applicable'),
(4,  2, 1,  1,  3,  'Apple MacBook Air M3 16GB Midnight',              124900.00, 4, 'INR', 'approved', TRUE, TRUE, 'Student discount available'),
(5,  2, 1,  1,  4,  'Sony Bravia 55 inch 4K Google TV (2025/2026)',     57990.00, 4, 'INR', 'approved', TRUE, TRUE, 'Free wall mount installation included'),
(6,  2, 3,  3,  5,  'Prestige Lakeside 2 BHK Lake View Apartment',      38000.00, 11, 'INR', 'approved', TRUE, TRUE, 'Maintenance ₹4,500/month extra'),
(7,  2, 15, 15, 6,  'Stanza Living Single AC Room Neeladri Road',       14500.00, 2,  'INR', 'approved', TRUE, TRUE, 'Food & housekeeping included in rent'),
(8,  2, 3,  3,  7,  'Prestige Lakeside 3 BHK 1780 sqft Ready to Move', 18500000.00, 12, 'INR', 'approved', TRUE, TRUE, 'Clear title, A-Khata, RERA approved'),
(9,  2, 6,  6,  8,  'Maruti Swift VXi 1.2 Petrol Manual',               729000.00, 5, 'INR', 'approved', TRUE, TRUE, 'Ex-showroom price Bengaluru'),
(10, 2, 6,  6,  9,  'Honda Activa 6G Standard Edition',                  79500.00, 5, 'INR', 'approved', TRUE, TRUE, 'Ex-showroom price Bengaluru'),
(11, 2, 6,  6,  10, 'Maruti 10,000 km Scheduled Periodic Service',       3450.00, 6, 'INR', 'approved', TRUE, TRUE, 'Includes synthetic engine oil & filter'),
(12, 2, 4,  4,  11, 'Cardiology Senior Consultant OPD Visit',             1000.00, 7, 'INR', 'approved', TRUE, TRUE, 'Valid for 1 follow-up within 7 days'),
(13, 2, 5,  5,  12, 'Lipid Profile Automated Investigation',              650.00, 8, 'INR', 'approved', TRUE, TRUE, '12 hours fasting required. Report in 6h'),
(14, 2, 4,  4,  13, 'Manipal Comprehensive Master Health Checkup',        5500.00, 8, 'INR', 'approved', TRUE, TRUE, 'Includes 64 tests & breakfast'),
(15, 2, 7,  7,  14, 'Standard AC Double Room Tariff Per Night',           3200.00, 1, 'INR', 'approved', TRUE, TRUE, 'Free high-speed WiFi, cancellation up to 24h'),
(16, 2, 8,  8,  15, 'The Leela Palace Royal Premier Suite Per Night',    24500.00, 1, 'INR', 'approved', TRUE, TRUE, 'Includes luxury buffet breakfast at Citrus'),
(17, 2, 9,  9,  16, 'Cultpass Elite Annual Subscription Plan',           16999.00, 17, 'INR', 'approved', TRUE, TRUE, 'Unlimited all-center access for 12 months'),
(18, 2, 9,  9,  17, '1-on-1 Personal Trainer Monthly Package (12x)',      9500.00, 15, 'INR', 'approved', TRUE, TRUE, 'Customized workout and nutrition coaching'),
(19, 2, 11, 11, 18, 'Barbeque Nation Unlimited Dinner Buffet',            1149.00, 14, 'INR', 'approved', TRUE, TRUE, 'Taxes extra. Covers live grill + buffet'),
(20, 2, 10, 10, 19, 'Toit Tint-In-Brew Artisanal Witbier Pitcher',         950.00, 13, 'INR', 'approved', TRUE, TRUE, '1.5 Litres freshly brewed craft beer'),
(21, 2, 12, 12, 20, 'Split AC Foam & Jet Cleaning Service',                599.00, 15, 'INR', 'approved', TRUE, TRUE, 'Includes 30 days service warranty'),
(22, 2, 12, 12, 21, 'Standard Plumbing Repair & Tap Leak Fix',            199.00, 15, 'INR', 'approved', TRUE, TRUE, 'Spare parts charged at actual MRP'),
(23, 2, 13, 13, 22, 'NPS Grade 10 Annual Academic Year Fee',            145000.00, 9,  'INR', 'approved', TRUE, TRUE, 'Payable in 2 equal semester installments'),
(24, 2, 14, 14, 23, 'India Gate Basmati Rice Classic 5kg Bag',             649.00, 18, 'INR', 'approved', TRUE, TRUE, 'MRP ₹750. In-store discounted price'),
(25, 2, 14, 14, 24, 'Surf Excel Matic Top Load 2kg Detergent',             415.00, 18, 'INR', 'approved', TRUE, TRUE, 'MRP ₹460. Special grocery offer')
ON CONFLICT (id) DO UPDATE SET price_amount = EXCLUDED.price_amount;

-- 12. IMMUTABLE PRICE OBSERVATIONS (History & Benchmarking)
INSERT INTO price_observations (listing_id, price_type_id, price_amount, currency, source_type, submission_date, effective_date, verification_status, evidence_reference, notes)
SELECT 
    id AS listing_id,
    price_type_id,
    price_amount,
    currency,
    'user_submitted' AS source_type,
    created_at AS submission_date,
    created_at AS effective_date,
    'verified' AS verification_status,
    'Verified by PricePulse Field Team' AS evidence_reference,
    notes
FROM listings
ON CONFLICT DO NOTHING;

-- Reset identity sequence counters
SELECT setval('cities_id_seq', (SELECT COALESCE(MAX(id), 1) FROM cities));
SELECT setval('areas_id_seq', (SELECT COALESCE(MAX(id), 1) FROM areas));
SELECT setval('categories_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categories));
SELECT setval('subcategories_id_seq', (SELECT COALESCE(MAX(id), 1) FROM subcategories));
SELECT setval('price_types_id_seq', (SELECT COALESCE(MAX(id), 1) FROM price_types));
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('providers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM providers));
SELECT setval('provider_locations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM provider_locations));
SELECT setval('products_and_services_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products_and_services));
SELECT setval('product_service_attributes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM product_service_attributes));
SELECT setval('listings_id_seq', (SELECT COALESCE(MAX(id), 1) FROM listings));
SELECT setval('price_observations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM price_observations));

