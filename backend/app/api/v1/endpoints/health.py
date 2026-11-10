import time
from fastapi import APIRouter, Depends, status
from pydantic import BaseModel
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import text
from app.db.session import get_db

router = APIRouter(tags=["Health"])

class HealthResponse(BaseModel):
    status: str
    database: str
    version: str
    timestamp: int

@router.get("/health", response_model=HealthResponse, status_code=status.HTTP_200_OK)
async def health_check(db: AsyncSession = Depends(get_db)):
    db_status = "healthy"
    try:
        await db.execute(text("SELECT 1"))
    except Exception as e:
        db_status = f"unhealthy: {str(e)}"

    return {
        "status": "online",
        "database": db_status,
        "version": "1.0.0",
        "timestamp": int(time.time())
    }
