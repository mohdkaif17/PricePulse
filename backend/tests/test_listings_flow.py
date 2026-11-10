import asyncio
import os
import sys
from httpx import AsyncClient, ASGITransport

sys.path.append(os.path.join(os.path.dirname(__file__), ".."))
from app.main import app

async def test_listings_full_flow():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Check existing listings in PostgreSQL
        res = await ac.get("/api/v1/listings?limit=100")
        assert res.status_code == 200, f"Get listings failed: {res.text}"
        listings = res.json()
        print(f"Total active listings in PostgreSQL: {len(listings)}")
        assert len(listings) > 0, "No listings found in PostgreSQL!"

        # 2. Get categories and price types
        cats_res = await ac.get("/api/v1/categories")
        assert cats_res.status_code == 200
        cats = cats_res.json()
        assert len(cats) == 10, f"Expected 10 categories, got {len(cats)}"
        first_cat = cats[0]
        first_sub = first_cat["subcategories"][0]

        pt_res = await ac.get(f"/api/v1/price-types?category_id={first_cat['id']}")
        assert pt_res.status_code == 200
        pts = pt_res.json()
        assert len(pts) > 0, f"No price types for category {first_cat['id']}"
        selected_pt = pts[0]

        # 3. Register & login a test user to test Add Listing
        unique_suffix = os.urandom(4).hex()
        email = f"lister_{unique_suffix}@pricepulse.in"
        reg_payload = {
            "username": f"list_{unique_suffix}",
            "email": email,
            "password": "Password123!",
            "full_name": "Listing Tester",
            "phone": "+919876500000"
        }
        reg_res = await ac.post("/api/v1/auth/register", json=reg_payload)
        assert reg_res.status_code == 201
        token = reg_res.json()["access_token"]
        headers = {"Authorization": f"Bearer {token}"}

        # 4. Get localities to pick one
        loc_res = await ac.get("/api/v1/localities")
        assert loc_res.status_code == 200
        localities = loc_res.json()
        assert len(localities) >= 15
        selected_loc = localities[0]

        # 5. Add a listing with user token
        new_listing_payload = {
            "locality_id": selected_loc["id"],
            "category_id": first_cat["id"],
            "subcategory_id": first_sub["id"],
            "title": "Deluxe King Room Test Listing",
            "brand_or_provider_name": "Grand Bengaluru Hotel",
            "price_amount": 4500.0,
            "price_type_id": selected_pt["id"],
            "currency": "INR",
            "source_type": "USER_SUBMITTED",
            "is_price_available": True,
            "notes": "Includes breakfast and free Wi-Fi"
        }
        create_res = await ac.post("/api/v1/listings", json=new_listing_payload, headers=headers)
        assert create_res.status_code == 201, f"Create listing failed: {create_res.text}"
        created = create_res.json()
        print(f"Created listing ID: {created['id']}, status: {created['status']}, user_id: {created.get('user_id')}")
        assert created["custom_title"] == "Deluxe King Room Test Listing"

        # 6. Verify user can view their newly created listing under my-listings
        my_res = await ac.get("/api/v1/my-listings", headers=headers)
        assert my_res.status_code == 200
        my_listings = my_res.json()
        assert any(l["id"] == created["id"] for l in my_listings), "Created listing not found in my-listings"
        print(f"Successfully retrieved newly created listing from my-listings!")

        # 7. Test invalid price type rejection (Category 1 price type with Category 2)
        second_cat = cats[1]
        second_sub = second_cat["subcategories"][0]
        invalid_payload = {
            "locality_id": selected_loc["id"],
            "category_id": second_cat["id"],
            "subcategory_id": second_sub["id"],
            "title": "Smartphone Test",
            "brand_or_provider_name": "Test Brand",
            "price_amount": 15000.0,
            "price_type_id": selected_pt["id"], # Mismatched price type!
            "currency": "INR",
            "source_type": "USER_SUBMITTED"
        }
        invalid_res = await ac.post("/api/v1/listings", json=invalid_payload, headers=headers)
        assert invalid_res.status_code == 400, "Backend should have rejected mismatched category/price_type"
        assert "Invalid price_type_id" in invalid_res.json()["detail"]
        print("Backend correctly rejects category/price-type mismatch as expected!")

if __name__ == "__main__":
    asyncio.run(test_listings_full_flow())
    print(">>> ALL LISTINGS VERIFICATION TESTS PASSED SUCCESSFULLY!")

