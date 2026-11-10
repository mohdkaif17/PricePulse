import pytest
from httpx import AsyncClient, ASGITransport
from app.main import app

@pytest.mark.anyio
async def test_dataset_summary():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/api/v1/meta/summary")
    assert response.status_code == 200
    data = response.json()
    assert data["total_localities"] == 15
    assert data["total_categories"] == 10
    assert data["total_subcategories"] == 38
    assert data["total_listings"] >= 25
    assert "Whitefield" in data["active_localities"]
    assert "Koramangala" in data["active_localities"]

@pytest.mark.anyio
async def test_get_localities():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/api/v1/localities")
    assert response.status_code == 200
    localities = response.json()
    assert len(localities) == 15
    loc_names = [l["name"] for l in localities]
    assert "Indiranagar" in loc_names
    assert "HSR Layout" in loc_names
    assert "Malleshwaram" in loc_names
    assert "Banashankari" in loc_names
    assert "Yelahanka" in loc_names

@pytest.mark.anyio
async def test_get_categories_and_subcategories():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/api/v1/categories")
    assert response.status_code == 200
    categories = response.json()
    assert len(categories) == 10
    cat_names = [c["name"] for c in categories]
    assert "Hotels & Accommodation" in cat_names
    assert "Electronics & Appliances" in cat_names
    assert "Automotive" in cat_names
    assert "Healthcare" in cat_names
    assert "Education" in cat_names
    assert "Real Estate & Rentals" in cat_names
    assert "Restaurants & Food" in cat_names
    assert "Home Services" in cat_names
    assert "Fitness & Wellness" in cat_names
    assert "Shopping & Daily Essentials" in cat_names

@pytest.mark.anyio
async def test_listing_crud_lifecycle():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # 1. Create a listing
        payload = {
            "user_id": 2,
            "locality_id": 5, # Koramangala
            "category_id": 2, # Electronics
            "subcategory_id": 5, # Smartphones
            "title": "OnePlus 12 (256GB)",
            "brand_or_provider_name": "OnePlus Experience Store",
            "price_amount": 64999.00,
            "price_type_id": 4, # Discounted price
            "currency": "INR",
            "attributes": {"ram_gb": 12, "storage": "256GB", "color": "Flowy Emerald"},
            "notes": "Launch offer discount included"
        }
        res_create = await ac.post("/api/v1/listings", json=payload)
        assert res_create.status_code == 201
        created = res_create.json()
        listing_id = created["id"]
        assert created["custom_title"] == "OnePlus 12 (256GB)"
        assert created["price_amount"] == 64999.00
        assert created["area_name"] == "Koramangala"

        # 2. Update listing
        update_payload = {
            "price_amount": 62999.00,
            "notes": "Special weekend price drop"
        }
        res_update = await ac.put(f"/api/v1/listings/{listing_id}", json=update_payload)
        assert res_update.status_code == 200
        updated = res_update.json()
        assert updated["price_amount"] == 62999.00
        assert "weekend" in updated["notes"]

        # 3. Moderate listing (Admin action)
        mod_payload = {
            "moderation_status": "approved",
            "notes": "Verified by Admin"
        }
        res_mod = await ac.post(f"/api/v1/listings/{listing_id}/moderate", json=mod_payload)
        assert res_mod.status_code == 200
        assert res_mod.json()["moderation_status"] == "approved"

        # 4. Delete listing
        res_del = await ac.delete(f"/api/v1/listings/{listing_id}")
        assert res_del.status_code == 200
        assert res_del.json()["success"] is True

