from datetime import datetime
from decimal import Decimal
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field

class ListingCreateRequest(BaseModel):
    locality_id: int = Field(..., description="ID of the Bengaluru area/locality")
    category_id: int = Field(..., description="ID of the main category")
    subcategory_id: int = Field(..., description="ID of the approved subcategory")
    title: str = Field(..., min_length=3, max_length=255, description="Product, service, or offering title")
    brand_or_provider_name: str = Field(..., min_length=2, max_length=150, description="Name of the seller, hospital, school, gym, or brand")
    price_amount: Optional[Decimal] = Field(default=None, description="Listed offering price (null if unlisted)")
    price_type_id: int = Field(..., description="Associated price type ID")
    currency: str = Field(default="INR", max_length=3)
    source_type: str = Field(default="USER_SUBMITTED", description="Source classification: VERIFIED_REAL, USER_SUBMITTED, PROVIDER_SUBMITTED, DEMO_DATA")
    is_price_available: bool = Field(default=True)
    evidence_reference: Optional[str] = None
    address_line: Optional[str] = Field(default=None, description="Optional specific street or branch address")
    attributes: Dict[str, Any] = Field(default_factory=dict, description="Category-specific attributes")
    notes: Optional[str] = Field(default=None, description="Additional notes, discounts, terms, or conditions")

class ListingUpdateRequest(BaseModel):
    custom_title: Optional[str] = None
    price_amount: Optional[Decimal] = Field(default=None, ge=0)
    price_type_id: Optional[int] = None
    is_available: Optional[bool] = None
    is_price_available: Optional[bool] = None
    evidence_reference: Optional[str] = None
    notes: Optional[str] = None
    attributes: Optional[Dict[str, Any]] = None

class ListingModerationRequest(BaseModel):
    action: str = Field(..., pattern="^(approve|reject|suspend|restore|archive)$")
    rejection_reason: Optional[str] = None
    admin_notes: Optional[str] = None

class PriceObservationCreateRequest(BaseModel):
    price_amount: Decimal = Field(..., gt=0)
    price_type_id: int
    currency: str = Field(default="INR", max_length=3)
    source_type: str = Field(default="USER_SUBMITTED")
    evidence_reference: Optional[str] = None
    notes: Optional[str] = None

class PriceObservationResponse(BaseModel):
    id: int
    listing_id: int
    price_type_id: int
    price_type_code: str
    price_type_name: str
    price_amount: Optional[float] = None
    currency: str
    source_type: str
    submission_date: datetime
    effective_date: datetime
    expiry_date: Optional[datetime] = None
    verification_status: str
    evidence_reference: Optional[str] = None
    notes: Optional[str] = None

class ListingResponse(BaseModel):
    id: int
    user_id: Optional[int] = None
    user_name: Optional[str] = None
    custom_title: str
    price_amount: Optional[float] = None
    is_price_available: bool = True
    currency: str
    status: str # active, pending, rejected, archived, suspended
    moderation_status: str
    rejection_reason: Optional[str] = None
    source_type: str
    price_type_code: str
    price_type_name: str
    category_id: int
    category_name: str
    category_slug: str
    subcategory_id: int
    subcategory_name: str
    subcategory_slug: str
    provider_id: int
    provider_name: str
    provider_location_id: int
    branch_name: Optional[str] = None
    locality_id: int
    area_name: str
    address_line: str
    latitude: float
    longitude: float
    is_available: bool
    evidence_reference: Optional[str] = None
    attributes: Dict[str, Any] = Field(default_factory=dict)
    notes: Optional[str] = None
    created_at: datetime
    updated_at: datetime

class ComparisonResponse(BaseModel):
    target_listing: ListingResponse
    reference_price: Optional[float] = None
    lowest_price: Optional[float] = None
    highest_price: Optional[float] = None
    price_difference_amount: Optional[float] = None
    price_difference_percent: Optional[float] = None
    price_label: str
    sample_size: int
    cohort_criteria: Dict[str, Any] = Field(default_factory=dict)
    comparables: List[ListingResponse] = Field(default_factory=list)

class AuditLogResponse(BaseModel):
    id: int
    entity_type: str
    entity_id: int
    action: str
    user_id: Optional[int] = None
    old_values: Optional[Dict[str, Any]] = None
    new_values: Optional[Dict[str, Any]] = None
    created_at: datetime
