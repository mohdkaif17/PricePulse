import asyncio
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db.session import AsyncSessionLocal
from app.api.v1.endpoints.listings import get_listings

async def test_all_categories_backend():
    async with AsyncSessionLocal() as db:
        print("Testing backend listing queries for all 10 categories:")
        for cat_id in range(1, 11):
            results = await get_listings(
                locality_id=None,
                category_id=cat_id,
                subcategory_id=None,
                search=None,
                sort_by="newest",
                limit=50,
                offset=0,
                db=db
            )
            print(f"Cat ID {cat_id:2d}: Found {len(results):2d} listings.")
            for r in results[:2]:
                print(f"    - [{r.category_name} / {r.subcategory_name}] {r.custom_title} (Rs {r.price_amount})")

if __name__ == "__main__":
    asyncio.run(test_all_categories_backend())
