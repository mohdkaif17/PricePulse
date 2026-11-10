import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.anyio
async def test_complete_listing_lifecycle_and_security():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # ---------------------------------------------------------------------
        # 1. AUTHENTICATION & TOKEN ACQUISITION (PHASE 9)
        # ---------------------------------------------------------------------
        # Login as Admin (id: 1)
        res_admin_login = await ac.post("/api/v1/auth/login", json={
            "email_or_username": "admin@pricepulse.in",
            "password": "password123"
        })
        assert res_admin_login.status_code == 200
        admin_data = res_admin_login.json()
        admin_token = admin_data["access_token"]
        assert admin_data["role"] == "admin"

        # Login as Demo User (Kaif, id: 2)
        res_user_login = await ac.post("/api/v1/auth/login", json={
            "email_or_username": "kaif@example.com",
            "password": "password123"
        })
        assert res_user_login.status_code == 200
        user_data = res_user_login.json()
        user_token = user_data["access_token"]
        assert user_data["role"] == "user"

        headers_user = {"Authorization": f"Bearer {user_token}"}
        headers_admin = {"Authorization": f"Bearer {admin_token}"}

        # ---------------------------------------------------------------------
        # 2. USER ADDS NEW LISTING (PHASE 8.1 & PHASE 9)
        # ---------------------------------------------------------------------
        create_payload = {
            "locality_id": 5, # Koramangala
            "category_id": 2, # Electronics
            "subcategory_id": 5, # Smartphones
            "title": "Google Pixel 8 (128GB)",
            "brand_or_provider_name": "Croma Koramangala",
            "price_amount": 59999.00,
            "price_type_id": 25, # Discounted price
            "currency": "INR",
            "source_type": "USER_SUBMITTED",
            "attributes": {"brand": "Google", "model": "Pixel 8", "ram_gb": 8, "storage": "128GB"},
            "notes": "User submitted festive store discount"
        }
        res_create = await ac.post("/api/v1/listings", json=create_payload, headers=headers_user)
        assert res_create.status_code == 201
        created = res_create.json()
        listing_id = created["id"]
        assert created["user_id"] == 2 # Derived securely from token!
        assert created["status"] == "pending" # User submissions require moderation
        assert created["price_amount"] == 59999.00

        # ---------------------------------------------------------------------
        # 3. MY LISTINGS (PHASE 8.4)
        # ---------------------------------------------------------------------
        res_my = await ac.get("/api/v1/my-listings", headers=headers_user)
        assert res_my.status_code == 200
        my_items = res_my.json()
        assert any(item["id"] == listing_id for item in my_items)

        # ---------------------------------------------------------------------
        # 4. SECURITY & OWNERSHIP: UNAUTHORIZED EDIT ATTEMPT (PHASE 9)
        # ---------------------------------------------------------------------
        # Register an attacker / second user
        attacker_username = "other_user_test"
        await ac.post("/api/v1/auth/register", json={
            "username": attacker_username,
            "email": "other_user_test@example.com",
            "password": "password123",
            "full_name": "Other User",
            "role": "user"
        })
        res_other_login = await ac.post("/api/v1/auth/login", json={
            "email_or_username": "other_user_test@example.com",
            "password": "password123"
        })
        other_token = res_other_login.json()["access_token"]
        headers_other = {"Authorization": f"Bearer {other_token}"}

        # Attempt to edit user 2's listing as other user -> MUST RETURN 403 FORBIDDEN!
        res_hacked = await ac.patch(f"/api/v1/listings/{listing_id}", json={"price_amount": 1.00}, headers=headers_other)
        assert res_hacked.status_code == 403

        # Attempt to delete user 2's listing as other user -> MUST RETURN 403 FORBIDDEN!
        res_hacked_del = await ac.delete(f"/api/v1/listings/{listing_id}", headers=headers_other)
        assert res_hacked_del.status_code == 403

        # ---------------------------------------------------------------------
        # 5. USER EDITS OWN LISTING & PRESERVES PRICE OBSERVATIONS (PHASE 8.2)
        # ---------------------------------------------------------------------
        res_edit = await ac.patch(f"/api/v1/listings/{listing_id}", json={
            "price_amount": 57999.00,
            "notes": "Price dropped by 2000"
        }, headers=headers_user)
        assert res_edit.status_code == 200
        assert res_edit.json()["price_amount"] == 57999.00

        # Verify historical price observations preserved
        res_history = await ac.get(f"/api/v1/listings/{listing_id}/price-history")
        assert res_history.status_code == 200
        history = res_history.json()
        assert len(history) >= 2 # Original price + updated price

        # ---------------------------------------------------------------------
        # 6. USER REPORTS A LISTING (PHASE 8.6)
        # ---------------------------------------------------------------------
        report_payload = {
            "reason_category": "misleading_price",
            "details": "The store is charging 2000 extra for warranty without consent"
        }
        res_report = await ac.post(f"/api/v1/listings/{listing_id}/reports", json=report_payload, headers=headers_user)
        assert res_report.status_code == 201
        report = res_report.json()
        assert report["review_status"] == "pending_review"
        assert report["reason_category"] == "misleading_price"

        # ---------------------------------------------------------------------
        # 7. ADMIN MODERATION & REPORTS (PHASE 8.5 & PHASE 9)
        # ---------------------------------------------------------------------
        # Admin views reported listings
        res_admin_reports = await ac.get("/api/v1/admin/reports", headers=headers_admin)
        assert res_admin_reports.status_code == 200
        assert any(r["id"] == report["id"] for r in res_admin_reports.json())

        # Admin approves the listing
        res_mod = await ac.post(f"/api/v1/admin/listings/{listing_id}/moderate", json={
            "action": "approve",
            "admin_notes": "Verified with store catalog"
        }, headers=headers_admin)
        assert res_mod.status_code == 200
        assert res_mod.json()["status"] == "active"
        assert res_mod.json()["moderation_status"] == "approved"

        # Admin checks audit logs
        res_audit = await ac.get("/api/v1/admin/audit-logs", headers=headers_admin)
        assert res_audit.status_code == 200
        assert len(res_audit.json()) > 0

        # ---------------------------------------------------------------------
        # 8. POSTGIS SPATIAL SEARCH (PHASE 10 & 11)
        # ---------------------------------------------------------------------
        # Search near Koramangala (12.9352, 77.6245) with 5km radius
        res_nearby = await ac.get("/api/v1/listings/nearby?latitude=12.9352&longitude=77.6245&radius_km=5.0")
        assert res_nearby.status_code == 200
        nearby_listings = res_nearby.json()
        assert len(nearby_listings) > 0
        assert any(item["area_name"] == "Koramangala" for item in nearby_listings)

        # ---------------------------------------------------------------------
        # 9. COHORT COMPARISON ENGINE (PHASE 7)
        # ---------------------------------------------------------------------
        # Compare Samsung Galaxy S24 (listing id: 1)
        res_compare = await ac.get("/api/v1/listings/1/compare")
        assert res_compare.status_code == 200
        compare_data = res_compare.json()
        assert compare_data["sample_size"] >= 2
        assert compare_data["price_label"] in ["Best Deal Nearby", "Fair Price", "Slightly Expensive", "Overpriced"]
        assert len(compare_data["comparables"]) >= 2

        # ---------------------------------------------------------------------
        # 10. USER SOFT DELETES (ARCHIVES) LISTING (PHASE 8.3)
        # ---------------------------------------------------------------------
        res_del = await ac.delete(f"/api/v1/listings/{listing_id}", headers=headers_user)
        assert res_del.status_code == 200
        assert res_del.json()["success"] is True

        # Verify it no longer appears in public active search
        res_pub = await ac.get("/api/v1/listings?search=Google+Pixel+8")
        assert res_pub.status_code == 200
        assert not any(l["id"] == listing_id for l in res_pub.json())

