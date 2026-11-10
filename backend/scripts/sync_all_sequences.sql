SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));
SELECT setval('providers_id_seq', (SELECT COALESCE(MAX(id), 1) FROM providers));
SELECT setval('provider_locations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM provider_locations));
SELECT setval('products_and_services_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products_and_services));
SELECT setval('product_service_attributes_id_seq', (SELECT COALESCE(MAX(id), 1) FROM product_service_attributes));
SELECT setval('listings_id_seq', (SELECT COALESCE(MAX(id), 1) FROM listings));
SELECT setval('price_observations_id_seq', (SELECT COALESCE(MAX(id), 1) FROM price_observations));
SELECT setval('audit_logs_id_seq', (SELECT COALESCE(MAX(id), 1) FROM audit_logs));

