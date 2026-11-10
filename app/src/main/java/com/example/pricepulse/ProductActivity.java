package com.example.pricepulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pricepulse.utils.PriceStatusHelper;

public class ProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_PRODUCT_PRICE = "extra_product_price";
    public static final String EXTRA_SHOP_NAME = "extra_shop_name";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_SHOP_DISTANCE = "extra_shop_distance";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "extra_product_description";
    public static final String EXTRA_PRICE_STATUS = "extra_price_status";
    public static final String EXTRA_IS_FAIR_PRICE = "extra_is_fair_price";
    public static final String EXTRA_CATEGORY_KEY = "extra_category_key";

    private String currentProductName = "Samsung Galaxy S24";
    private String currentCategoryName = "Electronics";
    private String currentCategoryKey = CategoryActivity.CATEGORY_ELECTRONICS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product);

        View rootView = findViewById(R.id.productRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        loadProductDetails();
        setupNavigationAndActions();
    }

    private void setupNavigationAndActions() {
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Bookmark / Save Top Action
        View btnSaveTop = findViewById(R.id.btnSaveTop);
        if (btnSaveTop != null) {
            btnSaveTop.setOnClickListener(v -> Toast.makeText(this, getString(R.string.toast_product_saved), Toast.LENGTH_SHORT).show());
        }

        // Compare Prices Action
        View btnComparePrices = findViewById(R.id.btnComparePrices);
        if (btnComparePrices != null) {
            btnComparePrices.setOnClickListener(v -> {
                Intent intent = new Intent(ProductActivity.this, CompareActivity.class);
                intent.putExtra(CompareActivity.EXTRA_PRODUCT_NAME, currentProductName);
                intent.putExtra(CompareActivity.EXTRA_CATEGORY_NAME, currentCategoryName);
                intent.putExtra(CompareActivity.EXTRA_CATEGORY_KEY, currentCategoryKey);
                startActivity(intent);
            });
        }

        // Price History Action
        View btnViewPriceHistory = findViewById(R.id.btnViewPriceHistory);
        if (btnViewPriceHistory != null) {
            btnViewPriceHistory.setOnClickListener(v -> {
                Intent intent = new Intent(ProductActivity.this, PriceHistoryActivity.class);
                intent.putExtra(PriceHistoryActivity.EXTRA_PRODUCT_NAME, currentProductName);
                intent.putExtra(PriceHistoryActivity.EXTRA_CATEGORY_NAME, currentCategoryName);
                intent.putExtra(PriceHistoryActivity.EXTRA_CATEGORY_KEY, currentCategoryKey);
                startActivity(intent);
            });
        }
    }

    private void loadProductDetails() {
        // 1. Retrieve Extras with Safe Fallbacks
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        if (productName == null) productName = getIntent().getStringExtra("productName");
        if (productName == null || productName.trim().isEmpty()) {
            productName = "Samsung Galaxy S24";
        }
        currentProductName = productName;

        String productPrice = getIntent().getStringExtra(EXTRA_PRODUCT_PRICE);
        if (productPrice == null) productPrice = getIntent().getStringExtra("price");
        if (productPrice == null || productPrice.trim().isEmpty()) {
            productPrice = "₹74,999";
        }

        String shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        if (shopName == null) shopName = getIntent().getStringExtra("shopName");
        if (shopName == null || shopName.trim().isEmpty()) {
            shopName = "Apex Digital Hub";
        }

        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        if (categoryName == null) categoryName = getIntent().getStringExtra("category");
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = "Electronics";
        }
        currentCategoryName = categoryName;

        String categoryKey = getIntent().getStringExtra(EXTRA_CATEGORY_KEY);
        if (categoryKey == null || categoryKey.trim().isEmpty()) {
            categoryKey = categoryName;
        }
        currentCategoryKey = categoryKey;

        String distance = getIntent().getStringExtra(EXTRA_SHOP_DISTANCE);
        if (distance == null) distance = getIntent().getStringExtra("distance");
        if (distance == null || distance.trim().isEmpty()) {
            distance = "1.2 km away";
        }

        String priceStatus = getIntent().getStringExtra(EXTRA_PRICE_STATUS);
        if (priceStatus == null) priceStatus = getIntent().getStringExtra("priceStatus");
        if (priceStatus == null || priceStatus.trim().isEmpty()) {
            priceStatus = "Fair Price";
        }

        // 2. Bind View Elements
        TextView txtTopBarTitle = findViewById(R.id.txtTopBarTitle);
        ImageView imgProductLarge = findViewById(R.id.imgProductLarge);
        TextView txtProductCategoryBadge = findViewById(R.id.txtProductCategoryBadge);
        TextView txtProductName = findViewById(R.id.txtProductName);
        TextView txtProductPrice = findViewById(R.id.txtProductPrice);
        TextView txtProductMarketAvg = findViewById(R.id.txtProductMarketAvg);
        TextView txtProductShopName = findViewById(R.id.txtProductShopName);
        TextView txtProductShopDistance = findViewById(R.id.txtProductShopDistance);
        TextView txtProductShopLocation = findViewById(R.id.txtProductShopLocation);
        TextView txtProductPriceStatus = findViewById(R.id.txtProductPriceStatus);
        TextView txtProductPriceStatusDesc = findViewById(R.id.txtProductPriceStatusDesc);
        View layoutProductPriceStatus = findViewById(R.id.layoutProductPriceStatus);
        View dotProductPriceStatus = findViewById(R.id.dotProductPriceStatus);
        TextView txtProductDescription = findViewById(R.id.txtProductDescription);

        if (txtTopBarTitle != null) txtTopBarTitle.setText(productName);
        if (txtProductName != null) txtProductName.setText(productName);
        if (txtProductCategoryBadge != null) txtProductCategoryBadge.setText(categoryName);
        if (txtProductPrice != null) txtProductPrice.setText(productPrice);
        if (txtProductShopName != null) txtProductShopName.setText(shopName);
        if (txtProductShopDistance != null) txtProductShopDistance.setText(distance);
        if (txtProductShopLocation != null) txtProductShopLocation.setText("Bangalore");

        // Category Icon & Market Avg
        int iconRes = getCategoryIconRes(categoryName);
        if (imgProductLarge != null) {
            imgProductLarge.setImageResource(iconRes);
        }

        if (txtProductMarketAvg != null) {
            txtProductMarketAvg.setText(getMarketAverageText(productName));
        }

        // Price Status Badge formatting via PriceStatusHelper
        PriceStatusHelper.applyStatusPill(this, layoutProductPriceStatus, txtProductPriceStatus, null, dotProductPriceStatus, priceStatus);

        PriceStatusHelper.StatusType statusType = PriceStatusHelper.parseStatus(priceStatus);
        if (txtProductPriceStatusDesc != null) {
            switch (statusType) {
                case BEST_DEAL:
                    txtProductPriceStatusDesc.setText(getString(R.string.price_status_best_deal_desc));
                    break;
                case SLIGHTLY_EXPENSIVE:
                    txtProductPriceStatusDesc.setText(getString(R.string.price_status_slightly_expensive_desc));
                    break;
                case OVERPRICED:
                    txtProductPriceStatusDesc.setText(getString(R.string.price_status_overpriced_desc));
                    break;
                case FAIR_PRICE:
                default:
                    txtProductPriceStatusDesc.setText(getString(R.string.price_status_fair_desc));
                    break;
            }
        }

        // Product Description
        String description = getProductDescriptionText(productName);
        if (txtProductDescription != null) {
            txtProductDescription.setText(description);
        }
    }

    private int getCategoryIconRes(String category) {
        String normalized = category.toLowerCase();
        if (normalized.contains("hotel") || normalized.contains("accommodation") || normalized.contains("resort")) {
            return R.drawable.ic_category_hotels;
        } else if (normalized.contains("real") || normalized.contains("rent") || normalized.contains("property")) {
            return R.drawable.ic_category_real_estate;
        } else if (normalized.contains("vehicle") || normalized.contains("car") || normalized.contains("bike") || normalized.contains("auto")) {
            return R.drawable.ic_category_vehicles;
        } else if (normalized.contains("health") || normalized.contains("pharmacy") || normalized.contains("medic")) {
            return R.drawable.ic_category_healthcare;
        } else if (normalized.contains("restaurant") || normalized.contains("food") || normalized.contains("dining")) {
            return R.drawable.ic_category_restaurants;
        } else if (normalized.contains("education") || normalized.contains("learn") || normalized.contains("course") || normalized.contains("school")) {
            return R.drawable.ic_category_education;
        } else if (normalized.contains("home") || normalized.contains("repair") || normalized.contains("service")) {
            return R.drawable.ic_category_home_services;
        } else if (normalized.contains("fitness") || normalized.contains("gym") || normalized.contains("wellness")) {
            return R.drawable.ic_category_fitness;
        } else if (normalized.contains("shop") || normalized.contains("grocer") || normalized.contains("essential")) {
            return R.drawable.ic_category_shopping;
        } else {
            return R.drawable.ic_category_electronics;
        }
    }

    private String getMarketAverageText(String productName) {
        String name = productName.toLowerCase();
        if (name.contains("galaxy") || name.contains("s24")) {
            return "Market Avg: ₹79,990";
        } else if (name.contains("iphone 15")) {
            return "Market Avg: ₹71,499";
        } else if (name.contains("dell") || name.contains("laptop")) {
            return "Market Avg: ₹65,000";
        } else if (name.contains("sony") || name.contains("headphone")) {
            return "Market Avg: ₹9,500";
        } else if (name.contains("1 bhk")) {
            return "Market Avg: ₹15,000 /mo";
        } else if (name.contains("2 bhk")) {
            return "Market Avg: ₹25,500 /mo";
        } else if (name.contains("activa")) {
            return "Market Avg: ₹82,000";
        } else if (name.contains("java")) {
            return "Market Avg: ₹6,000";
        } else {
            return "Verified Market Rate";
        }
    }

    private String getProductDescriptionText(String productName) {
        String name = productName.toLowerCase();
        if (name.contains("samsung") || name.contains("galaxy") || name.contains("s24")) {
            return "Latest smartphone with premium display, camera and performance features.";
        } else if (name.contains("iphone")) {
            return "Next-generation smartphone with Dynamic Island, advanced 48MP main camera, and all-day battery life.";
        } else if (name.contains("dell") || name.contains("laptop")) {
            return "High-performance laptop featuring fast processor, crisp display, and reliable all-day productivity.";
        } else if (name.contains("sony") || name.contains("headphone")) {
            return "Premium wireless headphones with industry-leading active noise cancellation and high-resolution audio.";
        } else if (name.contains("tv") || name.contains("led")) {
            return "Ultra-clear 4K smart television with immersive sound, HDR clarity, and multiple connectivity ports.";
        } else if (name.contains("apartment") || name.contains("rent") || name.contains("bhk")) {
            return "Well-maintained residential living space with modern amenities, round-the-clock water, and nearby transit.";
        } else if (name.contains("health") || name.contains("pharmacy") || name.contains("kit")) {
            return "Certified healthcare diagnostic and wellness essentials verified for quality and standard pricing.";
        } else if (name.contains("course") || name.contains("java") || name.contains("python")) {
            return "Comprehensive professional training curriculum with hands-on projects and expert-led mentorship.";
        } else {
            return "Verified product with local shop pricing and fair market benchmark valuation.";
        }
    }
}
