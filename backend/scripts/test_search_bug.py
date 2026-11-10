import asyncio
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from app.db.session import AsyncSessionLocal
from app.api.v1.endpoints.listings import get_listings

async def test_search():
    async with AsyncSessionLocal() as db:
        try:
            results = await get_listings(
                locality_id=None,
                category_id=2,
                subcategory_id=None,
                search="Samsung",
                sort_by="newest",
                limit=50,
                offset=0,
                db=db
            )
            print("Search succeeded! Results:", len(results))
        except Exception as e:
            print("Search failed with error:", type(e), e)

if __name__ == "__main__":
    asyncio.run(test_search())
