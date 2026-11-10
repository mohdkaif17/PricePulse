from datetime import datetime, timezone
from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from sqlalchemy.orm import selectinload

from app.db.session import get_db
from app.models.entities import Listing, ListingReport, AuditLog, User, ProviderLocation, ProductService, Subcategory, Category
from app.schemas.listings import ListingResponse, ListingModerationRequest, AuditLogResponse
from app.schemas.reports import ListingReportResponse, ListingReportUpdateRequest
from app.api.deps import get_current_admin
from app.api.v1.endpoints.listings import build_listing_response

router = APIRouter(prefix="/admin", tags=["Administrator Moderation & Governance"])

@router.get("/listings", response_model=List[ListingResponse])
async def admin_get_all_listings(
    status_filter: Optional[str] = Query(None, description="active, pending, rejected, archived, suspended"),
    search: Optional[str] = Query(None),
    limit: int = Query(50, ge=1, le=200),
    offset: int = Query(0, ge=0),
    current_admin: User = Depends(get_current_admin),
    db: AsyncSession = Depends(get_db)
):
    """
    Administrator overview of all listings across the platform regardless of status (Phase 8.5).
    """
    stmt = (
        select(Listing)
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

    if search and search.strip():
        term = f"%{search.strip()}%"
        stmt = stmt.where(Listing.custom_title.ilike(term))

    stmt = stmt.order_by(Listing.id.desc()).limit(limit).offset(offset)
    result = await db.execute(stmt)
    listings = result.scalars().all()

    return [build_listing_response(l) for l in listings]

@router.post("/listings/{listing_id}/moderate", response_model=ListingResponse)
async def admin_moderate_listing(
    listing_id: int,
    payload: ListingModerationRequest,
    current_admin: User = Depends(get_current_admin),
    db: AsyncSession = Depends(get_db)
):
    """
    Administrator moderation: approve, reject (with reason), suspend, restore, or archive (Phase 8.5).
    """
    stmt = (
        select(Listing)
        .where(Listing.id == listing_id)
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

    old_status = listing.status

    if payload.action == "approve":
        listing.status = "active"
        listing.moderation_status = "approved"
        listing.is_active = True
        listing.rejection_reason = None
    elif payload.action == "reject":
        listing.status = "rejected"
        listing.moderation_status = "rejected"
        listing.is_active = False
        listing.rejection_reason = payload.rejection_reason or "Does not meet listing guidelines"
    elif payload.action == "suspend":
        listing.status = "suspended"
        listing.moderation_status = "rejected"
        listing.is_active = False
        listing.rejection_reason = payload.rejection_reason or "Temporarily suspended by administrator"
    elif payload.action == "restore":
        listing.status = "active"
        listing.moderation_status = "approved"
        listing.is_active = True
        listing.rejection_reason = None
    elif payload.action == "archive":
        listing.status = "archived"
        listing.is_active = False

    if payload.admin_notes:
        listing.notes = f"{listing.notes or ''} [Admin: {payload.admin_notes}]".strip()

    # Record Audit Log
    audit = AuditLog(
        entity_type="listing",
        entity_id=listing.id,
        action=f"moderate_{payload.action}",
        user_id=current_admin.id,
        old_values={"status": old_status},
        new_values={"status": listing.status, "reason": listing.rejection_reason}
    )
    db.add(audit)

    await db.commit()
    return build_listing_response(listing)

@router.get("/reports", response_model=List[ListingReportResponse])
async def admin_get_reports(
    review_status: Optional[str] = Query(None, pattern="^(pending_review|reviewed|action_taken|dismissed)$"),
    current_admin: User = Depends(get_current_admin),
    db: AsyncSession = Depends(get_db)
):
    """
    Administrator queue for reported listings (Phase 8.6).
    """
    stmt = (
        select(ListingReport)
        .options(selectinload(ListingReport.listing), selectinload(ListingReport.reporter))
        .order_by(ListingReport.id.desc())
    )
    if review_status:
        stmt = stmt.where(ListingReport.review_status == review_status)

    result = await db.execute(stmt)
    reports = result.scalars().all()

    return [
        ListingReportResponse(
            id=r.id,
            listing_id=r.listing_id,
            listing_title=r.listing.custom_title if r.listing else f"Listing #{r.listing_id}",
            reporter_user_id=r.reporter_user_id,
            reporter_name=r.reporter.full_name if r.reporter else "User",
            reason_category=r.reason_category,
            details=r.details,
            review_status=r.review_status,
            admin_notes=r.admin_notes,
            created_at=r.created_at,
            resolved_at=r.resolved_at
        )
        for r in reports
    ]

@router.patch("/reports/{report_id}", response_model=ListingReportResponse)
async def admin_update_report(
    report_id: int,
    payload: ListingReportUpdateRequest,
    current_admin: User = Depends(get_current_admin),
    db: AsyncSession = Depends(get_db)
):
    """
    Resolves or dismisses a reported listing issue.
    """
    report = await db.get(ListingReport, report_id)
    if not report:
        raise HTTPException(status_code=404, detail="Report not found")

    report.review_status = payload.review_status
    report.admin_notes = payload.admin_notes
    report.resolved_by = current_admin.id
    report.resolved_at = datetime.now(timezone.utc)

    await db.commit()
    await db.refresh(report)

    listing_title = "Listing"
    if report.listing_id:
        l = await db.get(Listing, report.listing_id)
        if l and l.custom_title:
            listing_title = l.custom_title

    return ListingReportResponse(
        id=report.id,
        listing_id=report.listing_id,
        listing_title=listing_title,
        reporter_user_id=report.reporter_user_id,
        reporter_name="User",
        reason_category=report.reason_category,
        details=report.details,
        review_status=report.review_status,
        admin_notes=report.admin_notes,
        created_at=report.created_at,
        resolved_at=report.resolved_at
    )

@router.get("/audit-logs", response_model=List[AuditLogResponse])
async def admin_get_audit_logs(
    entity_type: Optional[str] = None,
    entity_id: Optional[int] = None,
    limit: int = Query(50, ge=1, le=200),
    current_admin: User = Depends(get_current_admin),
    db: AsyncSession = Depends(get_db)
):
    """
    Immutable change audit trail for listings and system entities (Phase 8 & 9).
    """
    stmt = select(AuditLog).order_by(AuditLog.id.desc()).limit(limit)
    if entity_type:
        stmt = stmt.where(AuditLog.entity_type == entity_type)
    if entity_id:
        stmt = stmt.where(AuditLog.entity_id == entity_id)

    result = await db.execute(stmt)
    logs = result.scalars().all()

    return [
        AuditLogResponse(
            id=log.id,
            entity_type=log.entity_type,
            entity_id=log.entity_id,
            action=log.action,
            user_id=log.user_id,
            old_values=log.old_values,
            new_values=log.new_values,
            created_at=log.created_at
        )
        for log in logs
    ]

