package com.example.pricepulse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pricepulse.activities.LoginActivity;
import com.example.pricepulse.data.remote.ApiClient;
import com.example.pricepulse.data.remote.ApiModels;
import com.example.pricepulse.data.remote.PricePulseApiService;
import com.example.pricepulse.data.remote.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PulsePostActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private PricePulseApiService apiService;

    // Views
    private Spinner spCategory;
    private Spinner spSubcategory;
    private Spinner spPriceType;
    private Spinner spLocality;
    private EditText etTitle;
    private EditText etProvider;
    private EditText etPrice;
    private EditText etNotes;
    private EditText etAddressLine;
    private EditText etEvidenceRef;

    // Dynamic Attribute Views
    private LinearLayout layoutAttrRealEstate;
    private Spinner spAttrBhk;
    private Spinner spAttrFurnishing;
    private EditText etAttrSqft;

    private LinearLayout layoutAttrElectronics;
    private Spinner spAttrStorage;
    private Spinner spAttrCondition;

    private LinearLayout layoutAttrAutomotive;
    private Spinner spAttrFuel;
    private Spinner spAttrTransmission;

    private LinearLayout layoutAttrHealthcare;
    private Spinner spAttrMedicalType;

    private LinearLayout layoutAttrGeneric;
    private EditText etAttrGeneric;

    // Location & Media Views
    private TextView txtGpsStatus;
    private MaterialButton btnDetectGps;
    private MaterialButton btnPickImage;
    private LinearLayout layoutImagePreview;
    private ImageView imgPreview;
    private TextView txtImageFileName;
    private ImageView btnRemoveImage;

    // Submission
    private MaterialButton btnSubmit;
    private ProgressBar progressSubmit;

    // Cached metadata from backend
    private final List<ApiModels.CategoryModel> categoriesList = new ArrayList<>();
    private final List<ApiModels.LocalityModel> localitiesList = new ArrayList<>();
    private final List<ApiModels.PriceTypeModel> priceTypesList = new ArrayList<>();

    private Uri selectedImageUri = null;
    private Double detectedLatitude = null;
    private Double detectedLongitude = null;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pulse_post);

        View rootView = findViewById(R.id.pulsePostRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please log in to submit a PulsePost", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        apiService = ApiClient.getApiService(this);

        initViews();
        setupLaunchers();
        setupStaticSpinners();
        loadMetadata();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnMyPostsTop).setOnClickListener(v -> openMyPulsePosts());

        spCategory = findViewById(R.id.spCategory);
        spSubcategory = findViewById(R.id.spSubcategory);
        spPriceType = findViewById(R.id.spPriceType);
        spLocality = findViewById(R.id.spLocality);

        etTitle = findViewById(R.id.etTitle);
        etProvider = findViewById(R.id.etProvider);
        etPrice = findViewById(R.id.etPrice);
        etNotes = findViewById(R.id.etNotes);
        etAddressLine = findViewById(R.id.etAddressLine);
        etEvidenceRef = findViewById(R.id.etEvidenceRef);

        // Dynamic attribute sections
        layoutAttrRealEstate = findViewById(R.id.layoutAttrRealEstate);
        spAttrBhk = findViewById(R.id.spAttrBhk);
        spAttrFurnishing = findViewById(R.id.spAttrFurnishing);
        etAttrSqft = findViewById(R.id.etAttrSqft);

        layoutAttrElectronics = findViewById(R.id.layoutAttrElectronics);
        spAttrStorage = findViewById(R.id.spAttrStorage);
        spAttrCondition = findViewById(R.id.spAttrCondition);

        layoutAttrAutomotive = findViewById(R.id.layoutAttrAutomotive);
        spAttrFuel = findViewById(R.id.spAttrFuel);
        spAttrTransmission = findViewById(R.id.spAttrTransmission);

        layoutAttrHealthcare = findViewById(R.id.layoutAttrHealthcare);
        spAttrMedicalType = findViewById(R.id.spAttrMedicalType);

        layoutAttrGeneric = findViewById(R.id.layoutAttrGeneric);
        etAttrGeneric = findViewById(R.id.etAttrGeneric);

        // Location & Media
        txtGpsStatus = findViewById(R.id.txtGpsStatus);
        btnDetectGps = findViewById(R.id.btnDetectGps);
        btnPickImage = findViewById(R.id.btnPickImage);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        imgPreview = findViewById(R.id.imgPreview);
        txtImageFileName = findViewById(R.id.txtImageFileName);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);

        btnSubmit = findViewById(R.id.btnSubmitPulsePost);
        progressSubmit = findViewById(R.id.progressSubmit);

        btnDetectGps.setOnClickListener(v -> requestGpsLocation());
        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnRemoveImage.setOnClickListener(v -> clearSelectedImage());
        btnSubmit.setOnClickListener(v -> validateAndSubmitPulsePost());
    }

    private void setupLaunchers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        layoutImagePreview.setVisibility(View.VISIBLE);
                        imgPreview.setImageURI(uri);
                        txtImageFileName.setText("Attached: " + uri.getLastPathSegment());
                        if (etEvidenceRef.getText().toString().trim().isEmpty()) {
                            etEvidenceRef.setText("device_gallery://" + uri.getLastPathSegment());
                        }
                    }
                }
        );

        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fineGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    boolean coarseGranted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (fineGranted || coarseGranted) {
                        fetchDeviceLocation();
                    } else {
                        txtGpsStatus.setText("Location permission denied (using manual locality)");
                    }
                }
        );
    }

    private void clearSelectedImage() {
        selectedImageUri = null;
        layoutImagePreview.setVisibility(View.GONE);
        imgPreview.setImageDrawable(null);
        txtImageFileName.setText("");
        etEvidenceRef.setText("");
    }

    private void setupStaticSpinners() {
        // Real Estate: BHK
        ArrayAdapter<String> bhkAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"1 RK", "1 BHK", "2 BHK", "3 BHK", "4+ BHK", "Single Room PG", "Double Sharing PG", "Studio Apartment", "Commercial Office"});
        bhkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrBhk.setAdapter(bhkAdapter);

        // Real Estate: Furnishing
        ArrayAdapter<String> furnAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Semi-Furnished", "Fully Furnished", "Unfurnished"});
        furnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrFurnishing.setAdapter(furnAdapter);

        // Electronics: Storage
        ArrayAdapter<String> storageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"64 GB", "128 GB", "256 GB", "512 GB", "1 TB", "Not Applicable"});
        storageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrStorage.setAdapter(storageAdapter);

        // Electronics: Condition
        ArrayAdapter<String> condAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Brand New Sealed", "Manufacturer Refurbished", "Open Box / Demo Unit", "Pre-Owned Good"});
        condAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrCondition.setAdapter(condAdapter);

        // Automotive: Fuel
        ArrayAdapter<String> fuelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Petrol", "Diesel", "Electric (EV)", "CNG", "Hybrid"});
        fuelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrFuel.setAdapter(fuelAdapter);

        // Automotive: Transmission
        ArrayAdapter<String> transAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Manual", "Automatic", "Periodic Service / Maintenance"});
        transAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrTransmission.setAdapter(transAdapter);

        // Healthcare: Service Type
        ArrayAdapter<String> medAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"In-Person OPD Consultation", "Specialist Follow-up", "Diagnostic Blood / Imaging Test", "Annual Health Checkup Package", "Dental Procedure"});
        medAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spAttrMedicalType.setAdapter(medAdapter);
    }

    private void loadMetadata() {
        // 1. Categories
        apiService.getCategories().enqueue(new Callback<List<ApiModels.CategoryModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.CategoryModel>> call, Response<List<ApiModels.CategoryModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    categoriesList.clear();
                    categoriesList.addAll(response.body());

                    ArrayAdapter<ApiModels.CategoryModel> catAdapter = new ArrayAdapter<>(
                            PulsePostActivity.this,
                            android.R.layout.simple_spinner_item,
                            categoriesList
                    );
                    catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spCategory.setAdapter(catAdapter);

                    setupCategorySelectionListener();
                }
            }

            @Override
            public void onFailure(Call<List<ApiModels.CategoryModel>> call, Throwable t) {
                Toast.makeText(PulsePostActivity.this, "Failed to load categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // 2. Bengaluru Localities
        apiService.getLocalities().enqueue(new Callback<List<ApiModels.LocalityModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.LocalityModel>> call, Response<List<ApiModels.LocalityModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    localitiesList.clear();
                    localitiesList.addAll(response.body());

                    ArrayAdapter<ApiModels.LocalityModel> locAdapter = new ArrayAdapter<>(
                            PulsePostActivity.this,
                            android.R.layout.simple_spinner_item,
                            localitiesList
                    );
                    locAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spLocality.setAdapter(locAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<ApiModels.LocalityModel>> call, Throwable t) {}
        });

        // 3. Price Types cache
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

    private void setupCategorySelectionListener() {
        spCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ApiModels.CategoryModel selectedCat = categoriesList.get(position);
                updateSubcategories(selectedCat);
                updatePriceTypesForCategory(selectedCat.id);
                updateCategoryAttributeVisibility(selectedCat.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Trigger initial category
        if (!categoriesList.isEmpty()) {
            ApiModels.CategoryModel initial = categoriesList.get(0);
            updateSubcategories(initial);
            updatePriceTypesForCategory(initial.id);
            updateCategoryAttributeVisibility(initial.id);
        }
    }

    private void updateSubcategories(ApiModels.CategoryModel category) {
        if (category.subcategories != null && !category.subcategories.isEmpty()) {
            ArrayAdapter<ApiModels.SubcategoryModel> subAdapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    category.subcategories
            );
            subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spSubcategory.setAdapter(subAdapter);
        } else {
            apiService.getSubcategories(category.id).enqueue(new Callback<List<ApiModels.SubcategoryModel>>() {
                @Override
                public void onResponse(Call<List<ApiModels.SubcategoryModel>> call, Response<List<ApiModels.SubcategoryModel>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        category.subcategories = response.body();
                        ArrayAdapter<ApiModels.SubcategoryModel> subAdapter = new ArrayAdapter<>(
                                PulsePostActivity.this,
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
    }

    private void updatePriceTypesForCategory(long categoryId) {
        List<ApiModels.PriceTypeModel> filtered = new ArrayList<>();
        for (ApiModels.PriceTypeModel pt : priceTypesList) {
            if (pt.categoryId == categoryId) {
                filtered.add(pt);
            }
        }

        if (!filtered.isEmpty()) {
            ArrayAdapter<ApiModels.PriceTypeModel> ptAdapter = new ArrayAdapter<>(
                    this,
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
                                PulsePostActivity.this,
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

    private void updateCategoryAttributeVisibility(long categoryId) {
        layoutAttrRealEstate.setVisibility(categoryId == 6L ? View.VISIBLE : View.GONE);
        layoutAttrElectronics.setVisibility(categoryId == 2L ? View.VISIBLE : View.GONE);
        layoutAttrAutomotive.setVisibility(categoryId == 3L ? View.VISIBLE : View.GONE);
        layoutAttrHealthcare.setVisibility(categoryId == 4L ? View.VISIBLE : View.GONE);
        layoutAttrGeneric.setVisibility((categoryId != 6L && categoryId != 2L && categoryId != 3L && categoryId != 4L) ? View.VISIBLE : View.GONE);
    }

    private void requestGpsLocation() {
        boolean fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (fine || coarse) {
            fetchDeviceLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void fetchDeviceLocation() {
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                Location lastKnown = null;
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if (lastKnown == null && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

                if (lastKnown != null) {
                    detectedLatitude = lastKnown.getLatitude();
                    detectedLongitude = lastKnown.getLongitude();
                    txtGpsStatus.setText(String.format(Locale.US, "GPS: %.4f, %.4f (Captured)", detectedLatitude, detectedLongitude));
                    Toast.makeText(this, "Coordinates detected from device GPS", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (SecurityException ignored) {}

        // Fallback: use selected locality coordinates
        ApiModels.LocalityModel selectedLoc = (ApiModels.LocalityModel) spLocality.getSelectedItem();
        if (selectedLoc != null) {
            detectedLatitude = selectedLoc.latitude;
            detectedLongitude = selectedLoc.longitude;
            txtGpsStatus.setText(String.format(Locale.US, "Locality Geo: %.4f, %.4f", detectedLatitude, detectedLongitude));
        } else {
            txtGpsStatus.setText("GPS unavailable (manual locality used)");
        }
    }

    private void validateAndSubmitPulsePost() {
        String title = etTitle.getText().toString().trim();
        String provider = etProvider.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String addressLine = etAddressLine.getText().toString().trim();
        String evidenceRef = etEvidenceRef.getText().toString().trim();

        if (title.length() < 3) {
            etTitle.setError("Title must be at least 3 characters");
            etTitle.requestFocus();
            return;
        }

        if (provider.length() < 2) {
            etProvider.setError("Provider / brand name is required");
            etProvider.requestFocus();
            return;
        }

        if (priceStr.isEmpty()) {
            etPrice.setError("Please enter the offering price");
            etPrice.requestFocus();
            return;
        }

        double priceVal;
        try {
            priceVal = Double.parseDouble(priceStr);
            if (priceVal <= 0) {
                etPrice.setError("Price must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price amount");
            return;
        }

        ApiModels.CategoryModel selectedCat = (ApiModels.CategoryModel) spCategory.getSelectedItem();
        ApiModels.SubcategoryModel selectedSub = (ApiModels.SubcategoryModel) spSubcategory.getSelectedItem();
        ApiModels.PriceTypeModel selectedPt = (ApiModels.PriceTypeModel) spPriceType.getSelectedItem();
        ApiModels.LocalityModel selectedLoc = (ApiModels.LocalityModel) spLocality.getSelectedItem();

        if (selectedCat == null || selectedSub == null || selectedPt == null || selectedLoc == null) {
            Toast.makeText(this, "Please select Category, Subcategory, Price Type, and Locality", Toast.LENGTH_SHORT).show();
            return;
        }

        // Collect category-specific attributes dictionary
        Map<String, Object> attributes = new HashMap<>();
        long catId = selectedCat.id;
        if (catId == 6L) { // Real Estate
            attributes.put("bhk_type", spAttrBhk.getSelectedItem().toString());
            attributes.put("furnishing", spAttrFurnishing.getSelectedItem().toString());
            String sqft = etAttrSqft.getText().toString().trim();
            if (!sqft.isEmpty()) attributes.put("built_up_sqft", sqft);
        } else if (catId == 2L) { // Electronics
            attributes.put("storage", spAttrStorage.getSelectedItem().toString());
            attributes.put("condition", spAttrCondition.getSelectedItem().toString());
        } else if (catId == 3L) { // Automotive
            attributes.put("fuel_type", spAttrFuel.getSelectedItem().toString());
            attributes.put("transmission", spAttrTransmission.getSelectedItem().toString());
        } else if (catId == 4L) { // Healthcare
            attributes.put("consultation_type", spAttrMedicalType.getSelectedItem().toString());
        } else {
            String gen = etAttrGeneric.getText().toString().trim();
            if (!gen.isEmpty()) attributes.put("key_spec", gen);
        }

        if (detectedLatitude != null && detectedLongitude != null) {
            attributes.put("latitude", detectedLatitude);
            attributes.put("longitude", detectedLongitude);
        }

        setLoading(true);

        ApiModels.CreateListingRequest request = new ApiModels.CreateListingRequest(
                selectedLoc.id,
                selectedCat.id,
                selectedSub.id,
                title,
                provider,
                priceVal,
                selectedPt.id,
                addressLine.isEmpty() ? null : addressLine,
                evidenceRef.isEmpty() ? null : evidenceRef,
                notes.isEmpty() ? null : notes,
                attributes
        );

        apiService.createListing(request).enqueue(new Callback<ApiModels.ListingModel>() {
            @Override
            public void onResponse(Call<ApiModels.ListingModel> call, Response<ApiModels.ListingModel> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiModels.ListingModel created = response.body();
                    showSuccessDialog(created);
                } else {
                    String errCode = String.valueOf(response.code());
                    Toast.makeText(PulsePostActivity.this, "Submission rejected by server: " + errCode, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiModels.ListingModel> call, Throwable t) {
                setLoading(false);
                Toast.makeText(PulsePostActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(ApiModels.ListingModel listing) {
        new AlertDialog.Builder(this)
                .setTitle("🎉 PulsePost Published!")
                .setMessage("Your local intelligence has been recorded successfully.\n\n" +
                        "Offering: " + listing.customTitle + "\n" +
                        "Category: " + (listing.categoryName != null ? listing.categoryName : "Verified") + "\n" +
                        "Price: ₹" + String.format(Locale.US, "%,.0f", listing.priceAmount) + "\n" +
                        "Status: Pending Moderation\n\n" +
                        "You can monitor the approval status anytime in My PulsePosts.")
                .setPositiveButton("View in My PulsePosts", (dialog, which) -> {
                    openMyPulsePosts();
                    finish();
                })
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void openMyPulsePosts() {
        Intent intent = new Intent(this, ManageListingsActivity.class);
        intent.putExtra("initial_filter", "MY_LISTINGS");
        startActivity(intent);
    }

    private void setLoading(boolean isLoading) {
        if (progressSubmit != null) {
            progressSubmit.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!isLoading);
            btnSubmit.setText(isLoading ? "" : "Publish PulsePost");
        }
    }
}

