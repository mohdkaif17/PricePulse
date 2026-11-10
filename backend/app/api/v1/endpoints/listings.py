from datetime import datetime, timezone
from decimal import Decimal
from typing import List, Optional, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, Query, status, Request
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func, or_, text
from sqlalchemy.orm import selectinload

from app.db.session import get_db
from app.models.entities import (
    Listing, Provider, ProviderLocation, ProductService, ProductServiceAttributes,
    PriceType, PriceObservation, Area, Subcategory, Category, User,
    ListingReport, AuditLog
)
from app.schemas.listings import (
    ListingCreateRequest, ListingUpdateRequest, ListingResponse,
    ComparisonResponse, PriceObservationCreateRequest, PriceObservationResponse
)
from app.schemas.reports import ListingReportCreateRequest, ListingReportResponse
from app.services.comparison_service import ComparisonService
from app.api.deps import get_current_user, get_optional_current_user

router = APIRouter(tags=["Listings"])

def build_listing_response(listing: Listing) -> ListingResponse:
    area = listing.provider_location.area
    provider = listing.provider
    prod = listing.product_service
    subcat = prod.subcategory
    cat = subcat.category
    pt = listing.price_type

    attrs = {}
    if prod.attributes_rel and prod.attributes_rel.attributes:
        attrs = prod.attributes_rel.attributes

    user_name = listing.user.full_name if listing.user else "PricePulse Verified"

    return ListingResponse(
        id=listing.id,
        user_id=listing.user_id,
        user_name=user_name,
        custom_title=listing.custom_title or prod.name,
        price_amount=float(listing.price_amount) if listing.price_amount is not None else None,
        is_price_available=listing.is_price_available,
        currency=listing.currency,
        status=listing.status,
        moderation_status=listing.moderation_status,
        rejection_reason=listing.rejection_reason,
        source_type=listing.source_type,
        price_type_code=pt.code,
        price_type_name=pt.name,
        category_id=cat.id,
        category_name=cat.name,
        category_slug=cat.slug,
        subcategory_id=subcat.id,
        subcategory_name=subcat.name,
        subcategory_slug=subcat.slug,
        provider_id=provider.id,
        provider_name=provider.name,
        provider_location_id=listing.provider_location_id,
        branch_name=listing.provider_location.branch_name,
        locality_id=area.id,
        area_name=area.name,
        address_line=listing.provider_location.address_line,
        latitude=float(listing.provider_location.latitude),
        longitude=float(listing.provider_location.longitude),
        is_available=listing.is_available,
        evidence_reference=listing.evidence_reference,
        attributes=attrs,
        notes=listing.notes,
        created_at=listing.created_at,
        updated_at=listing.updated_at
    )

# -----------------------------------------------------------------------------
# 1. PUBLIC LISTING DISCOVERY & SPATIAL SEARCH (PHASE 10 & 11)
# -----------------------------------------------------------------------------

@router.get("/listings", response_model=List[ListingResponse])
async def get_listings(
    locality_id: Optional[int] = Query(None, description="Filter by Bengaluru locality ID"),
    category_id: Optional[int] = Query(None, description="Filter by Category ID"),
    subcategory_id: Optional[int] = Query(None, description="Filter by Subcategory ID"),
    search: Optional[str] = Query(None, description="Keyword search in title, product, provider"),
    sort_by: Optional[str] = Query("newest", pattern="^(newest|price_asc|price_desc)$"),
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0),
    db: AsyncSession = Depends(get_db)
):
    """
    Public listing discovery across Bengaluru. Only returns active, approved listings.
    """
    stmt = (
        select(Listing)
        .where(
            Listing.is_active == True,
            Listing.status == "active"
        )
        .options(
            selectinload(Listing.user),
            selectinload(Listing.provider),
            selectinload(Listing.provider_location).selectinload(ProviderLocation.area),
            selectinload(Listing.product_service).selectinload(ProductService.attributes_rel),
            selectinload(Listing.product_service).selectinload(ProductService.subcategory).selectinload(Subcategory.category),
            selectinload(Listing.price_type)
        )
    )

    if locality_id:
        stmt = stmt.join(Listing.provider_location).where(ProviderLocation.area_id == locality_id)
    if subcategory_id:
        stmt = stmt.join(Listing.product_service).where(ProductService.subcategory_id == subcategory_id)
    elif category_id:
        stmt = (
            stmt.join(Listing.product_service)
            .join(ProductService.subcategory)
            .where(Subcategory.category_id == category_id)
        )

    if search and search.strip():
        term = f"%{search.strip()}%"
        stmt = stmt.join(Listing.product_service).where(
            or_(
                Listing.custom_title.ilike(term),
                ProductService.name.ilike(term),
                Listing.notes.ilike(term)
            )
        )

    if sort_by == "price_asc":
        stmt = stmt.order_by(Listing.price_amount.asc().nulls_last())
    elif sort_by == "price_desc":
        stmt = stmt.order_by(Listing.price_amount.desc().nulls_last())
    else:
        stmt = stmt.order_by(Listing.id.desc())

    stmt = stmt.limit(limit).offset(offset)
    result = await db.execute(stmt)
    listings = result.scalars().all()

    return [build_listing_response(l) for l in listings]

@router.get("/listings/nearby", response_model=List[ListingResponse])
async def get_nearby_listings(
    latitude: float = Query(..., ge=-90, le=90, description="User current latitude (e.g. 12.9352)"),
    longitude: float = Query(..., ge=-180, le=180, description="User current longitude (e.g. 77.6245)"),
    radius_km: float = Query(5.0, gt=0, le=50.0, description="Search radius in kilometers"),
    category_id: Optional[int] = Query(None),
    limit: int = Query(50, ge=1, le=100),
    db: AsyncSession = Depends(get_db)
):
    """
    PostGIS spatial distance search: finds listings within radius_km from user GPS coordinates.
    """
    radius_meters = radius_km * 1000.0

    # PostGIS ST_DWithin query
    sql = text("""
        SELECT l.id
        FROM listings l
        JOIN provider_locations pl ON l.provider_location_id = pl.id
        JOIN products_and_services p ON l.product_service_id = p.id
        JOIN subcategories sc ON p.subcategory_id = sc.id
        WHERE l.is_active = TRUE
          AND l.status = 'active'
          AND (:cat_id IS NULL OR sc.category_id = :cat_id)
          AND ST_DWithin(
                pl.location,
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                :radius_meters
          )
        ORDER BY ST_Distance(
            pl.location,
            ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography
        ) ASC
        LIMIT :limit;
    """)

    res = await db.execute(sql, {
        "lat": latitude,
        "lng": longitude,
        "radius_meters": radius_meters,
        "cat_id": category_id,
        "limit": limit
    })
    ids = [row[0] for row in res.all()]
    if not ids:
        return []

    stmt = (
        select(Listing)
        .where(Listing.id.in_(ids))
        .options(
            selectinload(Listing.user),
            selectinload(Listing.provider),
            selectinload(Listing.provider_location).selectinload(ProviderLocation.area),
            selectinload(Listing.product_service).selectinload(ProductService.attributes_rel),
            selectinload(Listing.product_service).selectinload(ProductService.subcategory).selectinload(Subcategory.category),
            selectinload(Listing.price_type)
        )
    )
    loaded = {l.id: l for l in (await db.execute(stmt)).scalars().all()}
    # Preserve distance order
    return [build_listing_response(loaded[i]) for i in ids if i in loaded]

@router.get("/listings/{listing_id}", response_model=ListingResponse)
async def get_listing_detail(listing_id: int, db: AsyncSession = Depends(get_db)):
    """
    Retrieves complete details for a single listing.
    """
    stmt = (
        select(Listing)
        .where(Listing.id == listing_id, Listing.is_active == True)
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
    listing = result.scalars().first()
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")
    return build_listing_response(listing)

@router.get("/listings/{listing_id}/compare", response_model=ComparisonResponse)
async def get_listing_comparables(listing_id: int, db: AsyncSession = Depends(get_db)):
    """
    Cohort Comparison Engine (Phase 7): returns strictly matching comparable listings with market benchmarks.
    """
    cohort = await ComparisonService.get_comparable_cohort(listing_id, db)
    if not cohort:
        raise HTTPException(status_code=404, detail="Listing not found or cannot be compared")

    target_resp = build_listing_response(cohort.target_listing)
    comparables_resp = [build_listing_response(c) for c in cohort.comparables]

    prices = [c.price_amount for c in cohort.comparables if c.price_amount is not None]
    if not prices or cohort.target_listing.price_amount is None:
        return ComparisonResponse(
            target_listing=target_resp,
            reference_price=None,
            lowest_price=None,
            highest_price=None,
            price_difference_amount=None,
            price_difference_percent=None,
            price_label="Insufficient Data",
            sample_size=len(prices),
            cohort_criteria=cohort.cohort_criteria_applied,
            comparables=comparables_resp
        )

    ref_price = float(statistics.median(prices))
    lowest = float(min(prices))
    highest = float(max(prices))
    target_price = float(cohort.target_listing.price_amount)

    diff_amt = round(target_price - ref_price, 2)
    diff_pct = round(((target_price - ref_price) / ref_price) * 100.0, 1) if ref_price > 0 else 0.0

    if diff_pct <= -5.0:
        label = "Best Deal Nearby"
    elif -5.0 < diff_pct <= 5.0:
        label = "Fair Price"
    elif 5.0 < diff_pct <= 15.0:
        label = "Slightly Expensive"
    else:
        label = "Overpriced"

    return ComparisonResponse(
        target_listing=target_resp,
        reference_price=ref_price,
        lowest_price=lowest,
        highest_price=highest,
        price_difference_amount=diff_amt,
        price_difference_percent=diff_pct,
        price_label=label,
        sample_size=len(prices),
        cohort_criteria=cohort.cohort_criteria_applied,
        comparables=comparables_resp
    )

@router.get("/listings/{listing_id}/price-history", response_model=List[PriceObservationResponse])
async def get_listing_price_history(listing_id: int, db: AsyncSession = Depends(get_db)):
    """
    Returns full immutable historical price observations for this listing.
    """
    stmt = (
        select(PriceObservation)
        .where(PriceObservation.listing_id == listing_id)
        .options(selectinload(PriceObservation.price_type))
        .order_by(PriceObservation.effective_date.desc(), PriceObservation.id.desc())
    )
    result = await db.execute(stmt)
    observations = result.scalars().all()

    return [
        PriceObservationResponse(
            id=o.id,
            listing_id=o.listing_id,
            price_type_id=o.price_type_id,
            price_type_code=o.price_type.code,
            price_type_name=o.price_type.name,
            price_amount=float(o.price_amount) if o.price_amount is not None else None,
            currency=o.currency,
            source_type=o.source_type,
            submission_date=o.submission_date,
            effective_date=o.effective_date,
            expiry_date=o.expiry_date,
            verification_status=o.verification_status,
            evidence_reference=o.evidence_reference,
            notes=o.notes
        )
        for o in observations
    ]

# -----------------------------------------------------------------------------
# 2. USER-CONTROLLED LISTING MANAGEMENT (PHASE 8 & 9)
# -----------------------------------------------------------------------------

@router.get("/my-listings", response_model=List[ListingResponse])
async def get_my_listings(
    status_filter: Optional[str] = Query(None, pattern="^(active|pending|rejected|archived|suspended)$"),
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Returns all listings created by the authenticated user across all statuses (Phase 8.4).
    """
    stmt = (
        select(Listing)
        .where(Listing.user_id == current_user.id)
        .options(
            selectinload(Listing.user),
            selectinload(Listing.provider),
            selectinload(Listing.provider_location).selectinload(ProviderLocation.area),
            selectinload(Listing.product_service).selectinload(ProductService.attributes_rel),
            selectinload(Listing.product_service).selectinload(ProductService.subcategory).selectinload(Subcategory.category),
            selectinload(Listing.price_type)
        )
    )
    if status_filter:
        stmt = stmt.where(Listing.status == status_filter)
    else:
        # Don't show permanently archived unless requested
        stmt = stmt.where(Listing.status != "archived")

    stmt = stmt.order_by(Listing.id.desc())
    result = await db.execute(stmt)
    listings = result.scalars().all()

    return [build_listing_response(l) for l in listings]

@router.post("/listings", response_model=ListingResponse, status_code=status.HTTP_201_CREATED)
async def create_listing(
    payload: ListingCreateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Creates a new listing associated strictly with the authenticated user (Phase 8.1 & Phase 9).
    User ID is derived from the session/token; client-supplied user ID is ignored.
    """
    # 1. Validate Locality
    area = await db.get(Area, payload.locality_id)
    if not area:
        raise HTTPException(status_code=400, detail=f"Invalid locality_id: {payload.locality_id}")

    # 2. Validate Subcategory
    subcategory = await db.get(Subcategory, payload.subcategory_id)
    if not subcategory or subcategory.category_id != payload.category_id:
        raise HTTPException(status_code=400, detail="Invalid category or subcategory selection")

    # 3. Validate Price Type
    price_type = await db.get(PriceType, payload.price_type_id)
    if not price_type or price_type.category_id != payload.category_id:
        raise HTTPException(status_code=400, detail="Invalid price_type_id for selected category")

    # 4. Find or Create Provider
    prov_stmt = select(Provider).where(Provider.name.ilike(payload.brand_or_provider_name.strip()))
    prov_res = await db.execute(prov_stmt)
    provider = prov_res.scalars().first()
    if not provider:
        provider = Provider(
            name=payload.brand_or_provider_name.strip(),
            business_type="retail",
            is_verified=False,
            is_active=True
        )
        db.add(provider)
        await db.flush()

    # 5. Find or Create Provider Location in this Locality
    address = payload.address_line or f"{area.name}, Bengaluru"
    loc_stmt = select(ProviderLocation).where(
        ProviderLocation.provider_id == provider.id,
        ProviderLocation.area_id == area.id
    )
    loc_res = await db.execute(loc_stmt)
    location = loc_res.scalars().first()
    if not location:
        location = ProviderLocation(
            provider_id=provider.id,
            area_id=area.id,
            branch_name=f"{area.name} Branch",
            address_line=address,
            latitude=area.latitude,
            longitude=area.longitude,
            is_primary=True,
            is_active=True
        )
        db.add(location)
        await db.flush()

    # 6. Create Product / Service Item
    prod = ProductService(
        subcategory_id=subcategory.id,
        name=payload.title.strip(),
        brand_or_manufacturer=payload.brand_or_provider_name.strip(),
        description=payload.notes,
        is_active=True
    )
    db.add(prod)
    await db.flush()

    # 7. Store Category-Specific Attributes
    prod_attrs = ProductServiceAttributes(
        product_service_id=prod.id,
        attributes=payload.attributes,
        comparison_key_values={"title": payload.title.strip()}
    )
    db.add(prod_attrs)

    # 8. Set Status: Admins bypass moderation; regular user submissions require moderation
    initial_status = "active" if current_user.role == "admin" else "pending"
    initial_mod = "approved" if current_user.role == "admin" else "pending"

    # 9. Create Listing
    listing = Listing(
        user_id=current_user.id, # Derived strictly from authenticated context!
        provider_id=provider.id,
        provider_location_id=location.id,
        product_service_id=prod.id,
        custom_title=payload.title.strip(),
        price_amount=payload.price_amount,
        is_price_available=payload.is_price_available,
        price_type_id=price_type.id,
        currency=payload.currency,
        status=initial_status,
        moderation_status=initial_mod,
        source_type=payload.source_type,
        evidence_reference=payload.evidence_reference,
        is_available=True,
        is_active=True,
        notes=payload.notes
    )
    db.add(listing)
    await db.flush()

    # 10. Create Immutable Initial Price Observation
    if payload.price_amount is not None:
        obs = PriceObservation(
            listing_id=listing.id,
            price_type_id=price_type.id,
            price_amount=payload.price_amount,
            currency=payload.currency,
            source_type=payload.source_type,
            verification_status="verified" if current_user.role == "admin" else "pending",
            evidence_reference=payload.evidence_reference,
            notes=payload.notes
        )
        db.add(obs)

    # 11. Audit Log Entry
    audit = AuditLog(
        entity_type="listing",
        entity_id=listing.id,
        action="create",
        user_id=current_user.id,
        new_values={
            "title": listing.custom_title,
            "price": float(listing.price_amount) if listing.price_amount else None,
            "status": listing.status
        }
    )
    db.add(audit)
    await db.commit()

    return await get_listing_detail(listing.id, db)

@router.patch("/listings/{listing_id}", response_model=ListingResponse)
@router.put("/listings/{listing_id}", response_model=ListingResponse)
async def update_listing(
    listing_id: int,
    payload: ListingUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Edits a listing. Enforces backend ownership verification:
    Users can edit ONLY listings they created. Admins can edit any listing (Phase 8.2 & Phase 9).
    """
    stmt = (
        select(Listing)
        .where(Listing.id == listing_id)
        .options(selectinload(Listing.product_service).selectinload(ProductService.attributes_rel))
    )
    result = await db.execute(stmt)
    listing = result.scalars().first()
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")

    # Security check: User must own the listing or be an admin
    if listing.user_id != current_user.id and current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You do not have permission to modify this listing"
        )

    old_values = {
        "title": listing.custom_title,
        "price": float(listing.price_amount) if listing.price_amount else None,
        "status": listing.status
    }

    price_changed = False
    if payload.price_amount is not None and payload.price_amount != listing.price_amount:
        listing.price_amount = payload.price_amount
        price_changed = True

    if payload.custom_title is not None:
        listing.custom_title = payload.custom_title
    if payload.price_type_id is not None:
        listing.price_type_id = payload.price_type_id
    if payload.is_available is not None:
        listing.is_available = payload.is_available
    if payload.is_price_available is not None:
        listing.is_price_available = payload.is_price_available
    if payload.evidence_reference is not None:
        listing.evidence_reference = payload.evidence_reference
    if payload.notes is not None:
        listing.notes = payload.notes

    if payload.attributes and listing.product_service and listing.product_service.attributes_rel:
        listing.product_service.attributes_rel.attributes.update(payload.attributes)

    # Reapply pending moderation if non-admin made a substantial price change
    if price_changed and current_user.role != "admin":
        listing.status = "pending"
        listing.moderation_status = "pending"

    # Record historical price observation
    if price_changed and listing.price_amount is not None:
        obs = PriceObservation(
            listing_id=listing.id,
            price_type_id=listing.price_type_id,
            price_amount=listing.price_amount,
            currency=listing.currency,
            source_type="USER_SUBMITTED" if current_user.role != "admin" else "VERIFIED_REAL",
            verification_status="verified" if current_user.role == "admin" else "pending",
            evidence_reference=listing.evidence_reference,
            notes=f"Price revised by user on {datetime.now(timezone.utc).strftime('%Y-%m-%d %H:%M')}"
        )
        db.add(obs)

    # Record Audit Log
    audit = AuditLog(
        entity_type="listing",
        entity_id=listing.id,
        action="update",
        user_id=current_user.id,
        old_values=old_values,
        new_values={
            "title": listing.custom_title,
            "price": float(listing.price_amount) if listing.price_amount else None,
            "status": listing.status
        }
    )
    db.add(audit)

    await db.commit()
    return await get_listing_detail(listing.id, db)

@router.delete("/listings/{listing_id}", status_code=status.HTTP_200_OK)
async def delete_listing(
    listing_id: int,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Soft deletes (archives) a listing. Enforces backend ownership verification:
    Users can delete ONLY listings they created. Admins can delete any listing (Phase 8.3 & Phase 9).
    """
    listing = await db.get(Listing, listing_id)
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")

    if listing.user_id != current_user.id and current_user.role != "admin":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="You do not have permission to delete this listing"
        )

    # Soft delete / Archive
    listing.status = "archived"
    listing.is_active = False

    audit = AuditLog(
        entity_type="listing",
        entity_id=listing.id,
        action="archive_delete",
        user_id=current_user.id,
        old_values={"status": "active"},
        new_values={"status": "archived"}
    )
    db.add(audit)

    await db.commit()
    return {"success": True, "message": f"Listing #{listing_id} archived successfully"}

@router.post("/listings/{listing_id}/price-observations", response_model=PriceObservationResponse, status_code=status.HTTP_201_CREATED)
async def add_price_observation(
    listing_id: int,
    payload: PriceObservationCreateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Adds a new historical price observation to a listing (Phase 6).
    """
    listing = await db.get(Listing, listing_id)
    if not listing or not listing.is_active:
        raise HTTPException(status_code=404, detail="Listing not found")

    price_type = await db.get(PriceType, payload.price_type_id)
    if not price_type:
        raise HTTPException(status_code=400, detail="Invalid price_type_id")

    obs = PriceObservation(
        listing_id=listing.id,
        price_type_id=payload.price_type_id,
        price_amount=payload.price_amount,
        currency=payload.currency,
        source_type=payload.source_type,
        verification_status="verified" if current_user.role == "admin" else "pending",
        evidence_reference=payload.evidence_reference,
        notes=payload.notes
    )
    db.add(obs)

    # If owner updated price, update listing current price as well
    if listing.user_id == current_user.id or current_user.role == "admin":
        listing.price_amount = payload.price_amount
        listing.price_type_id = payload.price_type_id

    await db.commit()
    await db.refresh(obs)

    return PriceObservationResponse(
        id=obs.id,
        listing_id=obs.listing_id,
        price_type_id=obs.price_type_id,
        price_type_code=price_type.code,
        price_type_name=price_type.name,
        price_amount=float(obs.price_amount),
        currency=obs.currency,
        source_type=obs.source_type,
        submission_date=obs.submission_date,
        effective_date=obs.effective_date,
        expiry_date=obs.expiry_date,
        verification_status=obs.verification_status,
        evidence_reference=obs.evidence_reference,
        notes=obs.notes
    )

@router.post("/listings/{listing_id}/reports", response_model=ListingReportResponse, status_code=status.HTTP_201_CREATED)
async def report_listing(
    listing_id: int,
    payload: ListingReportCreateRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db)
):
    """
    Allows authenticated users to report inappropriate or inaccurate listings (Phase 8.6).
    """
    listing = await db.get(Listing, listing_id)
    if not listing:
        raise HTTPException(status_code=404, detail="Listing not found")

    report = ListingReport(
        listing_id=listing.id,
        reporter_user_id=current_user.id,
        reason_category=payload.reason_category,
        details=payload.details,
        review_status="pending_review"
    )
    db.add(report)
    await db.commit()
    await db.refresh(report)

    return ListingReportResponse(
        id=report.id,
        listing_id=report.listing_id,
        listing_title=listing.custom_title or "Listing",
        reporter_user_id=current_user.id,
        reporter_name=current_user.full_name,
        reason_category=report.reason_category,
        details=report.details,
        review_status=report.review_status,
        admin_notes=report.admin_notes,
        created_at=report.created_at,
        resolved_at=report.resolved_at
    )
