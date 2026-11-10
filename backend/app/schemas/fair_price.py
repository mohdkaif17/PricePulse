import math
from datetime import datetime, timezone
from decimal import Decimal
from enum import Enum
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field

class AssetCategory(str, Enum):
    STANDARD = "standard"          # Electronics, Vehicles, Healthcare, Restaurants, Education
    REAL_ESTATE = "real_estate"    # Rent & Real Estate

class PriceLabel(str, Enum):
    BEST_DEAL_NEARBY = "Best Deal Nearby"
    FAIR_PRICE = "Fair Price"
    SLIGHTLY_EXPENSIVE = "Slightly Expensive"
    OVERPRICED = "Overpriced"
    INSUFFICIENT_DATA = "Insufficient Data"

class ConfidenceLevel(str, Enum):
    HIGH = "high"
    MEDIUM = "medium"
    LOW = "low"
    NONE = "none"

class ComparableObservation(BaseModel):
    price: Decimal
    timestamp: datetime
    is_verified: bool = False
    source: str = "advertised"
    distance_km: Optional[float] = None
    
    # Real estate specialized metrics
    area_sqft: Optional[float] = None
    price_per_sqft: Optional[Decimal] = None
    bedrooms: Optional[int] = None
    property_type: Optional[str] = None
    transaction_type: Optional[str] = None  # "rent" or "sale"

class FairPriceAnalysisResult(BaseModel):
    listed_price: Decimal
    reference_price: Optional[Decimal] = None
    price_deviation_amount: Optional[Decimal] = None
    price_deviation_percent: Optional[float] = None
    price_label: PriceLabel
    confidence_level: ConfidenceLevel
    sample_size: int
    data_freshness_days: Optional[int] = None
    explanation: str
    limitations: List[str]
    metrics: Dict[str, Any] = Field(default_factory=dict)
