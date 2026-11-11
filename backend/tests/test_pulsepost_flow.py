import pytest
import asyncio
from httpx import AsyncClient, ASGITransport
import sys
import os

sys.path.append(os.path.join(os.path.dirname(__file__), ".."))

from app.main import app

@pytest.mark.anyio
async def test_pulsepost_complete_end_to_end_flow():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Register a test user for PulsePost
        suffix = os.urandom(4).hex()
        email = f"pulsepost_{suffix}@pricepulse.in"
        username = f"poster_{suffix}"
        reg_payload = {
            "username": username,
            "email": email,
            "password": "Password123!",
            "full_name": "Bengaluru Contributor",
            "phone": "9876543210"
        }
        res_reg = await ac.post("/api/v1/auth/register", json=reg_payload)
        assert res_reg.status_code == 201, f"Registration failed: {res_reg.text}"
        auth_data = res_reg.json()
        token = auth_data["access_token"]
        user_id = auth_data["user_id"]
        headers = {"Authorization": f"Bearer {token}"}

        # 2. Get valid price types for Category 6 (Real Estate)
        res_pt = await ac.get("/api/v1/price-types?category_id=6")
        assert res_pt.status_code == 200, f"Failed to get price types: {res_pt.text}"
        price_types = res_pt.json()
        assert len(price_types) > 0, "No price types found for Category 6"
        valid_pt_id = price_types[0]["id"]

        # 3. Submit a new PulsePost listing (e.g. Real Estate 2 BHK Flat rental in Koramangala)
        pulsepost_payload = {
            "locality_id": 1,  # Koramangala
            "category_id": 6,  # Real Estate & Rentals
            "subcategory_id": 22,  # Houses for Rent
            "title": f"Prestige Starlight 2 BHK ({suffix})",
            "brand_or_provider_name": "Prestige Group Properties",
            "price_amount": 32000.0,
            "price_type_id": valid_pt_id,
            "currency": "INR",
            "source_type": "USER_SUBMITTED",
            "is_price_available": True,
            "address_line": "5th Block, Koramangala, Bengaluru",
            "evidence_reference": "https://storage.pricepulse.in/evidence/starlight_quote.jpg",
            "attributes": {
                "bhk_type": "2 BHK",
                "furnishing": "Semi-Furnished",
                "built_up_sqft": "1250",
                "latitude": 12.9352,
                "longitude": 77.6245
            },
            "notes": "Includes 24/7 power backup, covered car parking, and maintenance charges."
        }

        res_post = await ac.post("/api/v1/listings", json=pulsepost_payload, headers=headers)
        assert res_post.status_code == 201, f"PulsePost submission failed: {res_post.text}"
        post_data = res_post.json()

        assert post_data["id"] is not None
        assert post_data["user_id"] == user_id
        assert post_data["category_id"] == 6
        assert post_data["category_name"] == "Real Estate & Rentals"
        assert post_data["subcategory_id"] == 22
        assert post_data["status"] == "pending"
        assert post_data["moderation_status"] == "pending"
        assert post_data["source_type"] == "USER_SUBMITTED"
        assert post_data["price_amount"] == 32000.0
        assert post_data["attributes"]["bhk_type"] == "2 BHK"
        listing_id = post_data["id"]
        print(f"[TEST STEP 1 PASSED] PulsePost created successfully with ID #{listing_id} (Status: Pending Moderation)")

        # 3. Retrieve user's submitted listings via /api/v1/my-listings
        res_my = await ac.get("/api/v1/my-listings", headers=headers)
        assert res_my.status_code == 200, f"Failed to retrieve my listings: {res_my.text}"
        my_posts = res_my.json()
        assert len(my_posts) >= 1, "User's submitted PulsePost was not found in /my-listings"

        matching = [p for p in my_posts if p["id"] == listing_id]
        assert len(matching) == 1, f"Created PulsePost #{listing_id} not present in user's My Listings"
        saved_post = matching[0]
        assert saved_post["custom_title"] == pulsepost_payload["title"]
        assert saved_post["moderation_status"] == "pending"
        assert saved_post["category_name"] == "Real Estate & Rentals"
        print(f"[TEST STEP 2 PASSED] My PulsePosts successfully retrieved #{listing_id} showing status: '{saved_post['status']}'")

if __name__ == "__main__":
    asyncio.run(test_pulsepost_complete_end_to_end_flow())

