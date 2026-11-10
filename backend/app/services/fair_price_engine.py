import statistics
from datetime import datetime, timezone
from decimal import Decimal, ROUND_HALF_UP
from typing import List, Optional
from app.schemas.fair_price import (
    AssetCategory,
    PriceLabel,
    ConfidenceLevel,
    ComparableObservation,
    FairPriceAnalysisResult
)

class FairPriceEngine:
    """
    Transparent, heuristic and statistical valuation engine for PricePulse.
    Distinguishes Standard Retail Products from Rent & Real Estate.
    """

    # Minimum evidence thresholds
    MIN_OBSERVATIONS_FOR_CONFIDENCE = 3
    MAX_RECENCY_DAYS_STANDARD = 90
    MAX_RECENCY_DAYS_REAL_ESTATE = 180

    # Deviation Thresholds (Percentage from reference)
    # - Deviation <= -5.0%: Best Deal Nearby (below market)
    # - -5.0% < Deviation <= +5.0%: Fair Price (market aligned)
    # - +5.0% < Deviation <= +15.0%: Slightly Expensive (modest premium)
    # - Deviation > +15.0%: Overpriced (substantially above alternatives)
    BEST_DEAL_THRESHOLD = -5.0
    FAIR_PRICE_UPPER_THRESHOLD = 5.0
    SLIGHTLY_EXPENSIVE_UPPER_THRESHOLD = 15.0

    @classmethod
    def evaluate(
        cls,
        listed_price: Decimal,
        observations: List[ComparableObservation],
        category: AssetCategory = AssetCategory.STANDARD,
        current_time: Optional[datetime] = None
    ) -> FairPriceAnalysisResult:
        """
        Evaluates a listed price against a cohort of comparable observations.
        """
        if current_time is None:
            current_time = datetime.now(timezone.utc)

        limitations = [
            "This analysis provides heuristic market context only and is not a certified professional appraisal or valuation.",
            "Prices are based on advertised and recorded observation data and do not account for unlisted discounts or negotiating margins."
        ]

        if not observations or listed_price <= Decimal("0"):
            return FairPriceAnalysisResult(
                listed_price=listed_price,
                reference_price=None,
                price_deviation_amount=None,
                price_deviation_percent=None,
                price_label=PriceLabel.INSUFFICIENT_DATA,
                confidence_level=ConfidenceLevel.NONE,
                sample_size=0,
                data_freshness_days=None,
                explanation="No comparable price observations were found for this item.",
                limitations=limitations,
                metrics={}
            )

        # 1. Filter out stale data based on category recency threshold
        max_days = cls.MAX_RECENCY_DAYS_REAL_ESTATE if category == AssetCategory.REAL_ESTATE else cls.MAX_RECENCY_DAYS_STANDARD
        fresh_observations: List[ComparableObservation] = []
        oldest_fresh_days = 0
        newest_fresh_days = 999999

        for obs in observations:
            obs_dt = obs.timestamp if obs.timestamp.tzinfo else obs.timestamp.replace(tzinfo=timezone.utc)
            age_days = (current_time - obs_dt).days
            if age_days <= max_days:
                fresh_observations.append(obs)
                if age_days > oldest_fresh_days:
                    oldest_fresh_days = age_days
                if age_days < newest_fresh_days:
                    newest_fresh_days = age_days

        sample_size = len(fresh_observations)

        # If after freshness filter, observations are below minimum threshold
        if sample_size < cls.MIN_OBSERVATIONS_FOR_CONFIDENCE:
            return FairPriceAnalysisResult(
                listed_price=listed_price,
                reference_price=None,
                price_deviation_amount=None,
                price_deviation_percent=None,
                price_label=PriceLabel.INSUFFICIENT_DATA,
                confidence_level=ConfidenceLevel.LOW if sample_size > 0 else ConfidenceLevel.NONE,
                sample_size=sample_size,
                data_freshness_days=newest_fresh_days if sample_size > 0 else None,
                explanation=(
                    f"Insufficient recent data: Found {sample_size} valid observation(s) within "
                    f"{max_days} days. At least {cls.MIN_OBSERVATIONS_FOR_CONFIDENCE} comparable listings are required."
                ),
                limitations=limitations,
                metrics={"unfiltered_sample_size": len(observations)}
            )

        # 2. Extract values based on Asset Category
        if category == AssetCategory.REAL_ESTATE:
            # For real estate, reference price comparison uses Price Per SqFt if available
            prices = [obs.price for obs in fresh_observations]
            ppsf_list = [obs.price_per_sqft for obs in fresh_observations if obs.price_per_sqft is not None]
            
            # Median price
            float_prices = [float(p) for p in prices]
            median_val = Decimal(str(statistics.median(float_prices))).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            mean_val = Decimal(str(statistics.mean(float_prices))).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            
            reference_price = median_val
            metrics = {
                "median_price": float(median_val),
                "mean_price": float(mean_val),
                "min_price": float(min(float_prices)),
                "max_price": float(max(float_prices)),
            }
            if ppsf_list:
                float_ppsf = [float(p) for p in ppsf_list]
                metrics["median_price_per_sqft"] = round(statistics.median(float_ppsf), 2)
                metrics["mean_price_per_sqft"] = round(statistics.mean(float_ppsf), 2)
        else:
            # Standard retail: Median price is resistant to outlier listing errors
            float_prices = [float(obs.price) for obs in fresh_observations]
            median_val = Decimal(str(statistics.median(float_prices))).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            mean_val = Decimal(str(statistics.mean(float_prices))).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
            
            reference_price = median_val
            metrics = {
                "median_price": float(median_val),
                "mean_price": float(mean_val),
                "min_price": float(min(float_prices)),
                "max_price": float(max(float_prices))
            }

        # 3. Calculate deviation
        deviation_amount = (listed_price - reference_price).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)
        deviation_percent = round((float(deviation_amount) / float(reference_price)) * 100.0, 2)

        # 4. Classify price label
        if deviation_percent <= cls.BEST_DEAL_THRESHOLD:
            label = PriceLabel.BEST_DEAL_NEARBY
            explanation = f"Listed at {abs(deviation_percent)}% below the market median reference (₹{reference_price:,.2f})."
        elif deviation_percent <= cls.FAIR_PRICE_UPPER_THRESHOLD:
            label = PriceLabel.FAIR_PRICE
            explanation = f"Price is consistent with the market median reference (₹{reference_price:,.2f}) within ±5%."
        elif deviation_percent <= cls.SLIGHTLY_EXPENSIVE_UPPER_THRESHOLD:
            label = PriceLabel.SLIGHTLY_EXPENSIVE
            explanation = f"Listed at {deviation_percent}% above the market median reference (₹{reference_price:,.2f})."
        else:
            label = PriceLabel.OVERPRICED
            explanation = f"Listed at {deviation_percent}% higher than prevailing comparable listings (₹{reference_price:,.2f})."

        # 5. Determine confidence level
        if sample_size >= 8 and newest_fresh_days <= 14:
            confidence = ConfidenceLevel.HIGH
        elif sample_size >= 4 and newest_fresh_days <= 30:
            confidence = ConfidenceLevel.MEDIUM
        else:
            confidence = ConfidenceLevel.LOW

        return FairPriceAnalysisResult(
            listed_price=listed_price,
            reference_price=reference_price,
            price_deviation_amount=deviation_amount,
            price_deviation_percent=deviation_percent,
            price_label=label,
            confidence_level=confidence,
            sample_size=sample_size,
            data_freshness_days=newest_fresh_days,
            explanation=explanation,
            limitations=limitations,
            metrics=metrics
        )
