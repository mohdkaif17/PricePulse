from fastapi import APIRouter
from app.api.v1.endpoints import health, fair_price, master_data, listings, auth, admin, users

api_router = APIRouter()
api_router.include_router(health.router)
api_router.include_router(fair_price.router)
api_router.include_router(auth.router)
api_router.include_router(users.router)
api_router.include_router(master_data.router)
api_router.include_router(listings.router)
api_router.include_router(admin.router)
