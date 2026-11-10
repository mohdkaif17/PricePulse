from typing import List, Optional
from decimal import Decimal
from fastapi import APIRouter, HTTPException, Query, status
from pydantic import BaseModel, Field
from app.schemas.fair_price import (
    AssetCategory,
    PriceLabel,
    ConfidenceLevel,
    ComparableObservation,
    FairPriceAnalysisResult
)
from app.services.fair_price_engine import FairPriceEngine

router = APIRouter(prefix="/fair-price", tags=["Fair Price Intelligence"])

class FairPriceEvaluationRequest(BaseModel):
    listed_price: Decimal = Field(..., gt=0, description="The listed asking price of the product or property")
    category: AssetCategory = Field(default=AssetCategory.STANDARD, description="Category of the asset (standard or real_estate)")
    comparables: List[ComparableObservation] = Field(..., description="List of recorded comparable observations")

@router.post("/evaluate", response_model=FairPriceAnalysisResult, status_code=status.HTTP_200_OK)
async def evaluate_fair_price(payload: FairPriceEvaluationRequest):
    """
    Evaluates whether a given listed price is reasonable compared with suitable alternatives.
    Returns:
    - listed_price
    - reference_price (median benchmark)
    - price_deviation_amount and percent
    - price_label (e.g. Best Deal Nearby, Fair Price, Slightly Expensive, Overpriced, Insufficient Data)
    - confidence_level and data freshness
    """
    result = FairPriceEngine.evaluate(
        listed_price=payload.listed_price,
        observations=payload.comparables,
        category=payload.category
    )
    return result
