package com.example.pricepulse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.adapters.ManageListingsAdapter;
import com.example.pricepulse.data.remote.ApiClient;
import com.example.pricepulse.data.remote.ApiModels;
import com.example.pricepulse.data.remote.PricePulseApiService;
import com.example.pricepulse.data.remote.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageListingsActivity extends AppCompatActivity implements ManageListingsAdapter.OnListingActionListener {

    private enum FilterMode {
        ALL_LISTINGS,
        MY_LISTINGS,
        ADMIN_MODERATION
    }

    private FilterMode currentFilter = FilterMode.ALL_LISTINGS;
    private ManageListingsAdapter adapter;
    private PricePulseApiService apiService;
    private SessionManager sessionManager;

    private TextView chipFilterAll;
    private TextView chipFilterMyListings;
    private TextView chipFilterPending;
    private TextView txtListingStats;
    private View layoutEmptyState;

    // Master metadata cached for dialogs
    private final List<ApiModels.LocalityModel> localitiesList = new ArrayList<>();
    private final List<ApiModels.CategoryModel> categoriesList = new ArrayList<>();
    private final List<ApiModels.PriceTypeModel> priceTypesList = new ArrayList<>();

    private static final long CURRENT_USER_ID = 2L; // Kaif Demo User ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_listings);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        initViews();
        setupFilterChips();

        String initialFilter = getIntent().getStringExtra("initial_filter");
        if ("MY_LISTINGS".equalsIgnoreCase(initialFilter)) {
            currentFilter = FilterMode.MY_LISTINGS;
            updateFilterChipStyles();
        }

        loadMetadata();
        fetchListings();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddNewListing).setOnClickListener(v -> showAddListingDialog());

        chipFilterAll = findViewById(R.id.chipFilterAll);
        chipFilterMyListings = findViewById(R.id.chipFilterMyListings);
        chipFilterPending = findViewById(R.id.chipFilterPending);
        txtListingStats = findViewById(R.id.txtListingStats);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);

        RecyclerView rv = findViewById(R.id.rvListings);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ManageListingsAdapter(this, this);
        rv.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipFilterAll.setOnClickListener(v -> {
            currentFilter = FilterMode.ALL_LISTINGS;
            updateFilterChipStyles();
            fetchListings();
        });

        chipFilterMyListings.setOnClickListener(v -> {
            currentFilter = FilterMode.MY_LISTINGS;
            updateFilterChipStyles();
            fetchListings();
        });

        chipFilterPending.setOnClickListener(v -> {
            currentFilter = FilterMode.ADMIN_MODERATION;
            updateFilterChipStyles();
            fetchListings();
        });
    }

    private void updateFilterChipStyles() {
        chipFilterAll.setBackgroundResource(currentFilter == FilterMode.ALL_LISTINGS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipFilterAll.setTextColor(getResources().getColor(currentFilter == FilterMode.ALL_LISTINGS ? R.color.primary : R.color.text_secondary));

        chipFilterMyListings.setBackgroundResource(currentFilter == FilterMode.MY_LISTINGS ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipFilterMyListings.setTextColor(getResources().getColor(currentFilter == FilterMode.MY_LISTINGS ? R.color.primary : R.color.text_secondary));

        chipFilterPending.setBackgroundResource(currentFilter == FilterMode.ADMIN_MODERATION ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipFilterPending.setTextColor(getResources().getColor(currentFilter == FilterMode.ADMIN_MODERATION ? R.color.primary : R.color.text_secondary));
    }

    private void loadMetadata() {
        apiService.getLocalities().enqueue(new Callback<List<ApiModels.LocalityModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.LocalityModel>> call, Response<List<ApiModels.LocalityModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    localitiesList.clear();
                    localitiesList.addAll(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<ApiModels.LocalityModel>> call, Throwable t) {}
        });

        apiService.getCategories().enqueue(new Callback<List<ApiModels.CategoryModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.CategoryModel>> call, Response<List<ApiModels.CategoryModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoriesList.clear();
                    categoriesList.addAll(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<ApiModels.CategoryModel>> call, Throwable t) {}
        });

        apiService.getPriceTypes(null).enqueue(new Callback<List<ApiModels.PriceTypeModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.PriceTypeModel>> call, Response<List<ApiModels.PriceTypeModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    priceTypesList.clear();
                    priceTypesList.addAll(response.body());
                }
            }
            @Override
            public void onFailure(Call<List<ApiModels.PriceTypeModel>> call, Throwable t) {}
        });
    }

    private void fetchListings() {
        Call<List<ApiModels.ListingModel>> call;
        if (currentFilter == FilterMode.MY_LISTINGS) {
            if (sessionManager == null || !sessionManager.isLoggedIn()) {
                Toast.makeText(this, "Please log in to view your listings", Toast.LENGTH_SHORT).show();
                adapter.setListings(new ArrayList<>());
                layoutEmptyState.setVisibility(View.VISIBLE);
                txtListingStats.setText("Log in to view your listings");
                return;
            }
            call = apiService.getMyListings(null);
        } else if (currentFilter == FilterMode.ADMIN_MODERATION) {
            if (sessionManager == null || !sessionManager.isAdmin()) {
                Toast.makeText(this, "Admin privileges required for moderation", Toast.LENGTH_SHORT).show();
                adapter.setListings(new ArrayList<>());
                layoutEmptyState.setVisibility(View.VISIBLE);
                txtListingStats.setText("Admin privileges required for moderation");
                return;
            }
            call = apiService.adminGetListings("pending", null);
        } else {
            call = apiService.getListings(null, null, null, null, null, 100, 0);
        }

        call.enqueue(new Callback<List<ApiModels.ListingModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.ListingModel>> call, Response<List<ApiModels.ListingModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ApiModels.ListingModel> items = response.body();
                    adapter.setListings(items);

                    boolean isEmpty = items.isEmpty();
                    layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    txtListingStats.setText("Showing " + items.size() + " listings in Bengaluru");
                } else {
                    Toast.makeText(ManageListingsActivity.this, "Failed to load listings: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ApiModels.ListingModel>> call, Throwable t) {
                Toast.makeText(ManageListingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePriceTypeSpinner(long categoryId, Spinner spPriceType) {
        List<ApiModels.PriceTypeModel> filtered = new ArrayList<>();
        for (ApiModels.PriceTypeModel pt : priceTypesList) {
            if (pt.categoryId == categoryId) {
                filtered.add(pt);
            }
        }
        if (!filtered.isEmpty()) {
            ArrayAdapter<ApiModels.PriceTypeModel> ptAdapter = new ArrayAdapter<>(
                    ManageListingsActivity.this,
                    android.R.layout.simple_spinner_item,
                    filtered
            );
            ptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spPriceType.setAdapter(ptAdapter);
        } else {
            apiService.getPriceTypes(categoryId).enqueue(new Callback<List<ApiModels.PriceTypeModel>>() {
                @Override
                public void onResponse(Call<List<ApiModels.PriceTypeModel>> call, Response<List<ApiModels.PriceTypeModel>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        priceTypesList.addAll(response.body());
                        ArrayAdapter<ApiModels.PriceTypeModel> ptAdapter = new ArrayAdapter<>(
                                ManageListingsActivity.this,
                                android.R.layout.simple_spinner_item,
                                response.body()
                        );
                        ptAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spPriceType.setAdapter(ptAdapter);
                    }
                }
                @Override
                public void onFailure(Call<List<ApiModels.PriceTypeModel>> call, Throwable t) {}
            });
        }
    }

    private void showAddListingDialog() {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please log in to add a listing", Toast.LENGTH_LONG).show();
            startActivity(new android.content.Intent(this, com.example.pricepulse.activities.LoginActivity.class));
            return;
        }

        if (localitiesList.isEmpty() || categoriesList.isEmpty()) {
            Toast.makeText(this, "Loading metadata from server, please wait...", Toast.LENGTH_SHORT).show();
            loadMetadata();
            return;
        }

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_listing, null);
        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etProvider = dialogView.findViewById(R.id.etDialogProvider);
        EditText etPrice = dialogView.findViewById(R.id.etDialogPrice);
        EditText etNotes = dialogView.findViewById(R.id.etDialogNotes);

        Spinner spLocality = dialogView.findViewById(R.id.spDialogLocality);
        Spinner spCategory = dialogView.findViewById(R.id.spDialogCategory);
        Spinner spSubcategory = dialogView.findViewById(R.id.spDialogSubcategory);
        Spinner spPriceType = dialogView.findViewById(R.id.spDialogPriceType);

        // Populate Localities
        ArrayAdapter<ApiModels.LocalityModel> locAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, localitiesList);
        locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLocality.setAdapter(locAdapter);

        // Populate Categories
        ArrayAdapter<ApiModels.CategoryModel> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesList);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // Dynamic Subcategories and Price Types on Category Selection
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ApiModels.CategoryModel selectedCat = categoriesList.get(position);
                if (selectedCat.subcategories != null && !selectedCat.subcategories.isEmpty()) {
                    ArrayAdapter<ApiModels.SubcategoryModel> subAdapter = new ArrayAdapter<>(
                            ManageListingsActivity.this,
                            android.R.layout.simple_spinner_item,
                            selectedCat.subcategories
                    );
                    subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spSubcategory.setAdapter(subAdapter);
                } else {
                    apiService.getSubcategories(selectedCat.id).enqueue(new Callback<List<ApiModels.SubcategoryModel>>() {
                        @Override
                        public void onResponse(Call<List<ApiModels.SubcategoryModel>> call, Response<List<ApiModels.SubcategoryModel>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                selectedCat.subcategories = response.body();
                                ArrayAdapter<ApiModels.SubcategoryModel> subAdapter = new ArrayAdapter<>(
                                        ManageListingsActivity.this,
                                        android.R.layout.simple_spinner_item,
                                        response.body()
                                );
                                subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spSubcategory.setAdapter(subAdapter);
                            }
                        }
                        @Override
                        public void onFailure(Call<List<ApiModels.SubcategoryModel>> call, Throwable t) {}
                    });
                }
                updatePriceTypeSpinner(selectedCat.id, spPriceType);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Initial Price Type populate based on initial category
        if (!categoriesList.isEmpty()) {
            updatePriceTypeSpinner(categoriesList.get(0).id, spPriceType);
        }

        new AlertDialog.Builder(this)
                .setTitle("Create New Bengaluru Listing")
                .setView(dialogView)
                .setPositiveButton("Submit Listing", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String provider = etProvider.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    if (title.isEmpty() || provider.isEmpty() || priceStr.isEmpty()) {
                        Toast.makeText(this, "Please fill in title, provider, and price", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double priceVal;
                    try {
                        priceVal = Double.parseDouble(priceStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid price number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ApiModels.LocalityModel selectedLoc = (ApiModels.LocalityModel) spLocality.getSelectedItem();
                    ApiModels.CategoryModel selectedCat = (ApiModels.CategoryModel) spCategory.getSelectedItem();
                    ApiModels.SubcategoryModel selectedSub = (ApiModels.SubcategoryModel) spSubcategory.getSelectedItem();
                    ApiModels.PriceTypeModel selectedPt = (ApiModels.PriceTypeModel) spPriceType.getSelectedItem();

                    if (selectedLoc == null || selectedCat == null || selectedSub == null || selectedPt == null) {
                        Toast.makeText(this, "Please select locality, category, and price type", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ApiModels.CreateListingRequest req = new ApiModels.CreateListingRequest(
                            sessionManager.getUserId(),
                            selectedLoc.id,
                            selectedCat.id,
                            selectedSub.id,
                            title,
                            provider,
                            priceVal,
                            selectedPt.id,
                            notes
                    );

                    apiService.createListing(req).enqueue(new Callback<ApiModels.ListingModel>() {
                        @Override
                        public void onResponse(Call<ApiModels.ListingModel> call, Response<ApiModels.ListingModel> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageListingsActivity.this, "Listing submitted successfully! (Status: Pending Moderation)", Toast.LENGTH_LONG).show();
                                currentFilter = FilterMode.MY_LISTINGS;
                                updateFilterChipStyles();
                                fetchListings();
                            } else {
                                Toast.makeText(ManageListingsActivity.this, "Server rejected listing: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiModels.ListingModel> call, Throwable t) {
                            Toast.makeText(ManageListingsActivity.this, "Failed to submit: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEdit(ApiModels.ListingModel listing) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_listing, null);
        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etProvider = dialogView.findViewById(R.id.etDialogProvider);
        EditText etPrice = dialogView.findViewById(R.id.etDialogPrice);
        EditText etNotes = dialogView.findViewById(R.id.etDialogNotes);

        String currentTitle = (listing.customTitle != null && !listing.customTitle.isEmpty())
                ? listing.customTitle : "Listing #" + listing.id;
        String currentProvider = (listing.providerName != null && !listing.providerName.isEmpty())
                ? listing.providerName : "";

        etTitle.setText(currentTitle);
        etProvider.setText(currentProvider);
        etProvider.setEnabled(false); // Locked for editing
        if (listing.priceAmount != null) {
            etPrice.setText(String.valueOf(listing.priceAmount));
        }

        // Hide selectors for quick price/title edit
        dialogView.findViewById(R.id.spDialogLocality).setVisibility(View.GONE);
        dialogView.findViewById(R.id.spDialogCategory).setVisibility(View.GONE);
        dialogView.findViewById(R.id.spDialogSubcategory).setVisibility(View.GONE);
        dialogView.findViewById(R.id.spDialogPriceType).setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle("Edit Listing #" + listing.id)
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String priceStr = etPrice.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    Double priceVal = null;
                    if (!priceStr.isEmpty()) {
                        try {
                            priceVal = Double.parseDouble(priceStr);
                        } catch (Exception ignored) {}
                    }

                    ApiModels.UpdateListingRequest updateReq = new ApiModels.UpdateListingRequest(
                            title.isEmpty() ? null : title,
                            priceVal,
                            null,
                            notes.isEmpty() ? null : notes
                    );

                    apiService.updateListing(listing.id, updateReq).enqueue(new Callback<ApiModels.ListingModel>() {
                        @Override
                        public void onResponse(Call<ApiModels.ListingModel> call, Response<ApiModels.ListingModel> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageListingsActivity.this, "Listing updated! Price history recorded.", Toast.LENGTH_SHORT).show();
                                fetchListings();
                            } else {
                                Toast.makeText(ManageListingsActivity.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiModels.ListingModel> call, Throwable t) {
                            Toast.makeText(ManageListingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(ApiModels.ListingModel listing) {
        String displayTitle = (listing.customTitle != null && !listing.customTitle.isEmpty())
                ? listing.customTitle : "Listing #" + listing.id;

        new AlertDialog.Builder(this)
                .setTitle("Delete Listing")
                .setMessage("Are you sure you want to remove '" + displayTitle + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    apiService.deleteListing(listing.id).enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageListingsActivity.this, "Listing removed", Toast.LENGTH_SHORT).show();
                                fetchListings();
                            } else {
                                Toast.makeText(ManageListingsActivity.this, "Delete failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(ManageListingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onModerate(ApiModels.ListingModel listing) {
        String[] options = new String[]{"Approve Listing", "Reject Listing", "Keep in Pending"};
        new AlertDialog.Builder(this)
                .setTitle("Admin Moderation • #" + listing.id)
                .setItems(options, (dialog, which) -> {
                    String status = which == 0 ? "active" : (which == 1 ? "rejected" : "pending");
                    ApiModels.ModerateListingRequest modReq = new ApiModels.ModerateListingRequest(status, "Reviewed by administrator");
                    apiService.moderateListing(listing.id, modReq).enqueue(new Callback<ApiModels.ListingModel>() {
                        @Override
                        public void onResponse(Call<ApiModels.ListingModel> call, Response<ApiModels.ListingModel> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageListingsActivity.this, "Listing status updated to: " + status, Toast.LENGTH_SHORT).show();
                                fetchListings();
                            } else {
                                Toast.makeText(ManageListingsActivity.this, "Moderation failed", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiModels.ListingModel> call, Throwable t) {
                            Toast.makeText(ManageListingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    @Override
    public void onReport(ApiModels.ListingModel listing) {
        String[] categories = new String[]{
                "Wrong Information / Price",
                "Duplicate Listing",
                "Misleading Price / Hidden Fees",
                "Outdated Info",
                "Fake Provider",
                "Inappropriate Content / Spam",
                "Other Issue"
        };
        String[] rawCodes = new String[]{
                "incorrect_info",
                "duplicate_listing",
                "misleading_price",
                "outdated_info",
                "fake_provider",
                "inappropriate_content",
                "other"
        };

        final int[] selectedIndex = {0};
        new AlertDialog.Builder(this)
                .setTitle("Report Listing #" + listing.id)
                .setSingleChoiceItems(categories, 0, (dialog, which) -> selectedIndex[0] = which)
                .setPositiveButton("Submit Report", (dialog, which) -> {
                    String chosenCode = rawCodes[selectedIndex[0]];
                    ApiModels.ReportRequest req = new ApiModels.ReportRequest(chosenCode, "Reported by user from listing interface");
                    apiService.reportListing(listing.id, req).enqueue(new Callback<ApiModels.ReportResponse>() {
                        @Override
                        public void onResponse(Call<ApiModels.ReportResponse> call, Response<ApiModels.ReportResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(ManageListingsActivity.this, "Listing reported. Our moderation team will review it.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ManageListingsActivity.this, "Report submission failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiModels.ReportResponse> call, Throwable t) {
                            Toast.makeText(ManageListingsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
