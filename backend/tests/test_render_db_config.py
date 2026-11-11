import pytest
import os
import sys

# Ensure backend root is in Python module search path
sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from sqlalchemy import text
from httpx import AsyncClient, ASGITransport

from app.core.config import Settings
from app.db.session import AsyncSessionLocal, engine
from app.main import app

def test_local_default_urls():
    """Verify that when DATABASE_URL is not set, local configuration is used."""
    s = Settings(
        DATABASE_URL=None,
        DB_HOST="localhost",
        DB_PORT=5432,
        DB_NAME="price_comparison",
        DB_USER="postgres",
        DB_PASSWORD="MyPassword@123",
        DB_POOL_SIZE=7,
        DB_MAX_OVERFLOW=14
    )
    assert s.require_ssl is False
    assert s.DB_POOL_SIZE == 7
    assert s.DB_MAX_OVERFLOW == 14
    assert s.async_database_url == "postgresql+asyncpg://postgres:MyPassword%40123@localhost:5432/price_comparison"
    assert s.sync_database_url == "postgresql+psycopg://postgres:MyPassword%40123@localhost:5432/price_comparison"

def test_render_postgres_url_normalization():
    """Verify Render's postgres:// scheme is transformed to postgresql+asyncpg and postgresql+psycopg."""
    render_raw_url = "postgres://pricepulse_user:secret_pass@dpg-abc12345-a.oregon-postgres.render.com/pricepulse_db"
    s = Settings(DATABASE_URL=render_raw_url)
    
    assert s.require_ssl is True
    assert s.async_database_url == "postgresql+asyncpg://pricepulse_user:secret_pass@dpg-abc12345-a.oregon-postgres.render.com/pricepulse_db"
    assert s.sync_database_url == "postgresql+psycopg://pricepulse_user:secret_pass@dpg-abc12345-a.oregon-postgres.render.com/pricepulse_db"

def test_render_sslmode_query_stripping_for_asyncpg():
    """Verify that ?sslmode=require is stripped for asyncpg and preserved for psycopg."""
    render_ssl_url = "postgresql://user:pass@dpg-render-postgres.render.com/mydb?sslmode=require&target_session_attrs=read-write"
    s = Settings(DATABASE_URL=render_ssl_url)
    
    assert s.require_ssl is True
    # asyncpg must NOT have sslmode in query string
    assert "sslmode" not in s.async_database_url
    assert "target_session_attrs=read-write" in s.async_database_url
    assert s.async_database_url.startswith("postgresql+asyncpg://")
    
    # psycopg CAN have sslmode
    assert "sslmode=require" in s.sync_database_url
    assert s.sync_database_url.startswith("postgresql+psycopg://")

def test_ssl_override_options():
    """Verify DB_SSL explicitly forces SSL on or off regardless of URL."""
    s_forced_off = Settings(
        DATABASE_URL="postgres://user:pass@dpg-render-postgres.render.com/mydb",
        DB_SSL=False
    )
    assert s_forced_off.require_ssl is False

    s_forced_on = Settings(
        DATABASE_URL="postgresql://user:pass@127.0.0.1:5432/mydb",
        DB_SSL=True
    )
    assert s_forced_on.require_ssl is True

@pytest.mark.anyio
async def test_live_database_session():
    """Verify that AsyncSessionLocal connects to the live local database successfully."""
    try:
        async with AsyncSessionLocal() as session:
            result = await session.execute(text("SELECT 1 AS alive"))
            row = result.scalar()
            assert row == 1
    finally:
        await engine.dispose()

@pytest.mark.anyio
async def test_health_endpoint_integration():
    """Verify /api/v1/health reports healthy database with the updated session configuration."""
    try:
        transport = ASGITransport(app=app)
        async with AsyncClient(transport=transport, base_url="http://test") as ac:
            response = await ac.get("/api/v1/health")
            assert response.status_code == 200
            data = response.json()
            assert data["status"] == "online"
            assert data["database"] == "healthy"
    finally:
        await engine.dispose()
