from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field

class ListingReportCreateRequest(BaseModel):
    reason_category: str = Field(..., pattern="^(incorrect_info|duplicate_listing|misleading_price|outdated_info|fake_provider|inappropriate_content|other)$")
    details: str = Field(..., min_length=10, max_length=1000)

class ListingReportUpdateRequest(BaseModel):
    review_status: str = Field(..., pattern="^(pending_review|reviewed|action_taken|dismissed)$")
    admin_notes: Optional[str] = None

class ListingReportResponse(BaseModel):
    id: int
    listing_id: int
    listing_title: str
    reporter_user_id: int
    reporter_name: str
    reason_category: str
    details: str
    review_status: str
    admin_notes: Optional[str] = None
    created_at: datetime
    resolved_at: Optional[datetime] = None

