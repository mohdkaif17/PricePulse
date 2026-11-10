from datetime import datetime, timezone
from decimal import Decimal
from typing import Optional, List, Dict, Any
from sqlalchemy import (
    BigInteger, String, Text, Boolean, Numeric, DateTime, ForeignKey, CheckConstraint, func
)
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import declarative_base, relationship, Mapped, mapped_column
try:
    from geoalchemy2 import Geography
except ImportError:
    Geography = None

from app.db.session import Base

class City(Base):
    __tablename__ = "cities"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    district: Mapped[str] = mapped_column(String(100), default="Bengaluru Urban", nullable=False)
    state: Mapped[str] = mapped_column(String(100), default="Karnataka", nullable=False)
    country: Mapped[str] = mapped_column(String(100), default="India", nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    areas: Mapped[List["Area"]] = relationship("Area", back_populates="city")


class Area(Base):
    __tablename__ = "areas"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    city_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("cities.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    district: Mapped[str] = mapped_column(String(100), default="Bengaluru Urban", nullable=False)
    pincode: Mapped[Optional[str]] = mapped_column(String(10), nullable=True)
    latitude: Mapped[Decimal] = mapped_column(Numeric(10, 6), nullable=False)
    longitude: Mapped[Decimal] = mapped_column(Numeric(10, 6), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    city: Mapped["City"] = relationship("City", back_populates="areas")
    locations: Mapped[List["ProviderLocation"]] = relationship("ProviderLocation", back_populates="area")


class Category(Base):
    __tablename__ = "categories"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    slug: Mapped[str] = mapped_column(String(120), unique=True, nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    icon_key: Mapped[Optional[str]] = mapped_column(String(50), nullable=True)
    display_order: Mapped[int] = mapped_column(default=0, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    subcategories: Mapped[List["Subcategory"]] = relationship("Subcategory", back_populates="category")
    price_types: Mapped[List["PriceType"]] = relationship("PriceType", back_populates="category")


class Subcategory(Base):
    __tablename__ = "subcategories"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    category_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("categories.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    slug: Mapped[str] = mapped_column(String(120), unique=True, nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    item_type: Mapped[str] = mapped_column(String(30), nullable=False)
    attribute_schema: Mapped[Optional[Dict[str, Any]]] = mapped_column(JSONB, nullable=True)
    display_order: Mapped[int] = mapped_column(default=0, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    category: Mapped["Category"] = relationship("Category", back_populates="subcategories")
    products: Mapped[List["ProductService"]] = relationship("ProductService", back_populates="subcategory")


class PriceType(Base):
    __tablename__ = "price_types"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    category_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("categories.id"), nullable=False)
    code: Mapped[str] = mapped_column(String(60), unique=True, nullable=False)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    is_recurring: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    category: Mapped["Category"] = relationship("Category", back_populates="price_types")


class User(Base):
    __tablename__ = "users"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    username: Mapped[str] = mapped_column(String(100), unique=True, nullable=False)
    email: Mapped[str] = mapped_column(String(150), unique=True, nullable=False)
    full_name: Mapped[str] = mapped_column(String(150), nullable=False)
    password_hash: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    role: Mapped[str] = mapped_column(String(20), default="user", nullable=False)
    phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    listings: Mapped[List["Listing"]] = relationship("Listing", back_populates="user")
    reports: Mapped[List["ListingReport"]] = relationship("ListingReport", back_populates="reporter", foreign_keys="ListingReport.reporter_user_id")


class Provider(Base):
    __tablename__ = "providers"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    legal_name: Mapped[Optional[str]] = mapped_column(String(250), nullable=True)
    business_type: Mapped[str] = mapped_column(String(50), default="retail", nullable=False)
    phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    email: Mapped[Optional[str]] = mapped_column(String(150), nullable=True)
    website: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    is_verified: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    locations: Mapped[List["ProviderLocation"]] = relationship("ProviderLocation", back_populates="provider")
    listings: Mapped[List["Listing"]] = relationship("Listing", back_populates="provider")


class ProviderLocation(Base):
    __tablename__ = "provider_locations"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    provider_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("providers.id"), nullable=False)
    area_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("areas.id"), nullable=False)
    branch_name: Mapped[Optional[str]] = mapped_column(String(150), nullable=True)
    address_line: Mapped[str] = mapped_column(Text, nullable=False)
    latitude: Mapped[Decimal] = mapped_column(Numeric(10, 6), nullable=False)
    longitude: Mapped[Decimal] = mapped_column(Numeric(10, 6), nullable=False)
    contact_phone: Mapped[Optional[str]] = mapped_column(String(30), nullable=True)
    is_primary: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    provider: Mapped["Provider"] = relationship("Provider", back_populates="locations")
    area: Mapped["Area"] = relationship("Area", back_populates="locations")
    listings: Mapped[List["Listing"]] = relationship("Listing", back_populates="provider_location")


class ProductService(Base):
    __tablename__ = "products_and_services"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    subcategory_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("subcategories.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(255), nullable=False)
    identifier_code: Mapped[Optional[str]] = mapped_column(String(100), nullable=True)
    brand_or_manufacturer: Mapped[Optional[str]] = mapped_column(String(150), nullable=True)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)

    subcategory: Mapped["Subcategory"] = relationship("Subcategory", back_populates="products")
    attributes_rel: Mapped[Optional["ProductServiceAttributes"]] = relationship("ProductServiceAttributes", back_populates="product", uselist=False)
    listings: Mapped[List["Listing"]] = relationship("Listing", back_populates="product_service")


class ProductServiceAttributes(Base):
    __tablename__ = "product_service_attributes"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    product_service_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("products_and_services.id"), unique=True, nullable=False)
    attributes: Mapped[Dict[str, Any]] = mapped_column(JSONB, default=dict, nullable=False)
    comparison_key_values: Mapped[Dict[str, Any]] = mapped_column(JSONB, default=dict, nullable=False)

    product: Mapped["ProductService"] = relationship("ProductService", back_populates="attributes_rel")


class Listing(Base):
    __tablename__ = "listings"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    user_id: Mapped[Optional[int]] = mapped_column(BigInteger, ForeignKey("users.id"), nullable=True)
    provider_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("providers.id"), nullable=False)
    provider_location_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("provider_locations.id"), nullable=False)
    product_service_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("products_and_services.id"), nullable=False)
    custom_title: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    price_amount: Mapped[Optional[Decimal]] = mapped_column(Numeric(14, 2), nullable=True)
    is_price_available: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    price_type_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("price_types.id"), nullable=False)
    currency: Mapped[str] = mapped_column(String(3), default="INR", nullable=False)
    status: Mapped[str] = mapped_column(String(30), default="active", nullable=False) # active, pending, rejected, archived, suspended
    moderation_status: Mapped[str] = mapped_column(String(20), default="approved", nullable=False)
    rejection_reason: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    source_type: Mapped[str] = mapped_column(String(40), default="PUBLICLY_SOURCED", nullable=False)
    evidence_reference: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    is_available: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True, nullable=False)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    user: Mapped[Optional["User"]] = relationship("User", back_populates="listings")
    provider: Mapped["Provider"] = relationship("Provider", back_populates="listings")
    provider_location: Mapped["ProviderLocation"] = relationship("ProviderLocation", back_populates="listings")
    product_service: Mapped["ProductService"] = relationship("ProductService", back_populates="listings")
    price_type: Mapped["PriceType"] = relationship("PriceType")
    price_observations: Mapped[List["PriceObservation"]] = relationship("PriceObservation", back_populates="listing", cascade="all, delete-orphan")
    verification_records: Mapped[List["VerificationRecord"]] = relationship("VerificationRecord", back_populates="listing", cascade="all, delete-orphan")
    reports: Mapped[List["ListingReport"]] = relationship("ListingReport", back_populates="listing", cascade="all, delete-orphan")


class PriceObservation(Base):
    __tablename__ = "price_observations"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    listing_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("listings.id"), nullable=False)
    price_type_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("price_types.id"), nullable=False)
    price_amount: Mapped[Optional[Decimal]] = mapped_column(Numeric(14, 2), nullable=True)
    currency: Mapped[str] = mapped_column(String(3), default="INR", nullable=False)
    source_type: Mapped[str] = mapped_column(String(40), default="PUBLICLY_SOURCED", nullable=False)
    submission_date: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    effective_date: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    expiry_date: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    verification_status: Mapped[str] = mapped_column(String(20), default="verified", nullable=False)
    evidence_reference: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    listing: Mapped["Listing"] = relationship("Listing", back_populates="price_observations")


class VerificationRecord(Base):
    __tablename__ = "verification_records"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    listing_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("listings.id"), nullable=False)
    verifier_user_id: Mapped[Optional[int]] = mapped_column(BigInteger, ForeignKey("users.id"), nullable=True)
    verification_type: Mapped[str] = mapped_column(String(50), default="FIELD_VISIT", nullable=False)
    status: Mapped[str] = mapped_column(String(20), default="verified", nullable=False)
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    evidence_url: Mapped[Optional[str]] = mapped_column(String(500), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())

    listing: Mapped["Listing"] = relationship("Listing", back_populates="verification_records")


class ListingReport(Base):
    __tablename__ = "listing_reports"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    listing_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("listings.id"), nullable=False)
    reporter_user_id: Mapped[int] = mapped_column(BigInteger, ForeignKey("users.id"), nullable=False)
    reason_category: Mapped[str] = mapped_column(String(50), nullable=False)
    details: Mapped[str] = mapped_column(Text, nullable=False)
    review_status: Mapped[str] = mapped_column(String(30), default="pending_review", nullable=False) # pending_review, reviewed, action_taken, dismissed
    admin_notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    resolved_by: Mapped[Optional[int]] = mapped_column(BigInteger, ForeignKey("users.id"), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
    resolved_at: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)

    listing: Mapped["Listing"] = relationship("Listing", back_populates="reports")
    reporter: Mapped["User"] = relationship("User", foreign_keys=[reporter_user_id], back_populates="reports")


class AuditLog(Base):
    __tablename__ = "audit_logs"

    id: Mapped[int] = mapped_column(BigInteger, primary_key=True, index=True)
    entity_type: Mapped[str] = mapped_column(String(50), nullable=False) # listing, price_observation, etc.
    entity_id: Mapped[int] = mapped_column(BigInteger, nullable=False)
    action: Mapped[str] = mapped_column(String(30), nullable=False) # create, update, delete, moderate_approve, etc.
    user_id: Mapped[Optional[int]] = mapped_column(BigInteger, ForeignKey("users.id"), nullable=True)
    old_values: Mapped[Optional[Dict[str, Any]]] = mapped_column(JSONB, nullable=True)
    new_values: Mapped[Optional[Dict[str, Any]]] = mapped_column(JSONB, nullable=True)
    ip_address: Mapped[Optional[str]] = mapped_column(String(45), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), server_default=func.now())
