from datetime import datetime
from decimal import Decimal
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field

class LocalityResponse(BaseModel):
    id: int
    name: str
    city_name: str = "Bengaluru"
    district: str = "Bengaluru Urban"
    pincode: Optional[str] = None
    latitude: float
    longitude: float
    is_active: bool

class SubcategoryResponse(BaseModel):
    id: int
    category_id: int
    name: str
    slug: str
    description: Optional[str] = None
    item_type: str
    display_order: int
    is_active: bool

class CategoryResponse(BaseModel):
    id: int
    name: str
    slug: str
    description: Optional[str] = None
    icon_key: Optional[str] = None
    display_order: int
    is_active: bool
    subcategories: List[SubcategoryResponse] = Field(default_factory=list)

class PriceTypeResponse(BaseModel):
    id: int
    category_id: int
    code: str
    name: str
    description: Optional[str] = None
    is_recurring: bool

class ProviderBranchResponse(BaseModel):
    id: int
    area_id: int
    area_name: str
    branch_name: Optional[str] = None
    address_line: str
    latitude: float
    longitude: float
    contact_phone: Optional[str] = None

class ProviderResponse(BaseModel):
    id: int
    name: str
    legal_name: Optional[str] = None
    business_type: str
    phone: Optional[str] = None
    email: Optional[str] = None
    website: Optional[str] = None
    is_verified: bool
    is_active: bool
    branches: List[ProviderBranchResponse] = Field(default_factory=list)

class DatasetSummaryResponse(BaseModel):
    total_cities: int
    total_localities: int
    total_categories: int
    total_subcategories: int
    total_providers: int
    total_products_services: int
    total_listings: int
    total_price_observations: int
    active_localities: List[str]
