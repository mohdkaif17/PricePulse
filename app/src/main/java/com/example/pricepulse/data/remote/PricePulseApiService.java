package com.example.pricepulse.data.remote;

import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PricePulseApiService {

    // --- Health Check ---
    @GET("health")
    Call<ApiModels.HealthResponse> getHealth();

    // --- Auth Endpoints (Phase 9) ---
    @POST("auth/login")
    Call<ApiModels.TokenResponse> login(@Body ApiModels.LoginRequest request);

    @POST("auth/register")
    Call<ApiModels.TokenResponse> register(@Body ApiModels.RegisterRequest request);

    @POST("auth/logout")
    Call<Map<String, Object>> logout();

    @GET("auth/me")
    Call<ApiModels.UserModel> getMyProfile();

    @GET("users/profile")
    Call<ApiModels.UserModel> getUserProfile();

    @PATCH("users/profile")
    Call<ApiModels.UserModel> updateProfile(@Body ApiModels.UpdateProfileRequest request);

    @POST("auth/change-password")
    Call<Map<String, Object>> changePassword(@Body ApiModels.ChangePasswordRequest request);

    // --- Master Data Endpoints (Phase 11) ---
    @GET("meta/summary")
    Call<ApiModels.SummaryResponse> getSummary();

    @GET("areas")
    Call<List<ApiModels.LocalityModel>> getAreas();

    @GET("localities")
    Call<List<ApiModels.LocalityModel>> getLocalities();

    @GET("categories")
    Call<List<ApiModels.CategoryModel>> getCategories();

    @GET("categories/{id}/subcategories")
    Call<List<ApiModels.SubcategoryModel>> getSubcategories(@Path("id") long categoryId);

    @GET("providers")
    Call<List<Map<String, Object>>> getProviders(@Query("areaId") Long areaId);

    @GET("price-types")
    Call<List<ApiModels.PriceTypeModel>> getPriceTypes(@Query("category_id") Long categoryId);

    // --- Public Listings & PostGIS Spatial (Phase 10 & 11) ---
    @GET("listings")
    Call<List<ApiModels.ListingModel>> getListings(
            @Query("locality_id") Long localityId,
            @Query("category_id") Long categoryId,
            @Query("subcategory_id") Long subcategoryId,
            @Query("search") String search,
            @Query("sort_by") String sortBy,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset
    );

    @GET("listings/nearby")
    Call<List<ApiModels.ListingModel>> getNearbyListings(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radius_km") double radiusKm,
            @Query("category_id") Long categoryId
    );

    @GET("listings/{id}")
    Call<ApiModels.ListingModel> getListingDetail(@Path("id") long id);

    @GET("listings/{id}/compare")
    Call<ApiModels.ComparisonResponse> getListingComparables(@Path("id") long id);

    @GET("listings/{id}/price-history")
    Call<List<ApiModels.PriceObservationModel>> getListingPriceHistory(@Path("id") long id);

    // --- User-Controlled Listing Management (Phase 8 & 9) ---
    @GET("my-listings")
    Call<List<ApiModels.ListingModel>> getMyListings(@Query("status_filter") String statusFilter);

    @POST("listings")
    Call<ApiModels.ListingModel> createListing(@Body ApiModels.CreateListingRequest request);

    @PATCH("listings/{id}")
    Call<ApiModels.ListingModel> updateListing(@Path("id") long id, @Body ApiModels.UpdateListingRequest request);

    @DELETE("listings/{id}")
    Call<Map<String, Object>> deleteListing(@Path("id") long id);

    @POST("listings/{id}/price-observations")
    Call<ApiModels.PriceObservationModel> addPriceObservation(
            @Path("id") long id,
            @Body Map<String, Object> observationPayload
    );

    @POST("listings/{id}/reports")
    Call<ApiModels.ReportResponse> reportListing(
            @Path("id") long id,
            @Body ApiModels.ReportRequest request
    );

    // --- Administrator Moderation (Phase 8.5) ---
    @GET("admin/listings")
    Call<List<ApiModels.ListingModel>> adminGetListings(
            @Query("status_filter") String statusFilter,
            @Query("search") String search
    );

    @POST("admin/listings/{id}/moderate")
    Call<ApiModels.ListingModel> adminModerateListing(
            @Path("id") long id,
            @Body ApiModels.ModerateListingRequest request
    );

    @POST("admin/listings/{id}/moderate")
    Call<ApiModels.ListingModel> moderateListing(
            @Path("id") long id,
            @Body ApiModels.ModerateListingRequest request
    );

    @GET("admin/reports")
    Call<List<ApiModels.ReportResponse>> adminGetReports(
            @Query("review_status") String reviewStatus
    );

    // --- Legacy Fair Price Engine Support ---
    @POST("fair-price/evaluate")
    Call<ApiModels.FairPriceResponse> evaluateFairPrice(@Body ApiModels.FairPriceRequest request);
}
