from typing import List, Dict, Any, Optional
from decimal import Decimal
import statistics
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, and_, or_
from sqlalchemy.orm import selectinload

from app.models.entities import Listing, ProductService, ProductServiceAttributes, ProviderLocation, Area, PriceType, Subcategory, Category
from app.schemas.fair_price import PriceLabel, ConfidenceLevel

class CohortComparisonResult:
    def __init__(
        self,
        target_listing: Listing,
        comparables: List[Listing],
        cohort_criteria_applied: Dict[str, Any]
    ):
        self.target_listing = target_listing
        self.comparables = comparables
        self.cohort_criteria_applied = cohort_criteria_applied

class ComparisonService:
    """
    Implements Phase 7 Category-Specific Comparison Logic.
    Guarantees strict cohort isolation so only equivalent offerings are compared.
    """

    @classmethod
    async def get_comparable_cohort(
        cls,
        target_listing_id: int,
        db: AsyncSession
    ) -> Optional[CohortComparisonResult]:
        # 1. Fetch target listing with full graph
        stmt = (
            select(Listing)
            .where(Listing.id == target_listing_id, Listing.is_active == True)
            .options(
                selectinload(Listing.user),
                selectinload(Listing.provider),
                selectinload(Listing.provider_location).selectinload(ProviderLocation.area),
                selectinload(Listing.product_service).selectinload(ProductService.attributes_rel),
                selectinload(Listing.product_service).selectinload(ProductService.subcategory).selectinload(Subcategory.category),
                selectinload(Listing.price_type)
            )
        )
        result = await db.execute(stmt)
        target = result.scalars().first()
        if not target:
            return None

        prod = target.product_service
        subcat = prod.subcategory
        cat = subcat.category
        pt = target.price_type
        attrs = prod.attributes_rel.attributes if prod.attributes_rel else {}

        # 2. Build cohort query based on Phase 7 rules
        candidate_stmt = (
            select(Listing)
            .where(
                Listing.is_active == True,
                Listing.moderation_status == "approved",
                Listing.is_price_available == True,
                Listing.price_amount != None
            )
            .join(Listing.product_service)
            .where(ProductService.subcategory_id == subcat.id)
            .join(Listing.price_type)
            .options(
                selectinload(Listing.user),
                selectinload(Listing.provider),
                selectinload(Listing.provider_location).selectinload(ProviderLocation.area),
                selectinload(Listing.product_service).selectinload(ProductService.attributes_rel),
                selectinload(Listing.product_service).selectinload(ProductService.subcategory).selectinload(Subcategory.category),
                selectinload(Listing.price_type)
            )
        )

        cohort_criteria: Dict[str, Any] = {
            "category": cat.name,
            "subcategory": subcat.name,
            "price_type": pt.name
        }

        # Subcategory and Price Type matching rules
        category_slug = cat.slug

        if category_slug == "electronics-appliances":
            # Match same product or exact model & storage
            brand = attrs.get("brand")
            storage = attrs.get("storage")
            if brand and storage:
                cohort_criteria["brand"] = brand
                cohort_criteria["storage"] = storage
                # Match identical catalog item or brand/storage match
                candidate_stmt = candidate_stmt.where(
                    or_(
                        Listing.product_service_id == prod.id,
                        ProductService.name.ilike(f"%{brand}%{storage}%")
                    )
                )
            else:
                candidate_stmt = candidate_stmt.where(Listing.product_service_id == prod.id)

        elif category_slug == "automotive":
            # Ex-showroom can only be compared with Ex-showroom; on-road with on-road
            cohort_criteria["price_type_strict"] = pt.code
            candidate_stmt = candidate_stmt.where(
                Listing.product_service_id == prod.id,
                PriceType.code == pt.code
            )

        elif category_slug == "healthcare":
            # Same test name or specialty
            cohort_criteria["service_spec"] = prod.name
            candidate_stmt = candidate_stmt.where(
                or_(
                    Listing.product_service_id == prod.id,
                    ProductService.name.ilike(f"%{prod.name[:10]}%")
                )
            )

        elif category_slug == "real-estate-rentals":
            # Same property type, bedrooms, and transaction type (rent vs rent, sale vs sale)
            prop_type = attrs.get("property_type")
            bhk = attrs.get("bedrooms")
            cohort_criteria["property_type"] = prop_type
            cohort_criteria["bedrooms"] = bhk
            cohort_criteria["price_type_code"] = pt.code
            candidate_stmt = candidate_stmt.where(
                PriceType.code == pt.code,
                or_(
                    Listing.product_service_id == prod.id,
                    ProductService.name.ilike(f"%{bhk} BHK%")
                )
            )

        elif category_slug == "hotels-accommodation":
            # Same room tier and price type
            cohort_criteria["room_type"] = attrs.get("room_type")
            cohort_criteria["price_type_code"] = pt.code
            candidate_stmt = candidate_stmt.where(
                PriceType.code == pt.code,
                Listing.product_service_id == prod.id
            )

        elif category_slug == "fitness-wellness":
            # Same duration (e.g. 12-month membership)
            duration = attrs.get("duration_months")
            cohort_criteria["duration_months"] = duration
            candidate_stmt = candidate_stmt.where(
                PriceType.code == pt.code,
                Listing.product_service_id == prod.id
            )

        else:
            # Shopping, Restaurants, Home Services, Education
            candidate_stmt = candidate_stmt.where(Listing.product_service_id == prod.id)

        # Execute candidate search
        candidates_res = await db.execute(candidate_stmt.order_by(Listing.price_amount.asc()))
        all_candidates = candidates_res.scalars().all()

        return CohortComparisonResult(
            target_listing=target,
            comparables=all_candidates,
            cohort_criteria_applied=cohort_criteria
        )

