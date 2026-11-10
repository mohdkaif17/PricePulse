package com.example.pricepulse;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pricepulse.adapters.ShopAdapter;
import com.example.pricepulse.adapters.ShopProductAdapter;
import com.example.pricepulse.models.CategoryProduct;
import com.example.pricepulse.models.Shop;

import java.util.ArrayList;
import java.util.List;

public class ShopActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_NAME = "extra_shop_name";
    public static final String EXTRA_SHOP_LOCATION = "extra_shop_location";
    public static final String EXTRA_SHOP_DISTANCE = "extra_shop_distance";
    public static final String EXTRA_SHOP_CATEGORY = "extra_shop_category";
    public static final String EXTRA_SHOP_DESCRIPTION = "extra_shop_description";
    public static final String EXTRA_SHOP_RATING = "extra_shop_rating";
    public static final String EXTRA_IS_DETAIL_MODE = "extra_is_detail_mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        View rootView = findViewById(R.id.shopRoot);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        String shopName = getIntent().getStringExtra(EXTRA_SHOP_NAME);
        if (shopName == null) {
            shopName = getIntent().getStringExtra("shopName");
        }

        boolean isDetailMode = getIntent().getBooleanExtra(EXTRA_IS_DETAIL_MODE, false)
                || (shopName != null && !shopName.trim().isEmpty());

        if (isDetailMode) {
            setupShopDetailView(shopName);
        } else {
            setupShopListView();
        }
    }

    /**
     * Mode 1: Displays the Nearby Shops List with RecyclerView and mock data
     */
    private void setupShopListView() {
        View layoutList = findViewById(R.id.layoutShopList);
        View layoutDetail = findViewById(R.id.layoutShopDetail);

        if (layoutList != null) layoutList.setVisibility(View.VISIBLE);
        if (layoutDetail != null) layoutDetail.setVisibility(View.GONE);

        View btnBack = findViewById(R.id.btnListBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        RecyclerView rvNearbyShops = findViewById(R.id.rvNearbyShops);
        if (rvNearbyShops != null) {
            rvNearbyShops.setLayoutManager(new LinearLayoutManager(this));

            List<Shop> mockShops = getMockShopsList();
            ShopAdapter adapter = new ShopAdapter(this, mockShops, shop -> {
                Intent intent = new Intent(ShopActivity.this, ShopActivity.class);
                intent.putExtra(EXTRA_SHOP_NAME, shop.getName());
                intent.putExtra(EXTRA_SHOP_CATEGORY, shop.getCategory());
                intent.putExtra(EXTRA_SHOP_DISTANCE, shop.getDistance());
                intent.putExtra(EXTRA_SHOP_DESCRIPTION, shop.getDescription());
                intent.putExtra(EXTRA_SHOP_RATING, shop.getRating());
                intent.putExtra(EXTRA_IS_DETAIL_MODE, true);
                startActivity(intent);
            });
            rvNearbyShops.setAdapter(adapter);
        }
    }

    /**
     * Mode 2: Displays the details for the selected shop
     */
    private void setupShopDetailView(String initialShopName) {
        View layoutList = findViewById(R.id.layoutShopList);
        View layoutDetail = findViewById(R.id.layoutShopDetail);

        if (layoutList != null) layoutList.setVisibility(View.GONE);
        if (layoutDetail != null) layoutDetail.setVisibility(View.VISIBLE);

        View btnBack = findViewById(R.id.btnDetailBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        String shopName = initialShopName;
        if (shopName == null || shopName.trim().isEmpty()) {
            shopName = "Apex Digital Hub";
        }

        String category = getIntent().getStringExtra(EXTRA_SHOP_CATEGORY);
        if (category == null) category = getIntent().getStringExtra("category");
        if (category == null || category.trim().isEmpty()) category = "Electronics";

        String distance = getIntent().getStringExtra(EXTRA_SHOP_DISTANCE);
        if (distance == null) distance = getIntent().getStringExtra("distance");
        if (distance == null || distance.trim().isEmpty()) distance = "1.2 km away";

        String description = getIntent().getStringExtra(EXTRA_SHOP_DESCRIPTION);
        if (description == null) description = getIntent().getStringExtra("description");
        if (description == null || description.trim().isEmpty()) {
            description = "Phones, laptops & gadgets";
        }

        String rating = getIntent().getStringExtra(EXTRA_SHOP_RATING);
        if (rating == null || rating.trim().isEmpty()) {
            rating = "4.8";
        }

        TextView txtTopBarTitle = findViewById(R.id.txtTopBarTitle);
        TextView txtShopNameHeader = findViewById(R.id.txtShopNameHeader);
        TextView txtShopCategoryHeader = findViewById(R.id.txtShopCategoryHeader);
        TextView txtShopDistanceHeader = findViewById(R.id.txtShopDistanceHeader);
        TextView txtShopRatingHeader = findViewById(R.id.txtShopRatingHeader);
        TextView txtAboutShopDescription = findViewById(R.id.txtAboutShopDescription);

        if (txtTopBarTitle != null) txtTopBarTitle.setText(shopName);
        if (txtShopNameHeader != null) txtShopNameHeader.setText(shopName);
        if (txtShopCategoryHeader != null) txtShopCategoryHeader.setText(category);
        if (txtShopDistanceHeader != null) txtShopDistanceHeader.setText(distance);
        if (txtShopRatingHeader != null) txtShopRatingHeader.setText(rating);
        if (txtAboutShopDescription != null) txtAboutShopDescription.setText(description);

        // Populate Available Products for this shop
        RecyclerView rvShopProducts = findViewById(R.id.rvShopProducts);
        if (rvShopProducts != null) {
            rvShopProducts.setLayoutManager(new LinearLayoutManager(this));
            List<CategoryProduct> products = getMockProductsForShop(shopName, category);
            final String currentShopName = shopName;
            final String currentCategory = category;
            final String currentDistance = distance;
            ShopProductAdapter productAdapter = new ShopProductAdapter(this, products, product -> {
                Intent productIntent = new Intent(ShopActivity.this, ProductActivity.class);
                productIntent.putExtra(ProductActivity.EXTRA_PRODUCT_NAME, product.getName());
                productIntent.putExtra(ProductActivity.EXTRA_PRODUCT_PRICE, product.getPrice());
                productIntent.putExtra(ProductActivity.EXTRA_SHOP_NAME, currentShopName);
                productIntent.putExtra(ProductActivity.EXTRA_CATEGORY_NAME, currentCategory);
                productIntent.putExtra(ProductActivity.EXTRA_SHOP_DISTANCE, currentDistance);
                productIntent.putExtra(ProductActivity.EXTRA_PRODUCT_DESCRIPTION, product.getCategory());
                productIntent.putExtra(ProductActivity.EXTRA_PRICE_STATUS, product.getPriceStatus());
                productIntent.putExtra(ProductActivity.EXTRA_IS_FAIR_PRICE, product.isFairPrice());
                startActivity(productIntent);
            });
            rvShopProducts.setAdapter(productAdapter);
        }

        // Action buttons
        View btnSaveTop = findViewById(R.id.btnSaveTop);
        if (btnSaveTop != null) {
            btnSaveTop.setOnClickListener(v -> Toast.makeText(this, getString(R.string.toast_shop_saved), Toast.LENGTH_SHORT).show());
        }
    }

    private List<CategoryProduct> getMockProductsForShop(String shopName, String category) {
        List<CategoryProduct> list = new ArrayList<>();
        String normalized = (shopName + " " + category).toLowerCase();

        if (normalized.contains("apex")) {
            // Apex Digital Hub exact mock items
            list.add(new CategoryProduct("Samsung Galaxy S24", "Electronics • Smartphones", "₹74,999", "Fair Price", "Market Avg: ₹79,990", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("iPhone 15", "Electronics • Smartphones", "₹68,999", "Fair Price", "Market Avg: ₹71,499", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Dell Inspiron Laptop", "Electronics • Computers", "₹62,999", "Fair Price", "Market Avg: ₹65,000", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Sony Headphones", "Electronics • Audio", "₹8,999", "Fair Price", "Market Avg: ₹9,500", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));

        } else if (normalized.contains("smart electronics")) {
            list.add(new CategoryProduct("Smart LED TV 55\"", "Electronics • Entertainment", "₹34,999", "Fair Price", "Market Avg: ₹38,000", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Microwave Oven", "Electronics • Home Appliances", "₹12,499", "Fair Price", "Market Avg: ₹13,500", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Bluetooth Speaker", "Electronics • Audio", "₹4,999", "Fair Price", "Market Avg: ₹5,500", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Refrigerator 260L", "Electronics • Appliances", "₹28,500", "Fair Price", "Market Avg: ₹30,000", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));

        } else if (normalized.contains("city mobiles")) {
            list.add(new CategoryProduct("OnePlus 12", "Electronics • Smartphones", "₹54,999", "Fair Price", "Market Avg: ₹58,000", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Xiaomi 14", "Electronics • Smartphones", "₹49,999", "Fair Price", "Market Avg: ₹52,000", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Fast Charger 65W", "Electronics • Accessories", "₹1,499", "Fair Price", "Market Avg: ₹1,800", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Tempered Glass & Cover", "Electronics • Protection", "₹299", "Fair Price", "Market Avg: ₹450", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));

        } else if (normalized.contains("rental") || normalized.contains("real estate")) {
            list.add(new CategoryProduct("1 BHK Apartment Monthly Rent", "Rent & Real Estate • Flats", "₹14,500 /mo", "Fair Price", "Market Avg: ₹15,000 /mo", R.drawable.ic_category_real_estate, Color.parseColor("#F0FDF4"), true));
            list.add(new CategoryProduct("2 BHK Apartment Monthly Rent", "Rent & Real Estate • Flats", "₹24,000 /mo", "Fair Price", "Market Avg: ₹25,500 /mo", R.drawable.ic_category_real_estate, Color.parseColor("#F0FDF4"), true));
            list.add(new CategoryProduct("PG Single Room", "Rent & Real Estate • Accommodation", "₹8,500 /mo", "Fair Price", "Market Avg: ₹9,000 /mo", R.drawable.ic_category_real_estate, Color.parseColor("#F0FDF4"), true));
            list.add(new CategoryProduct("Office Space", "Rent & Real Estate • Commercial", "₹45,000 /mo", "Slightly Expensive", "Market Avg: ₹40,000 /mo", R.drawable.ic_category_real_estate, Color.parseColor("#F0FDF4"), false));

        } else if (normalized.contains("pharmacy") || normalized.contains("health")) {
            list.add(new CategoryProduct("General Health Checkup Kit", "Healthcare • Preventive", "₹799", "Fair Price", "Market Avg: ₹950", R.drawable.ic_category_healthcare, Color.parseColor("#FEF2F2"), true));
            list.add(new CategoryProduct("Multi-Vitamin Pack", "Healthcare • Wellness", "₹650", "Fair Price", "Market Avg: ₹750", R.drawable.ic_category_healthcare, Color.parseColor("#FEF2F2"), true));
            list.add(new CategoryProduct("Digital BP Monitor", "Healthcare • Medical Devices", "₹1,850", "Fair Price", "Market Avg: ₹2,100", R.drawable.ic_category_healthcare, Color.parseColor("#FEF2F2"), true));
            list.add(new CategoryProduct("First Aid Kit", "Healthcare • Emergency Care", "₹499", "Fair Price", "Market Avg: ₹600", R.drawable.ic_category_healthcare, Color.parseColor("#FEF2F2"), true));

        } else if (normalized.contains("learning") || normalized.contains("education") || normalized.contains("campus")) {
            list.add(new CategoryProduct("Java Course", "Education • Programming", "₹4,999", "Fair Price", "Market Avg: ₹6,000", R.drawable.ic_category_education, Color.parseColor("#F0FDFA"), true));
            list.add(new CategoryProduct("Python Course", "Education • Programming", "₹4,499", "Fair Price", "Market Avg: ₹5,500", R.drawable.ic_category_education, Color.parseColor("#F0FDFA"), true));
            list.add(new CategoryProduct("Data Science Course", "Education • Professional", "₹14,999", "Fair Price", "Market Avg: ₹18,000", R.drawable.ic_category_education, Color.parseColor("#F0FDFA"), true));
            list.add(new CategoryProduct("Aptitude Training", "Education • Skill Building", "₹2,999", "Fair Price", "Market Avg: ₹3,500", R.drawable.ic_category_education, Color.parseColor("#F0FDFA"), true));

        } else {
            // Default mock products
            list.add(new CategoryProduct("Samsung Galaxy S24", "Electronics • Smartphones", "₹74,999", "Fair Price", "Market Avg: ₹79,990", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("iPhone 15", "Electronics • Smartphones", "₹68,999", "Fair Price", "Market Avg: ₹71,499", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Dell Inspiron Laptop", "Electronics • Computers", "₹62,999", "Fair Price", "Market Avg: ₹65,000", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
            list.add(new CategoryProduct("Sony Headphones", "Electronics • Audio", "₹8,999", "Fair Price", "Market Avg: ₹9,500", R.drawable.ic_category_electronics, Color.parseColor("#EFF6FF"), true));
        }

        return list;
    }

    private List<Shop> getMockShopsList() {
        List<Shop> list = new ArrayList<>();

        // 1. Apex Digital Hub
        list.add(new Shop(
                "Apex Digital Hub",
                "Electronics",
                "1.2 km away",
                "Phones, laptops & gadgets",
                "4.8",
                R.drawable.ic_category_electronics,
                Color.parseColor("#EFF6FF"),
                true,
                "18 active prices"
        ));

        // 2. Smart Electronics Store
        list.add(new Shop(
                "Smart Electronics Store",
                "Electronics",
                "2.0 km away",
                "Home appliances, TVs, smart gadgets & repair services",
                "4.6",
                R.drawable.ic_category_electronics,
                Color.parseColor("#EFF6FF"),
                true,
                "14 active prices"
        ));

        // 3. City Mobiles
        list.add(new Shop(
                "City Mobiles",
                "Electronics",
                "2.8 km away",
                "Multi-brand mobile phones, cases, chargers & fast repairs",
                "4.4",
                R.drawable.ic_category_electronics,
                Color.parseColor("#EFF6FF"),
                true,
                "9 active prices"
        ));

        // 4. Indiranagar Home Rentals
        list.add(new Shop(
                "Indiranagar Home Rentals",
                "Rent & Real Estate",
                "3.5 km away",
                "Verified residential apartments, studio flats & PG rooms",
                "4.7",
                R.drawable.ic_category_real_estate,
                Color.parseColor("#F0FDF4"),
                true,
                "12 active listings"
        ));

        // 5. Green Care Pharmacy
        list.add(new Shop(
                "Green Care Pharmacy",
                "Healthcare",
                "1.8 km away",
                "Prescription medicines, wellness checkup kits & consultations",
                "4.9",
                R.drawable.ic_category_healthcare,
                Color.parseColor("#FEF2F2"),
                true,
                "22 active items"
        ));

        // 6. Campus Learning Center
        list.add(new Shop(
                "Campus Learning Center",
                "Education",
                "2.4 km away",
                "Computer programming, coaching batches & study materials",
                "4.8",
                R.drawable.ic_category_education,
                Color.parseColor("#F0FDFA"),
                true,
                "8 active courses"
        ));

        return list;
    }
}
