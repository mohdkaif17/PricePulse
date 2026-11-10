package com.example.pricepulse;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pricepulse.utils.PriceStatusHelper;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MapActivity extends AppCompatActivity {

    public static final String EXTRA_INITIAL_SHOP_NAME = "extra_initial_shop_name";
    public static final String EXTRA_CATEGORY_KEY = "extra_category_key";

    private enum FilterMode {
        NEAREST,
        LOWEST_PRICE,
        BEST_DEAL,
        FAIR_PRICE
    }

    private FilterMode currentFilter = FilterMode.NEAREST;
    private InteractiveMapView mapView;
    private LinearLayout layoutNearbyShopsContainer;
    private EditText edtSearchShops;

    private TextView chipFilterNearest;
    private TextView chipFilterLowestPrice;
    private TextView chipFilterBestDeal;
    private TextView chipFilterFairPrice;

    private TextView chipCatAll;
    private TextView chipCatElectronics;
    private TextView chipCatRealEstate;
    private TextView chipCatVehicles;
    private TextView chipCatHealthcare;
    private TextView chipCatRestaurants;
    private TextView chipCatEducation;

    private TextView txtPreviewShopName;
    private TextView txtPreviewDistance;
    private TextView txtPreviewProductName;
    private TextView txtPreviewPrice;
    private TextView txtPreviewStatusBadge;
    private MaterialButton btnPreviewViewShop;

    private InteractiveMapView.ShopMarker currentSelectedShop;
    private String searchQuery = "";
    private String selectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        View root = findViewById(R.id.mapRoot);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        setupViews();
        handleInitialIntent();
    }

    private void setupViews() {
        // Back Navigation
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // "Use My Location" Action
        View btnUseMyLocation = findViewById(R.id.btnUseMyLocation);
        if (btnUseMyLocation != null) {
            btnUseMyLocation.setOnClickListener(v ->
                    Toast.makeText(this, getString(R.string.toast_live_location_notice), Toast.LENGTH_SHORT).show());
        }

        mapView = findViewById(R.id.mapView);
        layoutNearbyShopsContainer = findViewById(R.id.layoutNearbyShopsContainer);
        edtSearchShops = findViewById(R.id.edtSearchShops);

        chipFilterNearest = findViewById(R.id.chipFilterNearest);
        chipFilterLowestPrice = findViewById(R.id.chipFilterLowestPrice);
        chipFilterBestDeal = findViewById(R.id.chipFilterBestDeal);
        chipFilterFairPrice = findViewById(R.id.chipFilterFairPrice);

        chipCatAll = findViewById(R.id.chipCatAll);
        chipCatElectronics = findViewById(R.id.chipCatElectronics);
        chipCatRealEstate = findViewById(R.id.chipCatRealEstate);
        chipCatVehicles = findViewById(R.id.chipCatVehicles);
        chipCatHealthcare = findViewById(R.id.chipCatHealthcare);
        chipCatRestaurants = findViewById(R.id.chipCatRestaurants);
        chipCatEducation = findViewById(R.id.chipCatEducation);

        txtPreviewShopName = findViewById(R.id.txtPreviewShopName);
        txtPreviewDistance = findViewById(R.id.txtPreviewDistance);
        txtPreviewProductName = findViewById(R.id.txtPreviewProductName);
        txtPreviewPrice = findViewById(R.id.txtPreviewPrice);
        txtPreviewStatusBadge = findViewById(R.id.txtPreviewStatusBadge);
        btnPreviewViewShop = findViewById(R.id.btnPreviewViewShop);

        // Sort Filter chip listeners
        if (chipFilterNearest != null) chipFilterNearest.setOnClickListener(v -> setFilterMode(FilterMode.NEAREST));
        if (chipFilterLowestPrice != null) chipFilterLowestPrice.setOnClickListener(v -> setFilterMode(FilterMode.LOWEST_PRICE));
        if (chipFilterBestDeal != null) chipFilterBestDeal.setOnClickListener(v -> setFilterMode(FilterMode.BEST_DEAL));
        if (chipFilterFairPrice != null) chipFilterFairPrice.setOnClickListener(v -> setFilterMode(FilterMode.FAIR_PRICE));

        // Category Filter chip listeners
        if (chipCatAll != null) chipCatAll.setOnClickListener(v -> setCategoryFilter("All"));
        if (chipCatElectronics != null) chipCatElectronics.setOnClickListener(v -> setCategoryFilter("Electronics"));
        if (chipCatRealEstate != null) chipCatRealEstate.setOnClickListener(v -> setCategoryFilter("Rent & Real Estate"));
        if (chipCatVehicles != null) chipCatVehicles.setOnClickListener(v -> setCategoryFilter("Vehicles"));
        if (chipCatHealthcare != null) chipCatHealthcare.setOnClickListener(v -> setCategoryFilter("Healthcare"));
        if (chipCatRestaurants != null) chipCatRestaurants.setOnClickListener(v -> setCategoryFilter("Restaurants"));
        if (chipCatEducation != null) chipCatEducation.setOnClickListener(v -> setCategoryFilter("Education"));

        // Map selection callback
        if (mapView != null) {
            mapView.setOnShopSelectedListener(this::updateShopPreview);
        }

        // Real-time search filter
        if (edtSearchShops != null) {
            edtSearchShops.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    searchQuery = (s != null) ? s.toString().trim().toLowerCase() : "";
                    renderShopsList();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void handleInitialIntent() {
        String initialCategory = getIntent().getStringExtra(EXTRA_CATEGORY_KEY);
        if (initialCategory != null && !initialCategory.trim().isEmpty()) {
            selectedCategory = initialCategory.trim();
            updateCategoryChipsUi();
        }

        String initialShopName = getIntent().getStringExtra(EXTRA_INITIAL_SHOP_NAME);
        if (initialShopName != null && !initialShopName.trim().isEmpty() && mapView != null) {
            mapView.selectMarkerByName(initialShopName);
        } else if (mapView != null) {
            List<InteractiveMapView.ShopMarker> markers = mapView.getMarkers();
            if (!markers.isEmpty()) {
                updateShopPreview(markers.get(0));
            }
        }

        renderShopsList();
    }

    private void setCategoryFilter(String category) {
        selectedCategory = category;
        updateCategoryChipsUi();
        renderShopsList();
    }

    private void updateCategoryChipsUi() {
        updateChipStyle(chipCatAll, "All".equalsIgnoreCase(selectedCategory));
        updateChipStyle(chipCatElectronics, "Electronics".equalsIgnoreCase(selectedCategory));
        updateChipStyle(chipCatRealEstate, "Rent & Real Estate".equalsIgnoreCase(selectedCategory) || "Real Estate".equalsIgnoreCase(selectedCategory));
        updateChipStyle(chipCatVehicles, "Vehicles".equalsIgnoreCase(selectedCategory));
        updateChipStyle(chipCatHealthcare, "Healthcare".equalsIgnoreCase(selectedCategory));
        updateChipStyle(chipCatRestaurants, "Restaurants".equalsIgnoreCase(selectedCategory));
        updateChipStyle(chipCatEducation, "Education".equalsIgnoreCase(selectedCategory));
    }

    private void updateChipStyle(TextView chip, boolean isSelected) {
        if (chip == null) return;
        chip.setBackgroundResource(isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chip.setTextColor(ContextCompat.getColor(this, isSelected ? R.color.white : R.color.text_secondary));
    }

    private void setFilterMode(FilterMode mode) {
        currentFilter = mode;
        updateSortChipsUi();
        renderShopsList();
    }

    private void updateSortChipsUi() {
        updateChipStyle(chipFilterNearest, currentFilter == FilterMode.NEAREST);
        updateChipStyle(chipFilterLowestPrice, currentFilter == FilterMode.LOWEST_PRICE);
        updateChipStyle(chipFilterBestDeal, currentFilter == FilterMode.BEST_DEAL);
        updateChipStyle(chipFilterFairPrice, currentFilter == FilterMode.FAIR_PRICE);
    }

    private void updateShopPreview(InteractiveMapView.ShopMarker shop) {
        if (shop == null) return;
        currentSelectedShop = shop;

        if (txtPreviewShopName != null) txtPreviewShopName.setText(shop.name);

        String categoryText = (shop.category != null && !shop.category.isEmpty()) ? shop.category : "Electronics";
        if (txtPreviewDistance != null) {
            txtPreviewDistance.setText(String.format("📍 %s • %s • %s", shop.distance, categoryText, shop.location));
        }
        if (txtPreviewProductName != null) txtPreviewProductName.setText(shop.productName);
        if (txtPreviewPrice != null) txtPreviewPrice.setText(shop.price);
        PriceStatusHelper.applyStatusBadge(txtPreviewStatusBadge, shop.statusBadge);

        if (btnPreviewViewShop != null) {
            btnPreviewViewShop.setOnClickListener(v -> openShop(shop.name, shop.location, shop.distance));
        }
    }

    private void renderShopsList() {
        if (mapView == null || layoutNearbyShopsContainer == null) return;

        List<InteractiveMapView.ShopMarker> filtered = new ArrayList<>();
        for (InteractiveMapView.ShopMarker m : mapView.getMarkers()) {
            boolean matchesCategory = "All".equalsIgnoreCase(selectedCategory) ||
                    (m.category != null && m.category.equalsIgnoreCase(selectedCategory)) ||
                    ("Rent & Real Estate".equalsIgnoreCase(selectedCategory) && "Real Estate".equalsIgnoreCase(m.category));

            boolean matchesQuery = searchQuery.isEmpty() ||
                    m.name.toLowerCase().contains(searchQuery) ||
                    m.location.toLowerCase().contains(searchQuery) ||
                    (m.category != null && m.category.toLowerCase().contains(searchQuery)) ||
                    m.productName.toLowerCase().contains(searchQuery);

            if (matchesCategory && matchesQuery) {
                filtered.add(m);
            }
        }

        if (currentFilter == FilterMode.NEAREST) {
            Collections.sort(filtered, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));
        } else if (currentFilter == FilterMode.LOWEST_PRICE) {
            Collections.sort(filtered, (a, b) -> Double.compare(a.priceValue, b.priceValue));
        } else if (currentFilter == FilterMode.BEST_DEAL) {
            Collections.sort(filtered, (a, b) -> Integer.compare(a.statusConcept, b.statusConcept));
        } else if (currentFilter == FilterMode.FAIR_PRICE) {
            Collections.sort(filtered, (a, b) -> {
                int scoreA = (a.statusConcept == 2) ? 0 : a.statusConcept;
                int scoreB = (b.statusConcept == 2) ? 0 : b.statusConcept;
                return Integer.compare(scoreA, scoreB);
            });
        }

        layoutNearbyShopsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (InteractiveMapView.ShopMarker shop : filtered) {
            View itemView = inflater.inflate(R.layout.item_shop_comparison, layoutNearbyShopsContainer, false);

            TextView txtName = itemView.findViewById(R.id.txtShopName);
            TextView txtLocation = itemView.findViewById(R.id.txtShopLocation);
            TextView txtDistance = itemView.findViewById(R.id.txtShopDistance);
            TextView txtPrice = itemView.findViewById(R.id.txtShopPrice);
            TextView txtStatusBadge = itemView.findViewById(R.id.txtShopStatusBadge);

            if (txtName != null) txtName.setText(shop.name);
            if (txtLocation != null) txtLocation.setText(shop.location);
            if (txtDistance != null) txtDistance.setText(shop.distance);
            if (txtPrice != null) txtPrice.setText(shop.price);
            PriceStatusHelper.applyStatusBadge(txtStatusBadge, shop.statusBadge);

            itemView.setOnClickListener(v -> {
                mapView.selectMarker(shop.id);
                updateShopPreview(shop);
            });

            layoutNearbyShopsContainer.addView(itemView);
        }
    }

    private void openShop(String shopName, String location, String distance) {
        Intent intent = new Intent(this, ShopActivity.class);
        intent.putExtra(ShopActivity.EXTRA_SHOP_NAME, shopName);
        intent.putExtra(ShopActivity.EXTRA_SHOP_LOCATION, location);
        intent.putExtra(ShopActivity.EXTRA_SHOP_DISTANCE, distance);
        startActivity(intent);
    }
}
