import pytest
from datetime import datetime, timezone, timedelta
from decimal import Decimal
from httpx import AsyncClient, ASGITransport
from app.main import app
from app.schemas.fair_price import AssetCategory, PriceLabel, ConfidenceLevel, ComparableObservation
from app.services.fair_price_engine import FairPriceEngine

def make_obs(price_val: str, days_ago: int = 1, price_per_sqft: str = None) -> ComparableObservation:
    dt = datetime.now(timezone.utc) - timedelta(days=days_ago)
    return ComparableObservation(
        price=Decimal(price_val),
        timestamp=dt,
        price_per_sqft=Decimal(price_per_sqft) if price_per_sqft else None
    )

def test_engine_insufficient_data():
    # Only 2 observations (< minimum 3)
    obs = [make_obs("10000"), make_obs("10500")]
    result = FairPriceEngine.evaluate(Decimal("10000"), obs)
    assert result.price_label == PriceLabel.INSUFFICIENT_DATA
    assert result.sample_size == 2

def test_engine_stale_data():
    # 3 observations, but older than 90 days
    obs = [make_obs("10000", days_ago=95), make_obs("10200", days_ago=100), make_obs("10500", days_ago=120)]
    result = FairPriceEngine.evaluate(Decimal("10000"), obs)
    assert result.price_label == PriceLabel.INSUFFICIENT_DATA
    assert result.sample_size == 0

def test_engine_best_deal_nearby():
    # Comparables median = 10,000. Listed = 9,000 (-10% <= -5%)
    obs = [make_obs("9800"), make_obs("10000"), make_obs("10200"), make_obs("10500")]
    result = FairPriceEngine.evaluate(Decimal("9000"), obs)
    assert result.price_label == PriceLabel.BEST_DEAL_NEARBY
    assert result.price_deviation_percent < -5.0
    assert result.reference_price == Decimal("10100.00")

def test_engine_fair_price():
    # Comparables median = 10,000. Listed = 10,200 (+2% within [-5%, +5%])
    obs = [make_obs("9500"), make_obs("10000"), make_obs("10500")]
    result = FairPriceEngine.evaluate(Decimal("10200"), obs)
    assert result.price_label == PriceLabel.FAIR_PRICE
    assert result.reference_price == Decimal("10000.00")

def test_engine_slightly_expensive():
    # Comparables median = 10,000. Listed = 11,000 (+10% within (+5%, +15%])
    obs = [make_obs("9500"), make_obs("10000"), make_obs("10500")]
    result = FairPriceEngine.evaluate(Decimal("11000"), obs)
    assert result.price_label == PriceLabel.SLIGHTLY_EXPENSIVE
    assert result.price_deviation_percent == 10.0

def test_engine_overpriced():
    # Comparables median = 10,000. Listed = 12,500 (+25% > +15%)
    obs = [make_obs("9500"), make_obs("10000"), make_obs("10500")]
    result = FairPriceEngine.evaluate(Decimal("12500"), obs)
    assert result.price_label == PriceLabel.OVERPRICED
    assert result.price_deviation_percent == 25.0

def test_engine_real_estate_evaluation():
    # Real estate with sqft metrics
    obs = [
        make_obs("25000", days_ago=10, price_per_sqft="25.00"),
        make_obs("26000", days_ago=20, price_per_sqft="26.00"),
        make_obs("24000", days_ago=30, price_per_sqft="24.00"),
    ]
    result = FairPriceEngine.evaluate(Decimal("25000"), obs, category=AssetCategory.REAL_ESTATE)
    assert result.price_label == PriceLabel.FAIR_PRICE
    assert "median_price_per_sqft" in result.metrics
    assert result.metrics["median_price_per_sqft"] == 25.00

@pytest.mark.anyio
async def test_fair_price_api_endpoint():
    payload = {
        "listed_price": "72000.00",
        "category": "standard",
        "comparables": [
            {"price": "79999.00", "timestamp": "2026-10-15T00:00:00Z"},
            {"price": "77999.00", "timestamp": "2026-10-16T00:00:00Z"},
            {"price": "76999.00", "timestamp": "2026-10-18T00:00:00Z"},
            {"price": "74999.00", "timestamp": "2026-10-20T00:00:00Z"}
        ]
    }
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.post("/api/v1/fair-price/evaluate", json=payload)
    
    assert response.status_code == 200
    data = response.json()
    assert data["price_label"] == "Best Deal Nearby"
    assert data["sample_size"] == 4
    assert float(data["price_deviation_percent"]) < 0
    assert "limitations" in data
