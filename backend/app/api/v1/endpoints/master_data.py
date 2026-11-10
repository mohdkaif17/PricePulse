from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select, func
from sqlalchemy.orm import selectinload

from app.db.session import get_db
from app.models.entities import City, Area, Category, Subcategory, PriceType, Provider, ProviderLocation, ProductService, Listing, PriceObservation
from app.schemas.master import (
    LocalityResponse, CategoryResponse, SubcategoryResponse, PriceTypeResponse,
    ProviderResponse, ProviderBranchResponse, DatasetSummaryResponse
)

router = APIRouter(tags=["Master Taxonomy & Bengaluru Localities"])

@router.get("/meta/summary", response_model=DatasetSummaryResponse)
async def get_dataset_summary(db: AsyncSession = Depends(get_db)):
    """
    Returns platform-wide summary metrics for Bengaluru dataset coverage.
    """
    cities_count = (await db.execute(select(func.count(City.id)))).scalar() or 0
    areas_count = (await db.execute(select(func.count(Area.id)))).scalar() or 0
    categories_count = (await db.execute(select(func.count(Category.id)))).scalar() or 0
    subcategories_count = (await db.execute(select(func.count(Subcategory.id)))).scalar() or 0
    providers_count = (await db.execute(select(func.count(Provider.id)))).scalar() or 0
    products_count = (await db.execute(select(func.count(ProductService.id)))).scalar() or 0
    listings_count = (await db.execute(select(func.count(Listing.id)))).scalar() or 0
    price_obs_count = (await db.execute(select(func.count(PriceObservation.id)))).scalar() or 0

    area_names_res = await db.execute(select(Area.name).order_by(Area.name))
    active_localities = [r[0] for r in area_names_res.all()]

    return DatasetSummaryResponse(
        total_cities=cities_count,
        total_localities=areas_count,
        total_categories=categories_count,
        total_subcategories=subcategories_count,
        total_providers=providers_count,
        total_products_services=products_count,
        total_listings=listings_count,
        total_price_observations=price_obs_count,
        active_localities=active_localities
    )

@router.get("/areas", response_model=List[LocalityResponse])
@router.get("/localities", response_model=List[LocalityResponse])
async def get_localities(db: AsyncSession = Depends(get_db)):
    """
    Returns all 15 supported Bengaluru localities with geo coordinates and PIN codes (Phase 11).
    """
    stmt = select(Area).where(Area.is_active == True).order_by(Area.name)
    result = await db.execute(stmt)
    areas = result.scalars().all()

    return [
        LocalityResponse(
            id=a.id,
            name=a.name,
            district=a.district,
            pincode=a.pincode,
            latitude=float(a.latitude),
            longitude=float(a.longitude),
            is_active=a.is_active
        )
        for a in areas
    ]

@router.get("/categories", response_model=List[CategoryResponse])
async def get_categories(db: AsyncSession = Depends(get_db)):
    """
    Returns all 10 approved main categories with their respective subcategories (Phase 11).
    """
    stmt = select(Category).where(Category.is_active == True).options(
        selectinload(Category.subcategories)
    ).order_by(Category.display_order)
    result = await db.execute(stmt)
    categories = result.scalars().all()

    response = []
    for cat in categories:
        sub_list = [
            SubcategoryResponse(
                id=sub.id,
                category_id=sub.category_id,
                name=sub.name,
                slug=sub.slug,
                description=sub.description,
                item_type=sub.item_type,
                display_order=sub.display_order,
                is_active=sub.is_active
            )
            for sub in sorted(cat.subcategories, key=lambda s: s.display_order)
            if sub.is_active
        ]
        response.append(
            CategoryResponse(
                id=cat.id,
                name=cat.name,
                slug=cat.slug,
                description=cat.description,
                icon_key=cat.icon_key,
                display_order=cat.display_order,
                is_active=cat.is_active,
                subcategories=sub_list
            )
        )
    return response

@router.get("/categories/{category_id}/subcategories", response_model=List[SubcategoryResponse])
async def get_subcategories_by_category(category_id: int, db: AsyncSession = Depends(get_db)):
    """
    Returns subcategories for a given main category ID (Phase 11).
    """
    stmt = select(Subcategory).where(
        Subcategory.category_id == category_id,
        Subcategory.is_active == True
    ).order_by(Subcategory.display_order)
    result = await db.execute(stmt)
    subcategories = result.scalars().all()

    return [
        SubcategoryResponse(
            id=sub.id,
            category_id=sub.category_id,
            name=sub.name,
            slug=sub.slug,
            description=sub.description,
            item_type=sub.item_type,
            display_order=sub.display_order,
            is_active=sub.is_active
        )
        for sub in subcategories
    ]

@router.get("/providers", response_model=List[ProviderResponse])
async def get_providers(
    area_id: Optional[int] = Query(None, alias="areaId", description="Filter providers operating in an area"),
    business_type: Optional[str] = Query(None),
    limit: int = Query(50, ge=1, le=100),
    offset: int = Query(0, ge=0),
    db: AsyncSession = Depends(get_db)
):
    """
    Returns providers with their branches, optionally filtered by locality/area (Phase 11).
    """
    stmt = (
        select(Provider)
        .where(Provider.is_active == True)
        .options(
            selectinload(Provider.locations).selectinload(ProviderLocation.area)
        )
    )

    if area_id:
        stmt = stmt.join(Provider.locations).where(ProviderLocation.area_id == area_id)
    if business_type:
        stmt = stmt.where(Provider.business_type == business_type)

    stmt = stmt.order_by(Provider.name).limit(limit).offset(offset)
    result = await db.execute(stmt)
    providers = result.scalars().all()

    response = []
    for p in providers:
        branches = [
            ProviderBranchResponse(
                id=loc.id,
                area_id=loc.area_id,
                area_name=loc.area.name if loc.area else "Bengaluru",
                branch_name=loc.branch_name,
                address_line=loc.address_line,
                latitude=float(loc.latitude),
                longitude=float(loc.longitude),
                contact_phone=loc.contact_phone
            )
            for loc in p.locations
            if loc.is_active and (not area_id or loc.area_id == area_id)
        ]
        response.append(
            ProviderResponse(
                id=p.id,
                name=p.name,
                legal_name=p.legal_name,
                business_type=p.business_type,
                phone=p.phone,
                email=p.email,
                website=p.website,
                is_verified=p.is_verified,
                is_active=p.is_active,
                branches=branches
            )
        )
    return response

@router.get("/price-types", response_model=List[PriceTypeResponse])
async def get_price_types(
    category_id: Optional[int] = None,
    db: AsyncSession = Depends(get_db)
):
    """
    Returns supported price types, optionally filtered by category.
    """
    stmt = select(PriceType).where(PriceType.is_active == True)
    if category_id:
        stmt = stmt.where(PriceType.category_id == category_id)
    stmt = stmt.order_by(PriceType.category_id, PriceType.id)

    result = await db.execute(stmt)
    price_types = result.scalars().all()

    return [
        PriceTypeResponse(
            id=pt.id,
            category_id=pt.category_id,
            code=pt.code,
            name=pt.name,
            description=pt.description,
            is_recurring=pt.is_recurring
        )
        for pt in price_types
    ]
