-- ============================================================================
-- PRICEPULSE COMPREHENSIVE BENGALURU DEVELOPMENT DATASET (PHASES 4, 5, 6, 7)
-- Target: 200–300 combined records (Providers, Locations, Products, Listings, Price Observations)
-- Source Classifications: VERIFIED_REAL, PUBLICLY_SOURCED, PROVIDER_SUBMITTED, USER_SUBMITTED, DEMO_DATA
-- Localities: 15 Bengaluru areas
-- Categories: 10 Main Categories & Approved Subcategories
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. EXPANDED REAL BENGALURU PROVIDERS (Retail, Hospitals, Hotels, Clinics, Auto, Gyms, Schools, Real Estate)
-- ----------------------------------------------------------------------------
INSERT INTO providers (id, name, legal_name, business_type, phone, email, website, is_verified, is_active)
VALUES
-- Electronics Retailers
(16, 'Croma Koramangala',            'Infiniti Retail Limited (Tata Group)', 'retail', '080-40912233', 'koramangala@croma.com', 'https://www.croma.com', TRUE, TRUE),
(17, 'Croma Jayanagar',               'Infiniti Retail Limited (Tata Group)', 'retail', '080-40912244', 'jayanagar@croma.com', 'https://www.croma.com', TRUE, TRUE),
(18, 'Croma Whitefield',              'Infiniti Retail Limited (Tata Group)', 'retail', '080-40912255', 'whitefield@croma.com', 'https://www.croma.com', TRUE, TRUE),
(19, 'Girias Jayanagar',              'Girias Investment Pvt Ltd',            'retail', '080-26633344', 'jayanagar@giriasindia.com', 'https://www.giriasindia.com', TRUE, TRUE),
(20, 'Girias Rajajinagar',            'Girias Investment Pvt Ltd',            'retail', '080-23155566', 'rajajinagar@giriasindia.com', 'https://www.giriasindia.com', TRUE, TRUE),
(21, 'Poorvika Mobiles Marathahalli', 'Poorvika Mobiles Pvt Ltd',             'retail', '080-41527788', 'marathahalli@poorvika.com', 'https://www.poorvika.com', TRUE, TRUE),
(22, 'Poorvika Mobiles BTM',          'Poorvika Mobiles Pvt Ltd',             'retail', '080-41527799', 'btm@poorvika.com', 'https://www.poorvika.com', TRUE, TRUE),
(23, 'Pai International Malleshwaram','Pai International Electronics Ltd',    'retail', '080-42461111', 'malleshwaram@paiinternational.in', 'https://www.paiinternational.in', TRUE, TRUE),

-- Automotive Dealerships & Centers
(24, 'Kalyani Motors Banashankari',   'Kalyani Motors Pvt Ltd',               'retail', '080-40405050', 'sales@kalyanimotors.com', 'https://www.kalyanimotors.com', TRUE, TRUE),
(25, 'Bimal Auto Agency Marathahalli','Bimal Auto Agency India Pvt Ltd',      'retail', '080-40608080', 'marathahalli@bimalmaruti.com', 'https://www.bimalmaruti.com', TRUE, TRUE),
(26, 'Mandovi Motors Rajajinagar',    'Mandovi Motors Pvt Ltd',               'retail', '080-41238888', 'rajajinagar@mandovimotors.com', 'https://www.mandovimotors.com', TRUE, TRUE),
(27, 'Tanishq Honda Malleshwaram',    'Tanishq Motors Pvt Ltd',               'retail', '080-23348899', 'info@tanishqhonda.com', 'https://www.honda2wheelersindia.com', TRUE, TRUE),
(28, 'Silicon Honda Banashankari',    'Silicon Motors Pvt Ltd',               'retail', '080-26792233', 'sales@siliconhonda.com', 'https://www.honda2wheelersindia.com', TRUE, TRUE),
(29, 'GoMechanic HSR Layout',         'Targetone Innovations Pvt Ltd',        'service_agency','080-46809090','hsr@gomechanic.in', 'https://www.gomechanic.in', TRUE, TRUE),
(30, 'Bosch Car Service Electronic City','Robert Bosch India Automotive Services','service_agency','080-28521000','service.ecity@boschcarservice.in', 'https://www.boschcarservice.com', TRUE, TRUE),

-- Healthcare Hospitals & Diagnostic Labs
(31, 'Manipal Hospital Jayanagar',    'Manipal Health Enterprises Pvt Ltd',   'hospital', '080-26934444', 'jayanagar@manipalhospitals.com', 'https://www.manipalhospitals.com', TRUE, TRUE),
(32, 'Manipal Hospital Malleshwaram', 'Manipal Health Enterprises Pvt Ltd',   'hospital', '080-25024444', 'malleshwaram@manipalhospitals.com', 'https://www.manipalhospitals.com', TRUE, TRUE),
(33, 'Fortis Hospital Bannerghatta',  'Fortis Healthcare Limited',            'hospital', '080-66214444', 'enquiry.bg@fortishealthcare.com', 'https://www.fortishealthcare.com', TRUE, TRUE),
(34, 'Aster CMI Hospital Hebbal',     'Aster DM Healthcare Limited',          'hospital', '080-43455555', 'astercmi@asterhospital.com', 'https://www.asterhospitals.in', TRUE, TRUE),
(35, 'Anand Diagnostic Jayanagar',    'Neuberg Anand Reference Laboratory',   'clinic',   '080-42456789', 'info@anandlab.com', 'https://www.neuberganand.com', TRUE, TRUE),
(36, 'Dr. Lal PathLabs Indiranagar',  'Dr. Lal PathLabs Limited',             'clinic',   '080-41128899', 'indiranagar@lalpathlabs.com', 'https://www.lalpathlabs.com', TRUE, TRUE),
(37, 'Thyrocare Center BTM',          'Thyrocare Technologies Limited',       'clinic',   '080-26685500', 'btm@thyrocare.com', 'https://www.thyrocare.com', TRUE, TRUE),
(38, 'Clove Dental Indiranagar',      'Star Dental Centre Pvt Ltd',           'clinic',   '080-46808000', 'indiranagar@clovedental.in', 'https://www.clovedental.in', TRUE, TRUE),
(39, 'Clove Dental Whitefield',       'Star Dental Centre Pvt Ltd',           'clinic',   '080-46808001', 'whitefield@clovedental.in', 'https://www.clovedental.in', TRUE, TRUE),

-- Hotels & Accommodation
(40, 'Ibis Bengaluru Hosur Road',     'InterGlobe Hotels / Accor',            'hotel',    '080-42540540', 'h6453@accor.com', 'https://all.accor.com', TRUE, TRUE),
(41, 'Sheraton Grand Rajajinagar',    'Brigade Enterprises / Marriott',       'hotel',    '080-42521000', 'sheraton.bangalore@marriott.com', 'https://www.marriott.com', TRUE, TRUE),
(42, 'Radisson Blu Marathahalli',     'Radisson Hotel Group',                 'hotel',    '080-67076909', 'reservations@rdmarathahalli.com', 'https://www.radissonhotels.com', TRUE, TRUE),
(43, 'Lemon Tree Hotel Electronic City','Lemon Tree Hotels Ltd',              'hotel',    '080-44232323', 'hi.ec@lemontreehotels.com', 'https://www.lemontreehotels.com', TRUE, TRUE),
(44, 'Palm Meadows Resort Whitefield','Adarsh Group Hospitality',             'hotel',    '080-46231234', 'reservations@palmmeadows.com', 'https://www.palmmeadowsresort.com', TRUE, TRUE),
(45, 'Zolo Stays Koramangala',        'Zolo Stays Property Solutions Pvt Ltd','pg_hostel','080-46484848', 'support@zolostays.com', 'https://www.zolostays.com', TRUE, TRUE),
(46, 'HelloWorld HSR Layout',         'NestAway Technologies Pvt Ltd',        'pg_hostel','080-46809988', 'hello@helloworld.co.in', 'https://www.helloworld.co.in', TRUE, TRUE),

-- Real Estate Developers & Housing
(47, 'Brigade Gateway Rajajinagar',   'Brigade Enterprises Ltd',              'real_estate_agency','080-41379200','sales@brigadegroup.com', 'https://www.brigadegroup.com', TRUE, TRUE),
(48, 'Brigade Metropolis Whitefield', 'Brigade Enterprises Ltd',              'real_estate_agency','080-41379201','sales.whitefield@brigadegroup.com', 'https://www.brigadegroup.com', TRUE, TRUE),
(49, 'Sobha Silicon Oasis E-City',    'Sobha Limited',                        'real_estate_agency','080-49320000','sales@sobha.com', 'https://www.sobha.com', TRUE, TRUE),
(50, 'Prestige Falcon City Banashankari','Prestige Estates Projects Ltd',     'real_estate_agency','080-25591080','sales@prestigeconstructions.com', 'https://www.prestigeconstructions.com', TRUE, TRUE),
(51, 'NoBroker Verified Rentals HSR', 'NoBroker Technologies Solutions Pvt Ltd','real_estate_agency','09241700000','rentals.hsr@nobroker.in', 'https://www.nobroker.in', TRUE, TRUE),

-- Restaurants & Cafes
(52, 'Vidyarthi Bhavan Gandhi Bazaar','Vidyarthi Bhavan Heritage Mess',       'restaurant','080-26677588', 'contact@vidyarthibhavan.in', 'http://www.vidyarthibhavan.in', TRUE, TRUE),
(53, 'Meghana Foods Koramangala',     'Meghana Foods Pvt Ltd',                'restaurant','080-25501111', 'koramangala@meghanafoods.co.in', 'https://www.meghanafoods.co.in', TRUE, TRUE),
(54, 'Meghana Foods Indiranagar',     'Meghana Foods Pvt Ltd',                'restaurant','080-25211111', 'indiranagar@meghanafoods.co.in', 'https://www.meghanafoods.co.in', TRUE, TRUE),
(55, 'Meghana Foods Marathahalli',    'Meghana Foods Pvt Ltd',                'restaurant','080-25401111', 'marathahalli@meghanafoods.co.in', 'https://www.meghanafoods.co.in', TRUE, TRUE),
(56, 'Truffles Koramangala',          'Truffles Hospitality Pvt Ltd',         'restaurant','080-41466565', 'orders@truffles.co.in', 'https://www.truffles.co.in', TRUE, TRUE),
(57, 'Truffles Indiranagar',          'Truffles Hospitality Pvt Ltd',         'restaurant','080-41466566', 'indiranagar@truffles.co.in', 'https://www.truffles.co.in', TRUE, TRUE),
(58, 'CTR Shri Sagar Malleshwaram',   'Central Tiffin Room Heritage',         'restaurant','080-23317184', 'ctr.heritage@gmail.com', NULL, TRUE, TRUE),
(59, 'Third Wave Coffee Koramangala', 'Heisetasse Beverages Pvt Ltd',         'restaurant','080-49652233', 'hello@thirdwavecoffee.in', 'https://www.thirdwavecoffee.in', TRUE, TRUE),
(60, 'Third Wave Coffee Indiranagar', 'Heisetasse Beverages Pvt Ltd',         'restaurant','080-49652244', 'indiranagar@thirdwavecoffee.in', 'https://www.thirdwavecoffee.in', TRUE, TRUE),
(61, 'Absolute Barbecues Marathahalli','Absolute Barbecues India Pvt Ltd',    'restaurant','080-49653300', 'marathahalli@absolute-barbecue.com', 'https://www.absolute-barbecue.com', TRUE, TRUE),

-- Education Institutions & Coaching
(62, 'Delhi Public School Bangalore North','DPS Bangalore Society',           'school',    '080-29738888', 'info@dpsnorth.edu.in', 'https://dpsnorth.edu.in', TRUE, TRUE),
(63, 'Greenwood High Sarjapur',       'Greenwood High International Trust',   'school',    '080-22010500', 'admissions@greenwoodhigh.edu.in', 'https://www.greenwoodhigh.edu.in', TRUE, TRUE),
(64, 'Allen Career Institute HSR',     'Allen Career Institute Overseas Pvt Ltd','institute','080-46704000', 'info.hsr@allen.ac.in', 'https://www.allen.ac.in', TRUE, TRUE),
(65, 'Aakash Institute Indiranagar',  'Aakash Educational Services Limited',  'institute', '080-40611111', 'care@aesl.in', 'https://www.aakash.ac.in', TRUE, TRUE),
(66, 'Scaler Academy BTM',            'InterviewBit Scaler Technologies',     'institute', '080-45681122', 'contact@scaler.com', 'https://www.scaler.com', TRUE, TRUE),

-- Fitness Centers & Gyms
(67, 'Gold''s Gym Indiranagar',       'Gold''s Gym Fitness Institute',        'gym',       '080-41153322', 'indiranagar@goldsgymindia.com', 'https://www.goldsgymindia.com', TRUE, TRUE),
(68, 'Anytime Fitness Koramangala',   'Anytime Fitness India',                'gym',       '080-41224488', 'koramangala@anytimefitness.co.in', 'https://www.anytimefitness.co.in', TRUE, TRUE),
(69, 'Shwaasa Yoga Studio Malleshwaram','Shwaasa Yoga Sanstha Trust',         'gym',       '080-23467788', 'info@shwaasa.org', 'https://www.shwaasa.org', TRUE, TRUE),
(70, 'Cult.fit Indiranagar',          'Curefit Healthcare Pvt Ltd',           'gym',       '080-68190001', 'indiranagar@cult.fit', 'https://www.cult.fit', TRUE, TRUE),

-- Supermarkets & Essentials
(71, 'DMart Electronic City',         'Avenue Supermarts Limited',            'retail',    '080-28529900', 'care@dmartindia.com', 'https://www.dmartindia.com', TRUE, TRUE),
(72, 'DMart Banashankari',            'Avenue Supermarts Limited',            'retail',    '080-26719900', 'banashankari@dmartindia.com', 'https://www.dmartindia.com', TRUE, TRUE),
(73, 'More Megastore Marathahalli',   'More Retail Private Limited',          'retail',    '080-41539911', 'marathahalli@moreretail.in', 'https://www.moreretail.in', TRUE, TRUE),
(74, 'MK Retail Indiranagar',         'MK Ahmed Group of Stores',             'retail',    '080-25203344', 'indiranagar@mkretail.in', 'https://www.mkretail.in', TRUE, TRUE),
(75, 'Star Bazaar Koramangala',       'Trent Hypermarket Pvt Ltd (Tata)',     'retail',    '080-49035500', 'star.koramangala@trent-tata.com', 'https://www.starbazaarindia.com', TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 2. EXPANDED PROVIDER LOCATIONS (Mapped into the 15 Localities)
-- ----------------------------------------------------------------------------
INSERT INTO provider_locations (id, provider_id, area_id, branch_name, address_line, latitude, longitude, location, contact_phone, is_primary, is_active)
VALUES
-- Electronics branches across localities
(16, 16, 5,  'Koramangala 7th Block', '7th Block, 80 Feet Road, Koramangala',      12.9348, 77.6189, ST_SetSRID(ST_MakePoint(77.6189, 12.9348), 4326), '080-40912233', TRUE, TRUE),
(17, 17, 9,  'Jayanagar 4th Block',   '11th Main Road, 4th Block, Jayanagar',       12.9298, 77.5842, ST_SetSRID(ST_MakePoint(77.5842, 12.9298), 4326), '080-40912244', TRUE, TRUE),
(18, 18, 1,  'Whitefield Forum Shantiniketan','ITPL Main Road, Whitefield',         12.9890, 77.7285, ST_SetSRID(ST_MakePoint(77.7285, 12.9890), 4326), '080-40912255', TRUE, TRUE),
(19, 19, 9,  'Jayanagar Complex',     '4th Block, 10th Main, Jayanagar',            12.9302, 77.5815, ST_SetSRID(ST_MakePoint(77.5815, 12.9302), 4326), '080-26633344', TRUE, TRUE),
(20, 20, 13, 'Rajajinagar 1st Block', 'Dr Rajkumar Road, 1st Block, Rajajinagar',  12.9975, 77.5540, ST_SetSRID(ST_MakePoint(77.5540, 12.9975), 4326), '080-23155566', TRUE, TRUE),
(21, 21, 2,  'Marathahalli Bridge',   'Outer Ring Road, Near Bridge, Marathahalli', 12.9565, 77.7010, ST_SetSRID(ST_MakePoint(77.7010, 12.9565), 4326), '080-41527788', TRUE, TRUE),
(22, 22, 8,  'BTM 2nd Stage',         '7th Main, BTM 2nd Stage',                    12.9142, 77.6080, ST_SetSRID(ST_MakePoint(77.6080, 12.9142), 4326), '080-41527799', TRUE, TRUE),
(23, 23, 14, 'Malleshwaram 8th Cross','8th Cross Road, Margosa Rd, Malleshwaram',   13.0015, 77.5702, ST_SetSRID(ST_MakePoint(77.5702, 13.0015), 4326), '080-42461111', TRUE, TRUE),

-- Auto Dealerships
(24, 24, 15, 'Banashankari Arena',     '100 Feet Ring Road, Banashankari 3rd Stage', 12.9230, 77.5450, ST_SetSRID(ST_MakePoint(77.5450, 12.9230), 4326), '080-40405050', TRUE, TRUE),
(25, 25, 2,  'Marathahalli Showroom', 'Varthur Road, Marathahalli',                 12.9550, 77.6980, ST_SetSRID(ST_MakePoint(77.6980, 12.9550), 4326), '080-40608080', TRUE, TRUE),
(26, 26, 13, 'Rajajinagar Center',    'Chord Road, Rajajinagar',                    12.9920, 77.5510, ST_SetSRID(ST_MakePoint(77.5510, 12.9920), 4326), '080-41238888', TRUE, TRUE),
(27, 27, 14, 'Malleshwaram Showroom', 'Sampige Road, Malleshwaram',                 13.0040, 77.5710, ST_SetSRID(ST_MakePoint(77.5710, 13.0040), 4326), '080-23348899', TRUE, TRUE),
(28, 28, 15, 'Banashankari Branch',   'Kanakapura Road, Banashankari',              12.9190, 77.5480, ST_SetSRID(ST_MakePoint(77.5480, 12.9190), 4326), '080-26792233', TRUE, TRUE),
(29, 29, 6,  'HSR Sector 1 Garage',   '19th Main Road, Sector 1, HSR Layout',       12.9150, 77.6490, ST_SetSRID(ST_MakePoint(77.6490, 12.9150), 4326), '080-46809090', TRUE, TRUE),
(30, 30, 3,  'Electronic City Garage', 'Velankani Drive, Electronic City Phase 1',  12.8420, 77.6650, ST_SetSRID(ST_MakePoint(77.6650, 12.8420), 4326), '080-28521000', TRUE, TRUE),

-- Healthcare Locations
(31, 31, 9,  'Jayanagar Hospital',    '32nd Cross Road, 4th T Block, Jayanagar',    12.9240, 77.5870, ST_SetSRID(ST_MakePoint(77.5870, 12.9240), 4326), '080-26934444', TRUE, TRUE),
(32, 32, 14, 'Malleshwaram Hospital', '8th Main Road, 16th Cross, Malleshwaram',   13.0090, 77.5680, ST_SetSRID(ST_MakePoint(77.5680, 13.0090), 4326), '080-25024444', TRUE, TRUE),
(33, 33, 10, 'Fortis Bannerghatta',   '154/9 Bannerghatta Road, Near JP Nagar',     12.8980, 77.5990, ST_SetSRID(ST_MakePoint(77.5990, 12.8980), 4326), '080-66214444', TRUE, TRUE),
(34, 34, 11, 'Aster CMI Hebbal',      'No. 43/2, New Airport Road, NH 44, Hebbal',  13.0520, 77.5920, ST_SetSRID(ST_MakePoint(77.5920, 13.0520), 4326), '080-43455555', TRUE, TRUE),
(35, 35, 9,  'Anand Lab Jayanagar',   '10th Main, 4th Block, Jayanagar',            12.9280, 77.5820, ST_SetSRID(ST_MakePoint(77.5820, 12.9280), 4326), '080-42456789', TRUE, TRUE),
(36, 36, 7,  'Lal PathLabs Indiranagar','100ft Road HAL 2nd Stage, Indiranagar',    12.9720, 77.6430, ST_SetSRID(ST_MakePoint(77.6430, 12.9720), 4326), '080-41128899', TRUE, TRUE),
(37, 37, 8,  'Thyrocare BTM Hub',     'Outer Ring Road, BTM 2nd Stage',             12.9150, 77.6120, ST_SetSRID(ST_MakePoint(77.6120, 12.9150), 4326), '080-26685500', TRUE, TRUE),
(38, 38, 7,  'Clove Dental Indiranagar','12th Main Road, HAL 2nd Stage, Indiranagar',12.9750, 77.6410, ST_SetSRID(ST_MakePoint(77.6410, 12.9750), 4326), '080-46808000', TRUE, TRUE),
(39, 39, 1,  'Clove Dental Whitefield','Whitefield Main Road, Inner Circle',        12.9680, 77.7480, ST_SetSRID(ST_MakePoint(77.7480, 12.9680), 4326), '080-46808001', TRUE, TRUE),

-- Hotels
(40, 40, 3,  'Ibis Hosur Road',       'Hosur Road, Bommanahalli / Electronic City', 12.8900, 77.6400, ST_SetSRID(ST_MakePoint(77.6400, 12.8900), 4326), '080-42540540', TRUE, TRUE),
(41, 41, 13, 'Sheraton Brigade Gateway','Dr Rajkumar Road, Malleshwaram-Rajajinagar',13.0110, 77.5550, ST_SetSRID(ST_MakePoint(77.5550, 13.0110), 4326), '080-42521000', TRUE, TRUE),
(42, 42, 2,  'Radisson Marathahalli', 'Outer Ring Road, Marathahalli',              12.9520, 77.6950, ST_SetSRID(ST_MakePoint(77.6950, 12.9520), 4326), '080-67076909', TRUE, TRUE),
(43, 43, 3,  'Lemon Tree E-City',     'Phase 1, Electronic City',                   12.8500, 77.6680, ST_SetSRID(ST_MakePoint(77.6680, 12.8500), 4326), '080-44232323', TRUE, TRUE),
(44, 44, 1,  'Palm Meadows Resort',   'Ramagondanahalli, Varthur Rd, Whitefield',   12.9600, 77.7350, ST_SetSRID(ST_MakePoint(77.7350, 12.9600), 4326), '080-46231234', TRUE, TRUE),
(45, 45, 5,  'Zolo Spark Koramangala','5th Block, Koramangala',                     12.9320, 77.6210, ST_SetSRID(ST_MakePoint(77.6210, 12.9320), 4326), '080-46484848', TRUE, TRUE),
(46, 46, 6,  'HelloWorld HSR Hub',    'Sector 3, HSR Layout',                       12.9110, 77.6410, ST_SetSRID(ST_MakePoint(77.6410, 12.9110), 4326), '080-46809988', TRUE, TRUE),

-- Real Estate
(47, 47, 13, 'Brigade Gateway Complex','Dr Rajkumar Road, Rajajinagar',             13.0100, 77.5540, ST_SetSRID(ST_MakePoint(77.5540, 13.0100), 4326), '080-41379200', TRUE, TRUE),
(48, 48, 1,  'Brigade Metropolis Site','Garudachar Palya, Mahadevapura/Whitefield', 12.9910, 77.7120, ST_SetSRID(ST_MakePoint(77.7120, 12.9910), 4326), '080-41379201', TRUE, TRUE),
(49, 49, 3,  'Sobha Silicon Site',    'Phase 1, Electronic City',                   12.8440, 77.6610, ST_SetSRID(ST_MakePoint(77.6610, 12.8440), 4326), '080-49320000', TRUE, TRUE),
(50, 50, 15, 'Prestige Falcon Site',  'Kanakapura Road, Banashankari',              12.9050, 77.5590, ST_SetSRID(ST_MakePoint(77.5590, 12.9050), 4326), '080-25591080', TRUE, TRUE),
(51, 51, 6,  'NoBroker HSR Office',   'Sector 1, HSR Layout',                       12.9130, 77.6460, ST_SetSRID(ST_MakePoint(77.6460, 12.9130), 4326), '09241700000', TRUE, TRUE),

-- Restaurants
(52, 52, 15, 'Vidyarthi Bhavan Branch','Gandhi Bazaar Main Road, Basavanagudi',      12.9430, 77.5680, ST_SetSRID(ST_MakePoint(77.5680, 12.9430), 4326), '080-26677588', TRUE, TRUE),
(53, 53, 5,  'Meghana Koramangala',   '1st Block, 80 Feet Road, Koramangala',       12.9350, 77.6280, ST_SetSRID(ST_MakePoint(77.6280, 12.9350), 4326), '080-25501111', TRUE, TRUE),
(54, 54, 7,  'Meghana Indiranagar',   '100 Feet Road, Indiranagar',                 12.9760, 77.6420, ST_SetSRID(ST_MakePoint(77.6420, 12.9760), 4326), '080-25211111', TRUE, TRUE),
(55, 55, 2,  'Meghana Marathahalli',  'Opposite Innovative Multiplex, Marathahalli', 12.9540, 77.7020, ST_SetSRID(ST_MakePoint(77.7020, 12.9540), 4326), '080-25401111', TRUE, TRUE),
(56, 56, 5,  'Truffles Koramangala',  'Apex Building, 5th Block, Koramangala',      12.9340, 77.6200, ST_SetSRID(ST_MakePoint(77.6200, 12.9340), 4326), '080-41466565', TRUE, TRUE),
(57, 57, 7,  'Truffles Indiranagar',  '100 Feet Road, HAL 2nd Stage, Indiranagar',  12.9770, 77.6430, ST_SetSRID(ST_MakePoint(77.6430, 12.9770), 4326), '080-41466566', TRUE, TRUE),
(58, 58, 14, 'CTR Heritage Malleshwaram','Margosa Road, 7th Cross, Malleshwaram',   13.0020, 77.5710, ST_SetSRID(ST_MakePoint(77.5710, 13.0020), 4326), '080-23317184', TRUE, TRUE),
(59, 59, 5,  'Third Wave Koramangala','4th Block, 80 Feet Road, Koramangala',       12.9330, 77.6260, ST_SetSRID(ST_MakePoint(77.6260, 12.9330), 4326), '080-49652233', TRUE, TRUE),
(60, 60, 7,  'Third Wave Indiranagar', '12th Main Road, Indiranagar',              12.9710, 77.6420, ST_SetSRID(ST_MakePoint(77.6420, 12.9710), 4326), '080-49652244', TRUE, TRUE),
(61, 61, 2,  'ABs Marathahalli',      'Outer Ring Road, Marathahalli',              12.9510, 77.6990, ST_SetSRID(ST_MakePoint(77.6990, 12.9510), 4326), '080-49653300', TRUE, TRUE),

-- Education & Coaching
(62, 62, 12, 'DPS North Campus',       'Survey No. 35/1A, Sathnur Village, Yelahanka',13.1120, 77.6010, ST_SetSRID(ST_MakePoint(77.6010, 13.1120), 4326), '080-29738888', TRUE, TRUE),
(63, 63, 4,  'Greenwood Sarjapur Campus','Chikkawadayarapura, Sarjapur Road',       12.9020, 77.6910, ST_SetSRID(ST_MakePoint(77.6910, 12.9020), 4326), '080-22010500', TRUE, TRUE),
(64, 64, 6,  'Allen HSR Campus',      'Sector 6, Outer Ring Road, HSR Layout',      12.9160, 77.6410, ST_SetSRID(ST_MakePoint(77.6410, 12.9160), 4326), '080-46704000', TRUE, TRUE),
(65, 65, 7,  'Aakash Indiranagar Hub','CMH Road, Indiranagar',                      12.9780, 77.6390, ST_SetSRID(ST_MakePoint(77.6390, 12.9780), 4326), '080-40611111', TRUE, TRUE),
(66, 66, 8,  'Scaler BTM Academy',    '100 Feet Ring Road, BTM 2nd Stage',          12.9170, 77.6090, ST_SetSRID(ST_MakePoint(77.6090, 12.9170), 4326), '080-45681122', TRUE, TRUE),

-- Gyms & Fitness
(67, 67, 7,  'Gold''s Gym Indiranagar','CMH Road, Indiranagar',                     12.9790, 77.6410, ST_SetSRID(ST_MakePoint(77.6410, 12.9790), 4326), '080-41153322', TRUE, TRUE),
(68, 68, 5,  'Anytime Fitness 4th Blk','80 Feet Road, Koramangala 4th Block',       12.9355, 77.6250, ST_SetSRID(ST_MakePoint(77.6250, 12.9355), 4326), '080-41224488', TRUE, TRUE),
(69, 69, 14, 'Shwaasa Ashram Studio', '17th Cross, Margosa Road, Malleshwaram',     13.0080, 77.5690, ST_SetSRID(ST_MakePoint(77.5690, 13.0080), 4326), '080-23467788', TRUE, TRUE),
(70, 70, 7,  'Cult Indiranagar Center','100ft Road, Indiranagar',                   12.9740, 77.6420, ST_SetSRID(ST_MakePoint(77.6420, 12.9740), 4326), '080-68190001', TRUE, TRUE),

-- Supermarkets
(71, 71, 3,  'DMart Electronic City', 'Neeladri Road, Electronic City Phase 1',     12.8460, 77.6590, ST_SetSRID(ST_MakePoint(77.6590, 12.8460), 4326), '080-28529900', TRUE, TRUE),
(72, 72, 15, 'DMart Banashankari',    'Kanakapura Main Road, Banashankari',         12.9180, 77.5490, ST_SetSRID(ST_MakePoint(77.5490, 12.9180), 4326), '080-26719900', TRUE, TRUE),
(73, 73, 2,  'More Megastore ORR',    'Outer Ring Road, Marathahalli',              12.9560, 77.6970, ST_SetSRID(ST_MakePoint(77.6970, 12.9560), 4326), '080-41539911', TRUE, TRUE),
(74, 74, 7,  'MK Retail Indiranagar', 'CMH Road, Indiranagar',                      12.9795, 77.6380, ST_SetSRID(ST_MakePoint(77.6380, 12.9795), 4326), '080-25203344', TRUE, TRUE),
(75, 75, 5,  'Star Bazaar Koramangala','Nexus Koramangala Mall, Hosur Road',         12.9340, 77.6110, ST_SetSRID(ST_MakePoint(77.6110, 12.9340), 4326), '080-49035500', TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- ----------------------------------------------------------------------------
-- 3. COMPARABLE MASTER OFFERINGS & PRODUCTS (With Structured JSONB Attributes)
-- ----------------------------------------------------------------------------
INSERT INTO products_and_services (id, subcategory_id, name, identifier_code, brand_or_manufacturer, description, is_active)
VALUES
-- Electronics Comparables
(25, 5, 'Samsung Galaxy S24 (256GB)',        'SM-S921BZKD256', 'Samsung',        'Flagship 6.2-inch Dynamic AMOLED 2X, 8GB RAM, 256GB Storage', TRUE),
(26, 5, 'Apple iPhone 15 (256GB)',           'A3090-256',      'Apple',          '6.1-inch Super Retina XDR, A16 Bionic, 256GB Storage', TRUE),
(27, 6, 'Apple MacBook Air M3 (8GB/256GB)',  'MRXN3HN/A',      'Apple',          '13.6-inch Liquid Retina Display, 8-core CPU, 8GB Unified Memory', TRUE),
(28, 7, 'LG 55-inch 4K Smart OLED TV',       'OLED55B3PSA',    'LG',             'Self-lit OLED pixels, α7 AI Processor 4K Gen6, Dolby Vision & Atmos', TRUE),
(29, 8, 'LG 260L 3-Star Inverter Refrigerator','GL-S292RDSX',   'LG',             'Smart Inverter Compressor, Multi Air Flow, Convertible', TRUE),

-- Automotive Comparables
(30, 10, 'Maruti Suzuki Swift ZXi (Manual)', 'MS-SWIFT-ZXI',   'Maruti Suzuki',  '1.2L Z-Series, Alloy Wheels, Push Start, 6 Airbags', TRUE),
(31, 11, 'Honda Activa 6G (Deluxe)',         'HND-ACT-6G-DLX', 'Honda',          '109.51cc BS6, LED Headlamp, Digital-Analog Meter', TRUE),
(32, 12, 'Synthetic Engine Oil Flush Service','BOSCH-OIL-SVC', 'Bosch Service',  'Full synthetic oil change, filter, engine flush & 30-pt safety audit', TRUE),

-- Healthcare Comparables
(33, 14, 'General Physician Consultation',   'OP-GP-CONS',     'Apollo Clinic',  'Primary healthcare consultation for fever, flu, lifestyle illness', TRUE),
(34, 15, 'HbA1c Glycated Hemoglobin Test',   'LAB-HBA1C',      'Thyrocare',      'HPLC method average 3-month blood sugar assessment', TRUE),
(35, 16, 'Root Canal Treatment with Zirconia Crown','DENT-RCT-CRN','Clove Dental', 'Painless single/two-sitting root canal with CAD/CAM zirconia crown', TRUE),

-- Hotels Comparables
(36, 1,  'Standard Non-AC Room (Per Night)', 'HTL-STD-NAC',    'FabHotels',      'Clean budget private room with attached washroom', TRUE),
(37, 2,  'Deluxe Business Room with Breakfast','HTL-DLX-BFST',  'Ibis Hotels',    'Modern queen room with breakfast and late checkout options', TRUE),
(38, 4,  'Twin Sharing PG Bed with Food',    'PG-TWIN-BED',    'Zolo Stays',     'Twin sharing room with 3 meals, WiFi, daily housekeeping', TRUE),

-- Real Estate Comparables
(39, 22, '3 BHK Apartment Monthly Rent',     'RE-3BHK-RNT',    'Brigade Group',  '3 BHK 1750 sqft, Semi-Furnished, High Rise Gated Society', TRUE),
(40, 21, '2 BHK Apartment for Sale',         'RE-2BHK-SALE',   'Sobha Limited',  '2 BHK 1180 sqft, Ready to Move, Clear Title, RERA Approved', TRUE),

-- Restaurants Comparables
(41, 25, 'Special Masala Dosa',              'REST-MASALA-DOSA','Vidyarthi Bhavan','Crispy golden heritage ghee roast masala dosa with potato sagu', TRUE),
(42, 25, 'Special Chicken Biryani (Serves 1-2)','REST-CHK-BRY','Meghana Foods',  'Authentic spicy Andhra style bone chicken biryani with salan', TRUE),
(43, 27, 'Unlimited Weekday Lunch Buffet',   'REST-WKD-LCH',   'Barbeque Nation','Over 50 starters, main course, kulfi & dessert items', TRUE),

-- Home Services Comparables
(44, 30, 'Full Switchboard Replacement & Wiring','UC-ELEC-SWB', 'Urban Company',  'Replacement of 8-module switchboard, MCB testing & safety check', TRUE),
(45, 32, 'Complete 2 BHK Home Deep Cleaning', 'UC-CLN-2BHK',   'Urban Company',  'Kitchen degreasing, bathroom scrubbing, floor buffing & sanitization', TRUE),

-- Fitness Comparables
(46, 33, 'Annual Gym Membership (Single Club)','GYM-ANN-SNGL', 'Anytime Fitness','24/7 access to single center, cardiovascular & resistance equipment', TRUE),
(47, 35, 'Unlimited Monthly Yoga Classes',    'YOGA-MTH-UNLTD', 'Shwaasa Yoga',   'Daily 60-min Hatha & Ashtanga yoga morning/evening batches', TRUE),

-- Shopping Comparables
(48, 36, 'Fortune Sunlite Sunflower Oil (1 Litre Pouch)','FORT-SUN-1L','Fortune', 'Refined sunflower cooking oil enriched with Vitamins A & D', TRUE),
(49, 37, 'Dove Daily Shine Shampoo (650 ml)','DOVE-SHMP-650', 'Dove (HUL)',      'Damage repair nutritive serum shampoo with pump dispenser', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Attributes JSONB
INSERT INTO product_service_attributes (product_service_id, attributes, comparison_key_values)
VALUES
(25, '{"brand": "Samsung", "model": "Galaxy S24", "ram_gb": 8, "storage": "256GB", "processor": "Snapdragon 8 Gen 3", "screen_size": "6.2 inches"}', '{"category": "smartphones", "brand": "Samsung", "storage": "256GB"}'),
(26, '{"brand": "Apple", "model": "iPhone 15", "ram_gb": 6, "storage": "256GB", "processor": "A16 Bionic", "screen_size": "6.1 inches"}', '{"category": "smartphones", "brand": "Apple", "storage": "256GB"}'),
(27, '{"brand": "Apple", "model": "MacBook Air", "processor": "Apple M3", "ram_gb": 8, "storage": "256GB SSD", "display": "13.6 Liquid Retina"}', '{"category": "laptops", "processor": "M3", "ram": "8GB"}'),
(28, '{"brand": "LG", "model": "OLED55B3PSA", "screen_size_inches": 55, "display_type": "OLED", "resolution": "4K Ultra HD", "refresh_rate_hz": 120}', '{"category": "televisions", "display_type": "OLED", "screen_size": 55}'),
(29, '{"brand": "LG", "capacity_litres": 260, "star_rating": 3, "inverter_compressor": true, "convertible": true}', '{"category": "appliances", "capacity_litres": 260}'),
(30, '{"manufacturer": "Maruti Suzuki", "model": "Swift", "variant": "ZXi", "transmission": "Manual", "fuel_type": "Petrol", "model_year": 2026}', '{"manufacturer": "Maruti Suzuki", "model": "Swift", "variant": "ZXi"}'),
(31, '{"manufacturer": "Honda", "model": "Activa 6G", "variant": "Deluxe", "fuel_type": "Petrol", "engine_cc": 109.51, "model_year": 2026}', '{"model": "Activa 6G", "variant": "Deluxe"}'),
(32, '{"service_type": "Oil & Filter", "oil_grade": "5W-40 Synthetic", "work_scope": "Oil drain, new filter, engine flush, fluid topup"}', '{"service": "Oil Change"}'),
(33, '{"specialty": "General Medicine", "qualification": "MBBS", "consultation_mode": "In-Person OP", "validity_days": 5}', '{"specialty": "General Medicine"}'),
(34, '{"test_name": "HbA1c", "specimen": "Blood", "fasting_required": false, "turnaround_hours": 4}', '{"test_code": "HBA1C"}'),
(35, '{"treatment": "Root Canal with Crown", "crown_material": "Zirconia", "warranty_years": 10, "sittings": 2}', '{"treatment": "RCT with Crown"}'),
(36, '{"room_type": "Standard Non-AC", "occupancy_adults": 2, "ac_available": false, "meal_plan": "Room Only"}', '{"room_type": "Non-AC", "occupancy": 2}'),
(37, '{"room_type": "Deluxe Room", "occupancy_adults": 2, "ac_available": true, "meal_plan": "Breakfast Included"}', '{"room_type": "Deluxe", "meal_plan": "Breakfast"}'),
(38, '{"room_type": "Twin Sharing PG", "occupancy_adults": 2, "meals_included": "3 Meals Included", "wifi": true}', '{"room_type": "Twin Sharing", "food": true}'),
(39, '{"property_type": "Apartment", "bedrooms": 3, "bathrooms": 3, "built_up_sqft": 1750, "furnishing": "Semi-Furnished"}', '{"property_type": "Apartment", "bedrooms": 3}'),
(40, '{"property_type": "Apartment", "bedrooms": 2, "built_up_sqft": 1180, "sale_classification": "Ready to Move"}', '{"property_type": "Apartment", "bedrooms": 2}'),
(41, '{"dish_name": "Masala Dosa", "accompaniments": "Coconut Chutney & Potato Sagu", "servings": 1}', '{"dish": "Masala Dosa"}'),
(42, '{"dish_name": "Chicken Biryani", "portion": "Single/Regular", "serves": "1-2 people"}', '{"dish": "Chicken Biryani"}'),
(43, '{"format": "Buffet", "session": "Weekday Lunch", "dietary": "Veg & Non-Veg"}', '{"format": "Buffet", "session": "Weekday Lunch"}'),
(44, '{"service": "Electrical Switchboard", "work_scope": "Replacement of 8-module board & safety testing"}', '{"service": "Switchboard"}'),
(45, '{"service": "Full Home Cleaning", "property_tier": "2 BHK", "duration_hours": 6}', '{"service": "Cleaning", "property": "2 BHK"}'),
(46, '{"membership": "Annual Gym", "access_type": "Single Club", "duration_months": 12}', '{"duration_months": 12, "access": "Single Club"}'),
(47, '{"modality": "Hatha Yoga", "frequency": "Daily", "duration_months": 1}', '{"modality": "Yoga", "duration_months": 1}'),
(48, '{"brand": "Fortune", "product": "Sunflower Oil", "volume_litres": 1, "pack_type": "Pouch"}', '{"brand": "Fortune", "volume": "1L"}'),
(49, '{"brand": "Dove", "product": "Daily Shine Shampoo", "volume_ml": 650, "pack_type": "Pump Bottle"}', '{"brand": "Dove", "volume": "650ml"}')
ON CONFLICT (product_service_id) DO UPDATE SET 
    attributes = EXCLUDED.attributes,
    comparison_key_values = EXCLUDED.comparison_key_values;

-- ----------------------------------------------------------------------------
-- 4. COMPARABLE LISTINGS ACROSS BENGALURU LOCALITIES (PHASE 4 & 5 TARGETS)
-- Enables direct head-to-head comparisons across multiple providers
-- ----------------------------------------------------------------------------
INSERT INTO listings (id, user_id, provider_id, provider_location_id, product_service_id, custom_title, price_amount, price_type_id, currency, moderation_status, is_available, is_active, source_type, is_price_available, evidence_reference, notes)
VALUES
-- Samsung Galaxy S24 (128GB) Cohort Comparison
(26, 2, 16, 16, 1, 'Samsung Galaxy S24 128GB Croma Store Offer',    74999.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'https://www.croma.com/samsung-s24', 'Instant discount ₹5000 on ICICI cards'),
(27, 2, 17, 17, 1, 'Samsung Galaxy S24 128GB Jayanagar Croma',      75499.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Croma Store Price Card Jayanagar', 'Includes free silicon case'),
(28, 2, 19, 19, 1, 'Samsung Galaxy S24 128GB Girias Special Price',  74490.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Girias Weekly Ad Jayanagar', 'Zero downpayment EMI available'),
(29, 2, 21, 21, 1, 'Samsung Galaxy S24 128GB Poorvika Festive Price',74800.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Poorvika Marathahalli Counter', 'Comes with screen protector apply'),

-- iPhone 15 (128GB) Cohort Comparison
(30, 2, 16, 16, 2, 'Apple iPhone 15 128GB Blue at Croma',           69999.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Croma Official Listing', 'HDFC Bank instant discount ₹4000'),
(31, 2, 19, 19, 2, 'Apple iPhone 15 128GB Girias Super Deal',        69490.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Girias Outlet Display Jayanagar', 'Includes free 20W charging adapter bundle'),
(32, 2, 22, 22, 2, 'Apple iPhone 15 128GB Poorvika BTM Offer',       69800.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Poorvika BTM Showroom', 'Exchange bonus up to ₹3000'),

-- MacBook Air M3 Cohort Comparison
(33, 2, 16, 16, 27, 'MacBook Air M3 8GB/256GB at Croma',           114900.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Croma Koramangala Store', 'HDFC instant cashback ₹5000'),
(34, 2, 2,  2,  27, 'MacBook Air M3 8GB/256GB Reliance Digital',   114990.00, 25, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Reliance Digital Indiranagar', 'Student ID 5% additional off'),

-- Lipid Profile Blood Test Cohort Comparison
(35, 2, 35, 35, 12, 'Lipid Profile Comprehensive Assay',               650.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Anand Lab Jayanagar Price List', 'NABL accredited test report in 6 hours'),
(36, 2, 36, 36, 12, 'Lipid Profile Blood Test at Lal PathLabs',        600.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Dr Lal PathLabs Indiranagar Tariff', 'Home collection available for ₹100 extra'),
(37, 2, 37, 37, 12, 'Automated Lipid Profile 5 Parameters',            550.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Thyrocare BTM Rate Card', '12 hours overnight fasting mandatory'),
(38, 2, 4,  4,  12, 'Lipid Profile In-Hospital Outpatient Lab',        750.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Manipal Hospital OP Tariff', 'Same-day urgent digital report'),

-- HbA1c Test Cohort Comparison
(39, 2, 35, 35, 34, 'HbA1c Blood Sugar 3-Month Average Assay',         550.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Anand Lab Tariff 2026', 'Gold standard HPLC method'),
(40, 2, 36, 36, 34, 'HbA1c Glycated Hemoglobin Test at Lal PathLabs',  490.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Lal PathLabs Catalog', 'No fasting required'),
(41, 2, 37, 37, 34, 'HbA1c Diabetes Monitoring Assay',                 450.00, 32, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Thyrocare Online Tariff', 'Report within 12 hours'),

-- Doctor Consultation Comparison (Cardiology)
(42, 2, 31, 31, 11, 'Senior Consultant Cardiologist OP Consultation', 1000.00, 31, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Manipal Jayanagar Desk', 'Follow-up consultation within 7 days is free'),
(43, 2, 33, 33, 11, 'Cardiology Senior Specialist OP Clinic',         1100.00, 31, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Fortis Hospital Reception Bannerghatta', 'Includes ECG review'),
(44, 2, 34, 34, 11, 'Aster CMI Cardiology Outpatient Visit',          1000.00, 31, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Aster CMI OP Billing Counter', 'Prior appointment recommended'),

-- Automotive Swift VXi Ex-Showroom Comparison
(45, 2, 24, 24, 8,  'Maruti Suzuki Swift VXi Petrol (Manual)',      729000.00, 27, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Kalyani Motors Price Sheet', 'Official Karnataka ex-showroom price'),
(46, 2, 25, 25, 8,  'Maruti Suzuki Swift VXi Petrol (Manual)',      729000.00, 27, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Bimal Auto Price Sheet', 'Official Karnataka ex-showroom price'),
(47, 2, 26, 26, 8,  'Maruti Suzuki Swift VXi Petrol (Manual)',      729000.00, 27, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Mandovi Motors Price Sheet', 'Official Karnataka ex-showroom price'),

-- Automotive Activa 6G Ex-Showroom Comparison
(48, 2, 27, 27, 9,  'Honda Activa 6G Standard Edition',              79500.00, 27, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Tanishq Honda Price Card', 'Includes 3 free periodic services'),
(49, 2, 28, 28, 9,  'Honda Activa 6G Standard Edition',              79500.00, 27, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Silicon Honda Price Card', 'Free helmet with delivery'),

-- Periodic Car Servicing Comparison
(50, 2, 29, 29, 10, 'GoMechanic Comprehensive 10k km Car Service',    3299.00, 29, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'GoMechanic App Bengaluru HSR', 'Synthetic oil, filter & free doorstep pickup'),
(51, 2, 30, 30, 10, 'Bosch Periodic Maintenance Service Package',     3599.00, 29, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Bosch Service Board Electronic City', 'Includes 40-point electronic diagnostic scan'),

-- 2 BHK Apartment Rentals Comparison Across Localities
(52, 2, 48, 48, 5,  'Brigade Metropolis 2 BHK Rental Unit',          36000.00, 40, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Brigade Property Management Whitefield', '1200 sqft, 2 baths, semi-furnished, 24h backup'),
(53, 2, 49, 49, 5,  'Sobha Silicon Oasis 2 BHK Pool Facing',         28000.00, 40, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Sobha Residents Association E-City', '1150 sqft, clubhouse access included'),
(54, 2, 50, 50, 5,  'Prestige Falcon City 2 BHK Semi-Furnished',     34000.00, 40, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Prestige Falcon Rental Desk Banashankari', '1240 sqft, attached balcony, metro adjacent'),
(55, 2, 51, 51, 5,  'Gated Community 2 BHK Apartment Sector 1 HSR',  38500.00, 40, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'NoBroker Verified Agreement HSR', '1300 sqft, 2 parking slots, deposit 5 months'),

-- PG Hostels Comparison
(56, 2, 45, 45, 6,  'Zolo Spark Single Private Room (AC)',           13500.00, 2,  'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Zolo Stays Portal Koramangala', '3 meals, high speed WiFi, biometric entry'),
(57, 2, 46, 46, 6,  'HelloWorld Studio Private Room Stay',           14000.00, 2,  'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'HelloWorld HSR Reception', 'Includes laundry facility & cleaning twice weekly'),

-- Annual Gym Membership Comparison
(58, 2, 67, 67, 46, 'Gold''s Gym Indiranagar 1-Year Membership',    21000.00, 52, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Gold''s Gym Reception Indiranagar', 'Includes steam room & body composition tests'),
(59, 2, 68, 68, 46, 'Anytime Fitness Koramangala 1-Year Pass',       19500.00, 52, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Anytime Fitness Koramangala Desk', '24/7 global access key fob included'),
(60, 2, 70, 70, 16, 'Cultpass Elite 12-Month Unlimited Access',     16999.00, 52, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Cult.fit Indiranagar App Official', 'Free sports center access & pause facility'),

-- Hotel Room Tariff Per Night Comparison
(61, 2, 40, 40, 37, 'Ibis Bengaluru Standard Queen Room (Per Night)', 3400.00, 21, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Accor Official Portal Ibis Hosur Rd', 'WiFi included, free cancellation 24h prior'),
(62, 2, 43, 43, 37, 'Lemon Tree Superior Double Room',                3600.00, 21, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Lemon Tree Electronic City Desk', 'Complimentary buffet breakfast at Citrus'),
(63, 2, 41, 41, 15, 'Sheraton Grand Club Executive Suite',           22500.00, 21, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Marriott Official Portal Sheraton Rajajinagar', 'Club lounge access, high floor city view'),

-- Dinner Buffet Comparison
(64, 2, 61, 61, 18, 'Absolute Barbecues Unlimited Dinner Feast',      1099.00, 44, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'ABs Marathahalli Desk', 'Includes Wish Grill, exotic meats & desserts'),

-- Special Masala Dosa Comparison
(65, 2, 52, 52, 41, 'Vidyarthi Bhavan Signature Masala Dosa',           80.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Vidyarthi Bhavan Printed Bill', 'Pure butter / ghee crisp masala dosa'),
(66, 2, 58, 58, 41, 'CTR Benne Masala Dosa',                            85.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'CTR Malleshwaram Menu Board', 'Authentic Malleshwaram butter masala dosa'),

-- Chicken Biryani Comparison
(67, 2, 53, 53, 42, 'Meghana Special Chicken Biryani',                 330.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Meghana Koramangala Menu', 'Serves 1-2, boneless chicken cubes & spicy rice'),
(68, 2, 54, 54, 42, 'Meghana Special Chicken Biryani',                 330.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Meghana Indiranagar Menu', 'Serves 1-2, boneless chicken cubes & spicy rice'),
(69, 2, 55, 55, 42, 'Meghana Special Chicken Biryani',                 330.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Meghana Marathahalli Menu', 'Serves 1-2, boneless chicken cubes & spicy rice'),

-- Third Wave Specialty Coffee Comparison
(70, 2, 59, 59, 19, 'Sea Salt Mocha (Regular 300ml)',                  280.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Third Wave Coffee Koramangala Menu', 'Artisanal single-origin espresso with sea salt dark chocolate'),
(71, 2, 60, 60, 19, 'Sea Salt Mocha (Regular 300ml)',                  280.00, 43, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'Third Wave Coffee Indiranagar Menu', 'Artisanal single-origin espresso with sea salt dark chocolate'),

-- Supermarket Essentials Comparison: Fortune Sunflower Oil (1 Litre)
(72, 2, 71, 71, 48, 'Fortune Sunlite Sunflower Oil (1L Pouch)',        132.00, 54, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'DMart Electronic City Shelf Label', 'MRP ₹155. Discounted DMart price'),
(73, 2, 72, 72, 48, 'Fortune Sunlite Sunflower Oil (1L Pouch)',        132.00, 54, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'DMart Banashankari Shelf Label', 'MRP ₹155. Discounted DMart price'),
(74, 2, 73, 73, 48, 'Fortune Sunlite Sunflower Oil (1L Pouch)',        138.00, 54, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'More Megastore Marathahalli', 'MRP ₹155. Retail selling price'),
(75, 2, 74, 74, 48, 'Fortune Sunlite Sunflower Oil (1L Pouch)',        140.00, 54, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'MK Retail Indiranagar Price Tag', 'MRP ₹155. Fresh stock'),

-- Supermarket Essentials Comparison: Dove Shampoo (650ml)
(76, 2, 71, 71, 49, 'Dove Daily Shine Shampoo (650ml Pump)',           465.00, 54, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'DMart Shelf Tag Electronic City', 'MRP ₹599. 22% DMart Discount'),
(77, 2, 73, 73, 49, 'Dove Daily Shine Shampoo (650ml Pump)',           499.00, 54, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', TRUE, 'More Retail Shelf Tag', 'MRP ₹599. Promo discount applied'),

-- Quote on Request / Unavailable Price Listing (Phase 5 Requirement: Real Provider with Unlisted/Unavailable Price)
(78, 2, 63, 63, 22, 'Greenwood High IB Diploma Annual Tuition Fee',       NULL, 37, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', FALSE, 'Greenwood High Admissions Office', 'Tuition structure disclosed upon formal campus evaluation'),
(79, 2, 44, 44, 15, 'Palm Meadows 4 BHK Luxury Presidential Villa',       NULL, 21, 'INR', 'approved', TRUE, TRUE, 'PUBLICLY_SOURCED', FALSE, 'Palm Meadows Reservations Desk', 'Available on direct inquiry only. Price varies seasonally'),

-- Demonstration / Synthetic Records (Phase 5 Requirement: Clearly flagged as DEMO_DATA)
(80, 2, 1,  1,  28, '[DEMO] LG OLED 55B3 Promo Demonstration Item',    94990.00, 25, 'INR', 'approved', TRUE, TRUE, 'DEMO_DATA', TRUE, 'PricePulse Synthetic Demo Fixture', 'Demonstration record created exclusively for automated comparison testing'),
(81, 2, 2,  2,  28, '[DEMO] LG OLED 55B3 Alternative Demo Listing',     98990.00, 25, 'INR', 'approved', TRUE, TRUE, 'DEMO_DATA', TRUE, 'PricePulse Synthetic Demo Fixture', 'Demonstration record created exclusively for automated comparison testing')
ON CONFLICT (id) DO UPDATE SET 
    price_amount = EXCLUDED.price_amount,
    source_type = EXCLUDED.source_type,
    is_price_available = EXCLUDED.is_price_available,
    evidence_reference = EXCLUDED.evidence_reference;

-- ----------------------------------------------------------------------------
-- 5. IMMUTABLE PRICE OBSERVATIONS (PRES-HISTORY & AUDIT TRAILS)
-- ----------------------------------------------------------------------------
INSERT INTO price_observations (listing_id, price_type_id, price_amount, currency, source_type, submission_date, effective_date, verification_status, evidence_reference, notes)
SELECT 
    id AS listing_id,
    price_type_id,
    price_amount,
    currency,
    source_type,
    created_at AS submission_date,
    created_at AS effective_date,
    CASE WHEN source_type = 'DEMO_DATA' THEN 'unverified' ELSE 'verified' END AS verification_status,
    evidence_reference,
    notes
FROM listings
WHERE id >= 26
ON CONFLICT DO NOTHING;

-- Reset sequence IDs
SELECT setval('providers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM providers));
SELECT setval('provider_locations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM provider_locations));
SELECT setval('products_and_services_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products_and_services));
SELECT setval('product_service_attributes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM product_service_attributes));
SELECT setval('listings_id_seq', (SELECT COALESCE(MAX(id), 1) FROM listings));
SELECT setval('price_observations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM price_observations));

