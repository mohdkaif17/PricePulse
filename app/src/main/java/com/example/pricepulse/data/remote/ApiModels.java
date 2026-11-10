package com.example.pricepulse.data.remote;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiModels {

    // --- Health Check ---
    public static class HealthResponse {
        @SerializedName("status")
        public String status;
        @SerializedName("database")
        public String database;
        @SerializedName("version")
        public String version;
        @SerializedName("timestamp")
        public long timestamp;
    }

    // --- Authentication ---
    public static class LoginRequest {
        @SerializedName("email_or_username")
        public String emailOrUsername;
        @SerializedName("password")
        public String password;

        public LoginRequest(String emailOrUsername, String password) {
            this.emailOrUsername = emailOrUsername;
            this.password = password;
        }
    }

    public static class RegisterRequest {
        @SerializedName("username")
        public String username;
        @SerializedName("email")
        public String email;
        @SerializedName("password")
        public String password;
        @SerializedName("full_name")
        public String fullName;
        @SerializedName("phone")
        public String phone;

        public RegisterRequest(String username, String email, String password, String fullName, String phone) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.fullName = fullName;
            this.phone = phone;
        }
    }

    public static class TokenResponse {
        @SerializedName("access_token")
        public String accessToken;
        @SerializedName("token_type")
        public String tokenType = "bearer";
        @SerializedName("user_id")
        public long userId;
        @SerializedName("username")
        public String username;
        @SerializedName("email")
        public String email;
        @SerializedName("role")
        public String role;
        @SerializedName("full_name")
        public String fullName;
        @SerializedName("phone")
        public String phone;
        @SerializedName("created_at")
        public String createdAt;
    }

    public static class UserModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("username")
        public String username;
        @SerializedName("email")
        public String email;
        @SerializedName("full_name")
        public String fullName;
        @SerializedName("role")
        public String role;
        @SerializedName("phone")
        public String phone;
        @SerializedName("is_active")
        public boolean isActive;
        @SerializedName("created_at")
        public String createdAt;
        @SerializedName("email_verified")
        public boolean emailVerified;
    }

    public static class UpdateProfileRequest {
        @SerializedName("full_name")
        public String fullName;
        @SerializedName("phone")
        public String phone;

        public UpdateProfileRequest(String fullName, String phone) {
            this.fullName = fullName;
            this.phone = phone;
        }
    }

    public static class ChangePasswordRequest {
        @SerializedName("current_password")
        public String currentPassword;
        @SerializedName("new_password")
        public String newPassword;

        public ChangePasswordRequest(String currentPassword, String newPassword) {
            this.currentPassword = currentPassword;
            this.newPassword = newPassword;
        }
    }

    // --- Dataset Summary ---
    public static class SummaryResponse {
        @SerializedName("total_cities")
        public int totalCities;
        @SerializedName("total_localities")
        public int totalLocalities;
        @SerializedName("total_categories")
        public int totalCategories;
        @SerializedName("total_subcategories")
        public int totalSubcategories;
        @SerializedName("total_providers")
        public int totalProviders;
        @SerializedName("total_products_services")
        public int totalProductsServices;
        @SerializedName("total_listings")
        public int totalListings;
        @SerializedName("total_price_observations")
        public int totalPriceObservations;
        @SerializedName("active_localities")
        public List<String> activeLocalities;
    }

    // --- Locality Master ---
    public static class LocalityModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("name")
        public String name;
        @SerializedName("district")
        public String district;
        @SerializedName("pincode")
        public String pincode;
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
        @SerializedName("is_active")
        public boolean isActive;

        @Override
        public String toString() {
            return name;
        }
    }

    // --- Category & Subcategory Taxonomy ---
    public static class SubcategoryModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("category_id")
        public long categoryId;
        @SerializedName("name")
        public String name;
        @SerializedName("slug")
        public String slug;
        @SerializedName("description")
        public String description;
        @SerializedName("item_type")
        public String itemType;
        @SerializedName("display_order")
        public int displayOrder;
        @SerializedName("is_active")
        public boolean isActive;

        @Override
        public String toString() {
            return name;
        }
    }

    public static class CategoryModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("name")
        public String name;
        @SerializedName("slug")
        public String slug;
        @SerializedName("description")
        public String description;
        @SerializedName("icon_key")
        public String iconKey;
        @SerializedName("display_order")
        public int displayOrder;
        @SerializedName("is_active")
        public boolean isActive;
        @SerializedName("subcategories")
        public List<SubcategoryModel> subcategories;

        @Override
        public String toString() {
            return name;
        }
    }

    // --- Price Types ---
    public static class PriceTypeModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("category_id")
        public long categoryId;
        @SerializedName("code")
        public String code;
        @SerializedName("name")
        public String name;
        @SerializedName("description")
        public String description;
        @SerializedName("is_recurring")
        public boolean isRecurring;

        @Override
        public String toString() {
            return name;
        }
    }

    // --- Listings ---
    public static class ListingModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("user_id")
        public Long userId;
        @SerializedName("user_name")
        public String userName;
        @SerializedName("custom_title")
        public String customTitle;
        @SerializedName("price_amount")
        public Double priceAmount;
        @SerializedName("is_price_available")
        public boolean isPriceAvailable = true;
        @SerializedName("currency")
        public String currency;
        @SerializedName("status")
        public String status;
        @SerializedName("moderation_status")
        public String moderationStatus;
        @SerializedName("rejection_reason")
        public String rejectionReason;
        @SerializedName("source_type")
        public String sourceType;
        @SerializedName("price_type_code")
        public String priceTypeCode;
        @SerializedName("price_type_name")
        public String priceTypeName;
        @SerializedName("category_id")
        public long categoryId;
        @SerializedName("category_name")
        public String categoryName;
        @SerializedName("category_slug")
        public String categorySlug;
        @SerializedName("subcategory_id")
        public long subcategoryId;
        @SerializedName("subcategory_name")
        public String subcategoryName;
        @SerializedName("subcategory_slug")
        public String subcategorySlug;
        @SerializedName("provider_id")
        public long providerId;
        @SerializedName("provider_name")
        public String providerName;
        @SerializedName("branch_name")
        public String branchName;
        @SerializedName("locality_id")
        public long localityId;
        @SerializedName("area_name")
        public String areaName;
        @SerializedName("address_line")
        public String addressLine;
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
        @SerializedName("is_available")
        public boolean isAvailable;
        @SerializedName("evidence_reference")
        public String evidenceReference;
        @SerializedName("attributes")
        public Map<String, Object> attributes;
        @SerializedName("notes")
        public String notes;
        @SerializedName("created_at")
        public String createdAt;
        @SerializedName("updated_at")
        public String updatedAt;
    }

    public static class CreateListingRequest {
        @SerializedName("locality_id")
        public long localityId;
        @SerializedName("category_id")
        public long categoryId;
        @SerializedName("subcategory_id")
        public long subcategoryId;
        @SerializedName("title")
        public String title;
        @SerializedName("brand_or_provider_name")
        public String brandOrProviderName;
        @SerializedName("price_amount")
        public Double priceAmount;
        @SerializedName("price_type_id")
        public long priceTypeId;
        @SerializedName("currency")
        public String currency = "INR";
        @SerializedName("source_type")
        public String sourceType = "USER_SUBMITTED";
        @SerializedName("is_price_available")
        public boolean isPriceAvailable = true;
        @SerializedName("evidence_reference")
        public String evidenceReference;
        @SerializedName("address_line")
        public String addressLine;
        @SerializedName("notes")
        public String notes;
        @SerializedName("attributes")
        public Map<String, Object> attributes = new HashMap<>();

        public CreateListingRequest(long localityId, long categoryId, long subcategoryId,
                                    String title, String brandOrProviderName, Double priceAmount,
                                    long priceTypeId, String notes) {
            this.localityId = localityId;
            this.categoryId = categoryId;
            this.subcategoryId = subcategoryId;
            this.title = title;
            this.brandOrProviderName = brandOrProviderName;
            this.priceAmount = priceAmount;
            this.priceTypeId = priceTypeId;
            this.notes = notes;
            this.attributes = new HashMap<>();
        }

        public CreateListingRequest(long userId, long localityId, long categoryId, long subcategoryId,
                                    String title, String brandOrProviderName, Double priceAmount,
                                    long priceTypeId, String notes) {
            this(localityId, categoryId, subcategoryId, title, brandOrProviderName, priceAmount, priceTypeId, notes);
        }

        public CreateListingRequest(long localityId, long categoryId, long subcategoryId,
                                    String title, String brandOrProviderName, Double priceAmount,
                                    long priceTypeId, String addressLine, String evidenceReference,
                                    String notes, Map<String, Object> attributes) {
            this(localityId, categoryId, subcategoryId, title, brandOrProviderName, priceAmount, priceTypeId, notes);
            this.addressLine = addressLine;
            this.evidenceReference = evidenceReference;
            if (attributes != null) {
                this.attributes = attributes;
            }
        }
    }

    public static class UpdateListingRequest {
        @SerializedName("custom_title")
        public String customTitle;
        @SerializedName("price_amount")
        public Double priceAmount;
        @SerializedName("is_available")
        public Boolean isAvailable;
        @SerializedName("notes")
        public String notes;

        public UpdateListingRequest(String customTitle, Double priceAmount, Boolean isAvailable, String notes) {
            this.customTitle = customTitle;
            this.priceAmount = priceAmount;
            this.isAvailable = isAvailable;
            this.notes = notes;
        }
    }

    public static class ModerateListingRequest {
        @SerializedName("action")
        public String action; // approve, reject, suspend, restore, archive
        @SerializedName("rejection_reason")
        public String rejectionReason;
        @SerializedName("admin_notes")
        public String adminNotes;

        public ModerateListingRequest(String action, String rejectionReason, String adminNotes) {
            this.action = action;
            this.rejectionReason = rejectionReason;
            this.adminNotes = adminNotes;
        }

        public ModerateListingRequest(String action, String rejectionReason) {
            this(action, rejectionReason, null);
        }
    }

    // --- Price History & Observations ---
    public static class PriceObservationModel implements Serializable {
        @SerializedName("id")
        public long id;
        @SerializedName("listing_id")
        public long listingId;
        @SerializedName("price_type_id")
        public long priceTypeId;
        @SerializedName("price_type_code")
        public String priceTypeCode;
        @SerializedName("price_type_name")
        public String priceTypeName;
        @SerializedName("price_amount")
        public Double priceAmount;
        @SerializedName("currency")
        public String currency;
        @SerializedName("source_type")
        public String sourceType;
        @SerializedName("submission_date")
        public String submissionDate;
        @SerializedName("effective_date")
        public String effectiveDate;
        @SerializedName("verification_status")
        public String verificationStatus;
        @SerializedName("evidence_reference")
        public String evidenceReference;
        @SerializedName("notes")
        public String notes;
    }

    // --- Reporting ---
    public static class ReportRequest {
        @SerializedName("reason_category")
        public String reasonCategory;
        @SerializedName("details")
        public String details;

        public ReportRequest(String reasonCategory, String details) {
            this.reasonCategory = reasonCategory;
            this.details = details;
        }
    }

    public static class ReportResponse {
        @SerializedName("id")
        public long id;
        @SerializedName("listing_id")
        public long listingId;
        @SerializedName("listing_title")
        public String listingTitle;
        @SerializedName("reporter_user_id")
        public long reporterUserId;
        @SerializedName("reporter_name")
        public String reporterName;
        @SerializedName("reason_category")
        public String reasonCategory;
        @SerializedName("details")
        public String details;
        @SerializedName("review_status")
        public String reviewStatus;
        @SerializedName("created_at")
        public String createdAt;
    }

    // --- Comparison Response ---
    public static class ComparisonResponse {
        @SerializedName("target_listing")
        public ListingModel targetListing;
        @SerializedName("reference_price")
        public Double referencePrice;
        @SerializedName("lowest_price")
        public Double lowestPrice;
        @SerializedName("highest_price")
        public Double highestPrice;
        @SerializedName("price_difference_amount")
        public Double priceDifferenceAmount;
        @SerializedName("price_difference_percent")
        public Double priceDifferencePercent;
        @SerializedName("price_label")
        public String priceLabel;
        @SerializedName("sample_size")
        public int sampleSize;
        @SerializedName("comparables")
        public List<ListingModel> comparables;
    }

    // --- Fair Price Evaluation Legacy Support ---
    public static class ComparableItemRequest {
        @SerializedName("price")
        public String price;
        @SerializedName("timestamp")
        public String timestamp;

        public ComparableItemRequest(String price, String timestamp) {
            this.price = price;
            this.timestamp = timestamp;
        }
    }

    public static class FairPriceRequest {
        @SerializedName("listed_price")
        public String listedPrice;
        @SerializedName("category")
        public String category;
        @SerializedName("comparables")
        public List<ComparableItemRequest> comparables;

        public FairPriceRequest(String listedPrice, String category, List<ComparableItemRequest> comparables) {
            this.listedPrice = listedPrice;
            this.category = category;
            this.comparables = comparables;
        }
    }

    public static class FairPriceResponse {
        @SerializedName("listed_price")
        public String listedPrice;
        @SerializedName("reference_price")
        public String referencePrice;
        @SerializedName("price_deviation_amount")
        public String priceDeviationAmount;
        @SerializedName("price_deviation_percent")
        public double priceDeviationPercent;
        @SerializedName("price_label")
        public String priceLabel;
        @SerializedName("confidence_level")
        public String confidenceLevel;
        @SerializedName("sample_size")
        public int sampleSize;
        @SerializedName("data_freshness_days")
        public Integer dataFreshnessDays;
        @SerializedName("explanation")
        public String explanation;
        @SerializedName("limitations")
        public List<String> limitations;
        @SerializedName("metrics")
        public Map<String, Object> metrics;
    }
}
