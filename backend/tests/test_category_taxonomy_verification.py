import pytest
import asyncio
from httpx import AsyncClient, ASGITransport
import sys
import os

# Add backend to path
sys.path.append(os.path.join(os.path.dirname(__file__), ".."))

from app.main import app

EXPECTED_CATEGORIES = {
    1: {"name": "Hotels & Accommodation", "slug": "hotels-accommodation"},
    2: {"name": "Electronics & Appliances", "slug": "electronics-appliances"},
    3: {"name": "Automotive", "slug": "automotive"},
    4: {"name": "Healthcare", "slug": "healthcare"},
    5: {"name": "Education", "slug": "education"},
    6: {"name": "Real Estate & Rentals", "slug": "real-estate-rentals"},
    7: {"name": "Restaurants & Food", "slug": "restaurants-food"},
    8: {"name": "Home Services", "slug": "home-services"},
    9: {"name": "Fitness & Wellness", "slug": "fitness-wellness"},
    10: {"name": "Shopping & Daily Essentials", "slug": "shopping-daily-essentials"},
}

@pytest.mark.anyio
async def test_all_10_categories_integrity():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        res = await ac.get("/api/v1/categories")
        assert res.status_code == 200, f"Failed to get categories: {res.text}"
        cats = res.json()
        assert len(cats) == 10, f"Expected 10 categories, found {len(cats)}"

        cat_by_id = {c["id"]: c for c in cats}
        for expected_id, expected_data in EXPECTED_CATEGORIES.items():
            assert expected_id in cat_by_id, f"Missing category ID {expected_id}"
            actual = cat_by_id[expected_id]
            assert actual["name"] == expected_data["name"], f"Cat {expected_id} name mismatch: {actual['name']} vs {expected_data['name']}"
            assert actual["slug"] == expected_data["slug"], f"Cat {expected_id} slug mismatch: {actual['slug']} vs {expected_data['slug']}"
            assert len(actual["subcategories"]) > 0, f"Cat {expected_id} has no subcategories"
        print("[TEST 1 PASSED] All 10 categories correctly verified in API taxonomy.")

@pytest.mark.anyio
async def test_all_10_categories_listings_isolation():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        total_tested_listings = 0
        category_counts = {}
        for cat_id, cat_info in EXPECTED_CATEGORIES.items():
            res = await ac.get(f"/api/v1/listings?category_id={cat_id}&limit=50")
            assert res.status_code == 200, f"Failed to get listings for category {cat_id}: {res.text}"
            listings = res.json()
            category_counts[cat_info["name"]] = len(listings)
            total_tested_listings += len(listings)
            
            for item in listings:
                assert item["category_id"] == cat_id, (
                    f"Cross-contamination detected! Listing '{item['custom_title']}' has category_id={item['category_id']} "
                    f"when querying category_id={cat_id}"
                )
                assert item["category_name"] == cat_info["name"], (
                    f"Category name mismatch: {item['category_name']} != {cat_info['name']}"
                )

        assert total_tested_listings > 0, "No listings were verified"
        print(f"[TEST 2 PASSED] Verified {total_tested_listings} listings across all 10 categories:")
        for cat_name, count in category_counts.items():
            print(f"   * {cat_name}: {count} listings")

@pytest.mark.anyio
async def test_automotive_vs_real_estate_isolation():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        # 1. Automotive (Cat 3)
        res_auto = await ac.get("/api/v1/listings?category_id=3")
        assert res_auto.status_code == 200
        auto_listings = res_auto.json()
        assert len(auto_listings) > 0, "Automotive listings should not be empty"
        for item in auto_listings:
            assert item["category_id"] == 3
            assert item["category_name"] == "Automotive"
            subcat = item["subcategory_name"].lower()
            assert any(k in subcat for k in ["car", "two-wheeler", "servicing", "parts", "vehicle"]), (
                f"Unexpected subcategory under Automotive: {item['subcategory_name']}"
            )

        # 2. Real Estate (Cat 6)
        res_re = await ac.get("/api/v1/listings?category_id=6")
        assert res_re.status_code == 200
        re_listings = res_re.json()
        assert len(re_listings) > 0, "Real estate listings should not be empty"
        for item in re_listings:
            assert item["category_id"] == 6
            assert item["category_name"] == "Real Estate & Rentals"
            subcat = item["subcategory_name"].lower()
            assert any(k in subcat for k in ["rent", "pg", "apartment", "sale", "flat", "commercial", "property"]), (
                f"Unexpected subcategory under Real Estate: {item['subcategory_name']}"
            )
        print("[TEST 3 PASSED] Automotive vs Real Estate complete isolation verified.")

@pytest.mark.anyio
async def test_subcategory_filtering():
    transport = ASGITransport(app=app)
    async with AsyncClient(transport=transport, base_url="http://test") as ac:
        res_subs = await ac.get("/api/v1/categories/2/subcategories")
        assert res_subs.status_code == 200
        subs = res_subs.json()
        assert len(subs) > 0
        
        first_sub = subs[0]
        res_filtered = await ac.get(f"/api/v1/listings?subcategory_id={first_sub['id']}")
        assert res_filtered.status_code == 200
        filtered_listings = res_filtered.json()
        for item in filtered_listings:
            assert item["subcategory_id"] == first_sub["id"]
            assert item["subcategory_name"] == first_sub["name"]
        print(f"[TEST 4 PASSED] Subcategory filtering verified ({len(filtered_listings)} listings in '{first_sub['name']}').")

async def run_all():
    print("=" * 60)
    print("RUNNING CATEGORY & LISTING ISOLATION VERIFICATION SUITE")
    print("=" * 60)
    await test_all_10_categories_integrity()
    await test_all_10_categories_listings_isolation()
    await test_automotive_vs_real_estate_isolation()
    await test_subcategory_filtering()
    print("=" * 60)
    print("ALL TESTS PASSED SUCCESSFULLY! ZERO MISMATCHES FOUND.")
    print("=" * 60)

if __name__ == "__main__":
    asyncio.run(run_all())
