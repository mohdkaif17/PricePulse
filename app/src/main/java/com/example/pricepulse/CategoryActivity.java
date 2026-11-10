package com.example.pricepulse;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.adapters.CategoryProductAdapter;
import com.example.pricepulse.data.remote.ApiClient;
import com.example.pricepulse.data.remote.ApiModels;
import com.example.pricepulse.data.remote.PricePulseApiService;
import com.example.pricepulse.models.CategoryProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_CATEGORY_KEY = "extra_category_key";

    // Standard Category Keys matching backend taxonomy
    public static final String CATEGORY_HOTELS = "Hotels & Accommodation";
    public static final String CATEGORY_ELECTRONICS = "Electronics & Appliances";
    public static final String CATEGORY_VEHICLES = "Automotive";
    public static final String CATEGORY_HEALTHCARE = "Healthcare";
    public static final String CATEGORY_EDUCATION = "Education";
    public static final String CATEGORY_REAL_ESTATE = "Real Estate & Rentals";
    public static final String CATEGORY_RESTAURANTS = "Restaurants & Food";
    public static final String CATEGORY_HOME_SERVICES = "Home Services";
    public static final String CATEGORY_FITNESS = "Fitness & Wellness";
    public static final String CATEGORY_SHOPPING = "Shopping & Daily Essentials";

    public static class CategoryDef {
        public final long id;
        public final String title;
        public final String slug;
        public final String description;
        public final int iconRes;
        public final int badgeBgColor;

        public CategoryDef(long id, String title, String slug, String description, int iconRes, int badgeBgColor) {
            this.id = id;
            this.title = title;
            this.slug = slug;
            this.description = description;
            this.iconRes = iconRes;
            this.badgeBgColor = badgeBgColor;
        }
    }

    private static final Map<Long, CategoryDef> CATEGORY_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put(1L, new CategoryDef(1L, "Hotels & Accommodation", "hotels-accommodation", "Compare hotel tariffs & accommodation rates", R.drawable.ic_category_hotels, Color.parseColor("#FFFBEB")));
        CATEGORY_MAP.put(2L, new CategoryDef(2L, "Electronics & Appliances", "electronics-appliances", "Compare local electronics & gadgets prices", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF")));
        CATEGORY_MAP.put(3L, new CategoryDef(3L, "Automotive", "automotive", "Compare cars, two-wheelers & service prices", R.drawable.ic_category_vehicles, Color.parseColor("#FFF7ED")));
        CATEGORY_MAP.put(4L, new CategoryDef(4L, "Healthcare", "healthcare", "Compare medical consultations & test rates", R.drawable.ic_category_healthcare, Color.parseColor("#FEF2F2")));
        CATEGORY_MAP.put(5L, new CategoryDef(5L, "Education", "education", "Compare school fees, courses & coaching rates", R.drawable.ic_category_education, Color.parseColor("#F0FDFA")));
        CATEGORY_MAP.put(6L, new CategoryDef(6L, "Real Estate & Rentals", "real-estate-rentals", "Compare local rent & real estate prices", R.drawable.ic_category_real_estate, Color.parseColor("#F0FDF4")));
        CATEGORY_MAP.put(7L, new CategoryDef(7L, "Restaurants & Food", "restaurants-food", "Compare local restaurant & dining prices", R.drawable.ic_category_restaurants, Color.parseColor("#FAF5FF")));
        CATEGORY_MAP.put(8L, new CategoryDef(8L, "Home Services", "home-services", "Compare AC servicing, plumbing & home repairs", R.drawable.ic_category_home_services, Color.parseColor("#F0F9FF")));
        CATEGORY_MAP.put(9L, new CategoryDef(9L, "Fitness & Wellness", "fitness-wellness", "Compare gym memberships & wellness packages", R.drawable.ic_category_fitness, Color.parseColor("#FEF2F2")));
        CATEGORY_MAP.put(10L, new CategoryDef(10L, "Shopping & Daily Essentials", "shopping-daily-essentials", "Compare daily groceries & retail essentials", R.drawable.ic_category_shopping, Color.parseColor("#ECFDF5")));
    }

    private CategoryDef currentCategoryDef;
    private Long selectedSubcategoryId = null; // null represents "All"
    private String currentSearchQuery = "";
    private PricePulseApiService apiService;

    private RecyclerView rvProducts;
    private LinearLayout layoutSubcategoryChips;
    private LinearLayout layoutEmptyState;
    private TextView txtEmptyTitle;
    private TextView txtEmptyMessage;
    private TextView txtListingsCountSub;
    private ProgressBar progressCategory;
    private EditText etSearch;
    private ImageView btnClearSearch;

    private final List<TextView> chipViews = new ArrayList<>();
    private final Handler searchDebounceHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);

        View rootView = findViewById(R.id.categoryRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        apiService = ApiClient.getApiService(this);

        initViews();
        resolveCategory();
        setupCategoryHeader();
        setupSearch();
        loadSubcategories();
        fetchListings();
    }

    private void initViews() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvProducts = findViewById(R.id.rvCategoryProducts);
        if (rvProducts != null) {
            rvProducts.setLayoutManager(new LinearLayoutManager(this));
        }

        layoutSubcategoryChips = findViewById(R.id.layoutSubcategoryChips);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        txtEmptyTitle = findViewById(R.id.txtEmptyTitle);
        txtEmptyMessage = findViewById(R.id.txtEmptyMessage);
        txtListingsCountSub = findViewById(R.id.txtListingsCountSub);
        progressCategory = findViewById(R.id.progressCategory);
        etSearch = findViewById(R.id.etCategorySearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
    }

    /**
     * Accurately resolves the CategoryDef from Intent by ID or by text slug/name.
     */
    private void resolveCategory() {
        long explicitId = getIntent().getLongExtra(EXTRA_CATEGORY_ID, -1L);
        if (CATEGORY_MAP.containsKey(explicitId)) {
            currentCategoryDef = CATEGORY_MAP.get(explicitId);
            return;
        }

        String rawKey = getIntent().getStringExtra("category");
        if (rawKey == null) {
            rawKey = getIntent().getStringExtra(EXTRA_CATEGORY_KEY);
        }
        if (rawKey == null) {
            rawKey = "electronics";
        }

        String norm = rawKey.trim().toLowerCase(Locale.US);

        if (norm.contains("hotel") || norm.contains("accommodation") || norm.contains("resort") || norm.contains("hostel")) {
            currentCategoryDef = CATEGORY_MAP.get(1L);
        } else if (norm.contains("electronic") || norm.contains("gadget") || norm.contains("appliance") || norm.contains("phone") || norm.contains("laptop") || norm.contains("tv")) {
            currentCategoryDef = CATEGORY_MAP.get(2L);
        } else if (norm.contains("vehicle") || norm.contains("automotive") || norm.contains("car") || norm.contains("bike") || norm.contains("auto") || norm.contains("motor")) {
            // Category 3 is strictly Automotive (Cars, Two-Wheelers, Vehicle Servicing)
            currentCategoryDef = CATEGORY_MAP.get(3L);
        } else if (norm.contains("health") || norm.contains("medic") || norm.contains("doctor") || norm.contains("clinic") || norm.contains("diagnostic")) {
            currentCategoryDef = CATEGORY_MAP.get(4L);
        } else if (norm.contains("edu") || norm.contains("school") || norm.contains("course") || norm.contains("tuition") || norm.contains("coaching")) {
            currentCategoryDef = CATEGORY_MAP.get(5L);
        } else if (norm.contains("real") || norm.contains("rent") || norm.contains("flat") || norm.contains("house") || norm.contains("apartment") || norm.contains("pg") || norm.contains("property")) {
            // Category 6 is strictly Real Estate & Rentals (Houses for Rent, PG Accommodation, Apartments for Sale)
            currentCategoryDef = CATEGORY_MAP.get(6L);
        } else if (norm.contains("restaurant") || norm.contains("food") || norm.contains("dining") || norm.contains("buffet") || norm.contains("cafe") || norm.contains("meal")) {
            currentCategoryDef = CATEGORY_MAP.get(7L);
        } else if (norm.contains("home") || norm.contains("plumb") || norm.contains("ac service") || norm.contains("repair")) {
            currentCategoryDef = CATEGORY_MAP.get(8L);
        } else if (norm.contains("fitness") || norm.contains("gym") || norm.contains("wellness") || norm.contains("workout")) {
            currentCategoryDef = CATEGORY_MAP.get(9L);
        } else if (norm.contains("shop") || norm.contains("grocer") || norm.contains("daily") || norm.contains("essential") || norm.contains("supermarket")) {
            currentCategoryDef = CATEGORY_MAP.get(10L);
        } else {
            currentCategoryDef = CATEGORY_MAP.get(2L); // Default fallback to Electronics
        }
    }

    private void setupCategoryHeader() {
        if (currentCategoryDef == null) return;

        TextView txtTitle = findViewById(R.id.txtCategoryTitle);
        TextView txtDesc = findViewById(R.id.txtCategoryDescription);
        ImageView imgBadge = findViewById(R.id.imgCategoryBadge);

        if (txtTitle != null) {
            txtTitle.setText(currentCategoryDef.title);
        }
        if (txtDesc != null) {
            txtDesc.setText(currentCategoryDef.description);
        }
        if (imgBadge != null) {
            imgBadge.setImageResource(currentCategoryDef.iconRes);
            imgBadge.setBackgroundTintList(ColorStateList.valueOf(currentCategoryDef.badgeBgColor));
        }
    }

    private void setupSearch() {
        if (etSearch == null) return;

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                etSearch.setText("");
                currentSearchQuery = "";
                btnClearSearch.setVisibility(View.GONE);
                fetchListings();
            });
        }

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s != null ? s.toString().trim() : "";
                if (btnClearSearch != null) {
                    btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                }

                if (searchRunnable != null) {
                    searchDebounceHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> {
                    currentSearchQuery = query;
                    fetchListings();
                };
                searchDebounceHandler.postDelayed(searchRunnable, 350);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Dynamically loads subcategories from PostgreSQL backend for the selected category
     * and constructs filter chips.
     */
    private void loadSubcategories() {
        if (layoutSubcategoryChips == null || currentCategoryDef == null) return;

        layoutSubcategoryChips.removeAllViews();
        chipViews.clear();

        // Add "All" chip first
        TextView allChip = createFilterChip("All", null);
        layoutSubcategoryChips.addView(allChip);
        chipViews.add(allChip);
        updateChipSelection(allChip);

        apiService.getSubcategories(currentCategoryDef.id).enqueue(new Callback<List<ApiModels.SubcategoryModel>>() {
            @Override
            public void onResponse(Call<List<ApiModels.SubcategoryModel>> call, Response<List<ApiModels.SubcategoryModel>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    for (ApiModels.SubcategoryModel sub : response.body()) {
                        TextView chip = createFilterChip(sub.name, sub.id);
                        layoutSubcategoryChips.addView(chip);
                        chipViews.add(chip);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ApiModels.SubcategoryModel>> call, Throwable t) {
                // If offline or network error, keep "All" chip
            }
        });
    }

    private TextView createFilterChip(String text, Long subcategoryId) {
        TextView chip = new TextView(this);
        chip.setText(text);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        chip.setPadding(dpToPx(14), dpToPx(6), dpToPx(14), dpToPx(6));
        chip.setGravity(Gravity.CENTER);
        chip.setTag(subcategoryId);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(params);

        chip.setBackgroundResource(R.drawable.bg_chip_unselected);
        chip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

        chip.setOnClickListener(v -> {
            selectedSubcategoryId = (Long) v.getTag();
            updateChipSelection((TextView) v);
            fetchListings();
        });

        return chip;
    }

    private void updateChipSelection(TextView selectedView) {
        for (TextView chip : chipViews) {
            boolean isSelected = (chip == selectedView);
            chip.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
            chip.setTextColor(isSelected ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    /**
     * Fetches listings filtered by category ID and optional subcategory ID / search term.
     */
    private void fetchListings() {
        if (currentCategoryDef == null) return;

        if (progressCategory != null) {
            progressCategory.setVisibility(View.VISIBLE);
        }

        String search = (currentSearchQuery != null && !currentSearchQuery.isEmpty()) ? currentSearchQuery : null;

        apiService.getListings(null, currentCategoryDef.id, selectedSubcategoryId, search, "newest", 50, 0)
                .enqueue(new Callback<List<ApiModels.ListingModel>>() {
                    @Override
                    public void onResponse(Call<List<ApiModels.ListingModel>> call, Response<List<ApiModels.ListingModel>> response) {
                        if (progressCategory != null) {
                            progressCategory.setVisibility(View.GONE);
                        }

                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            displayListings(response.body());
                        } else {
                            displayEmptyState();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<ApiModels.ListingModel>> call, Throwable t) {
                        if (progressCategory != null) {
                            progressCategory.setVisibility(View.GONE);
                        }
                        displayEmptyState();
                    }
                });
    }

    private void displayListings(List<ApiModels.ListingModel> listings) {
        if (rvProducts == null) return;

        List<CategoryProduct> productList = new ArrayList<>();
        for (ApiModels.ListingModel l : listings) {
            String title = (l.customTitle != null && !l.customTitle.trim().isEmpty())
                    ? l.customTitle : currentCategoryDef.title + " #" + l.id;

            String subcat = (l.subcategoryName != null && !l.subcategoryName.trim().isEmpty())
                    ? l.subcategoryName : currentCategoryDef.title;

            String priceStr = (l.priceAmount != null)
                    ? "₹" + String.format(Locale.US, "%,.0f", l.priceAmount)
                    : "Price on request";

            String status = ("VERIFIED_REAL".equalsIgnoreCase(l.sourceType) || "approved".equalsIgnoreCase(l.moderationStatus))
                    ? "Verified Real"
                    : (l.status != null ? l.status.toUpperCase(Locale.US) : "Active");

            String provider = (l.providerName != null ? l.providerName : "Bengaluru Verified");
            if (l.areaName != null && !l.areaName.isEmpty()) {
                provider = provider + " • " + l.areaName;
            }

            productList.add(new CategoryProduct(
                    title,
                    currentCategoryDef.title + " • " + subcat,
                    priceStr,
                    status,
                    provider,
                    currentCategoryDef.iconRes,
                    currentCategoryDef.badgeBgColor,
                    true
            ));
        }

        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.GONE);
        rvProducts.setVisibility(View.VISIBLE);

        if (txtListingsCountSub != null) {
            txtListingsCountSub.setText(String.format(Locale.US, "%d verified offerings in Bengaluru", productList.size()));
        }

        CategoryProductAdapter adapter = new CategoryProductAdapter(this, productList, product -> {
            Intent intent = new Intent(CategoryActivity.this, CompareActivity.class);
            intent.putExtra(CompareActivity.EXTRA_PRODUCT_NAME, product.getName());
            intent.putExtra(CompareActivity.EXTRA_CATEGORY_NAME, currentCategoryDef.title);
            intent.putExtra(CompareActivity.EXTRA_CATEGORY_KEY, currentCategoryDef.title);
            startActivity(intent);
        });
        rvProducts.setAdapter(adapter);
    }

    private void displayEmptyState() {
        if (rvProducts != null) rvProducts.setVisibility(View.GONE);
        if (layoutEmptyState != null) layoutEmptyState.setVisibility(View.VISIBLE);

        if (txtEmptyTitle != null) {
            txtEmptyTitle.setText("No " + currentCategoryDef.title + " Listings");
        }
        if (txtEmptyMessage != null) {
            txtEmptyMessage.setText("No offerings found for the current filter. Try selecting another subcategory or clearing the search.");
        }
        if (txtListingsCountSub != null) {
            txtListingsCountSub.setText("0 offerings found");
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                getResources().getDisplayMetrics()
        );
    }
}
