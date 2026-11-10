package com.example.pricepulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
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
import java.util.Comparator;
import java.util.List;

public class CompareActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "extra_product_description";
    public static final String EXTRA_CATEGORY_KEY = "extra_category_key";

    private enum SortMode {
        LOWEST_PRICE,
        NEAREST,
        BEST_DEAL
    }

    private static class ShopItem {
        final String name;
        final String location;
        final String distance;
        final double distanceKm;
        final String price;
        final double priceValue;
        final String statusBadge;
        final int statusBgRes;
        final int statusTextColorRes;
        final int statusScore; // 1 = Best Deal, 2 = Fair Price, 3 = Slightly Expensive, 4 = Overpriced

        ShopItem(String name, String location, String distance, double distanceKm,
                 String price, double priceValue, String statusBadge,
                 int statusBgRes, int statusTextColorRes, int statusScore) {
            this.name = name;
            this.location = location;
            this.distance = distance;
            this.distanceKm = distanceKm;
            this.price = price;
            this.priceValue = priceValue;
            this.statusBadge = statusBadge;
            this.statusBgRes = statusBgRes;
            this.statusTextColorRes = statusTextColorRes;
            this.statusScore = statusScore;
        }
    }

    private List<ShopItem> shopList = new ArrayList<>();
    private SortMode currentSortMode = SortMode.LOWEST_PRICE;
    private LinearLayout layoutShopListContainer;
    private TextView chipSortLowestPrice;
    private TextView chipSortNearest;
    private TextView chipSortBestDeal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_compare);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.compareRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViews();
        loadComparisonData();
    }

    private void setupViews() {
        // Back navigation
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Save comparison action
        findViewById(R.id.btnSaveTop).setOnClickListener(v ->
                Toast.makeText(this, "Comparison saved", Toast.LENGTH_SHORT).show());

        layoutShopListContainer = findViewById(R.id.layoutShopListContainer);
        chipSortLowestPrice = findViewById(R.id.chipSortLowestPrice);
        chipSortNearest = findViewById(R.id.chipSortNearest);
        chipSortBestDeal = findViewById(R.id.chipSortBestDeal);

        chipSortLowestPrice.setOnClickListener(v -> setSortMode(SortMode.LOWEST_PRICE));
        chipSortNearest.setOnClickListener(v -> setSortMode(SortMode.NEAREST));
        chipSortBestDeal.setOnClickListener(v -> setSortMode(SortMode.BEST_DEAL));
    }

    private void loadComparisonData() {
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        String categoryKey = getIntent().getStringExtra(EXTRA_CATEGORY_KEY);

        // Safe default handling
        if (productName == null || productName.trim().isEmpty()) {
            productName = "Samsung Galaxy S24";
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = getString(R.string.category_electronics);
        }
        if (categoryKey == null || categoryKey.trim().isEmpty()) {
            categoryKey = CategoryActivity.CATEGORY_ELECTRONICS;
        }

        TextView txtProductName = findViewById(R.id.txtCompareProductName);
        TextView txtCategoryBadge = findViewById(R.id.txtCompareCategoryBadge);
        ImageView imgProductIcon = findViewById(R.id.imgProductIcon);

        txtProductName.setText(productName);
        txtCategoryBadge.setText(categoryName);

        TextView txtMarketAvg = findViewById(R.id.txtMarketAverageValue);
        TextView txtBestPrice = findViewById(R.id.txtBestPriceValue);
        TextView txtSavings = findViewById(R.id.txtSavingsValue);

        TextView txtBestDealShopName = findViewById(R.id.txtBestDealShopName);
        TextView txtBestDealPrice = findViewById(R.id.txtBestDealPrice);
        TextView txtBestDealDistance = findViewById(R.id.txtBestDealDistance);
        TextView txtBestDealSavingsBadge = findViewById(R.id.txtBestDealSavingsBadge);
        MaterialButton btnBestDealViewShop = findViewById(R.id.btnBestDealViewShop);

        shopList.clear();

        // Category-specific demo values
        if (CategoryActivity.CATEGORY_REAL_ESTATE.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_real_estate);
            txtMarketAvg.setText("₹39,500 /mo");
            txtBestPrice.setText("₹36,500 /mo");
            txtSavings.setText("₹3,000");

            txtBestDealShopName.setText("Indiranagar Estates");
            txtBestDealPrice.setText("₹36,500 /mo");
            txtBestDealDistance.setText("1.2 km away • Indiranagar");
            txtBestDealSavingsBadge.setText("Save ₹3,000 /mo");

            shopList.add(new ShopItem("Indiranagar Estates", "Indiranagar, Bangalore", "1.2 km away", 1.2,
                    "₹36,500 /mo", 36500, "⭐ Best Deal Nearby",
                    R.drawable.bg_status_best_deal, R.color.price_best_deal_text, 1));
            shopList.add(new ShopItem("Metro Realty Hub", "MG Road, Bangalore", "2.4 km away", 2.4,
                    "₹38,000 /mo", 38000, "🟢 Fair Price",
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));
            shopList.add(new ShopItem("Urban Square Living", "Koramangala, Bangalore", "3.7 km away", 3.7,
                    "₹41,000 /mo", 41000, "🟡 Slightly Expensive",
                    R.drawable.bg_status_slightly_expensive, R.color.price_slightly_expensive_text, 3));
            shopList.add(new ShopItem("Prestige Prime Living", "Whitefield, Bangalore", "4.5 km away", 4.5,
                    "₹45,000 /mo", 45000, "🔴 Overpriced",
                    R.drawable.bg_status_overpriced, R.color.price_overpriced_text, 4));

            btnBestDealViewShop.setOnClickListener(v -> openShop("Indiranagar Estates", "Indiranagar, Bangalore", "1.2 km away"));

        } else if (CategoryActivity.CATEGORY_VEHICLES.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_vehicles);
            txtMarketAvg.setText("₹1,52,000");
            txtBestPrice.setText("₹1,42,000");
            txtSavings.setText("₹10,000");

            txtBestDealShopName.setText("Apex Motorworks");
            txtBestDealPrice.setText("₹1,42,000");
            txtBestDealDistance.setText("1.5 km away • Bangalore");
            txtBestDealSavingsBadge.setText("Save ₹10,000");

            shopList.add(new ShopItem("Apex Motorworks", "Indiranagar, Bangalore", "1.5 km away", 1.5,
                    "₹1,42,000", 142000, "⭐ Best Deal Nearby",
                    R.drawable.bg_status_best_deal, R.color.price_best_deal_text, 1));
            shopList.add(new ShopItem("Prime Auto Hub", "Koramangala, Bangalore", "2.8 km away", 2.8,
                    "₹1,45,000", 145000, "🟢 Fair Price",
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));
            shopList.add(new ShopItem("City Speed Motors", "HSR Layout, Bangalore", "3.5 km away", 3.5,
                    "₹1,54,000", 154000, "🟡 Slightly Expensive",
                    R.drawable.bg_status_slightly_expensive, R.color.price_slightly_expensive_text, 3));
            shopList.add(new ShopItem("National Wheels", "Whitefield, Bangalore", "4.8 km away", 4.8,
                    "₹1,62,000", 162000, "🔴 Overpriced",
                    R.drawable.bg_status_overpriced, R.color.price_overpriced_text, 4));

            btnBestDealViewShop.setOnClickListener(v -> openShop("Apex Motorworks", "Indiranagar, Bangalore", "1.5 km away"));

        } else if (CategoryActivity.CATEGORY_HEALTHCARE.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_healthcare);
            txtMarketAvg.setText("₹2,200");
            txtBestPrice.setText("₹1,790");
            txtSavings.setText("₹410");

            txtBestDealShopName.setText("Apollo Pharmacy");
            txtBestDealPrice.setText("₹1,790");
            txtBestDealDistance.setText("0.6 km away • Bangalore");
            txtBestDealSavingsBadge.setText("Save ₹410");

            shopList.add(new ShopItem("Apollo Pharmacy", "Indiranagar, Bangalore", "0.6 km away", 0.6,
                    "₹1,790", 1790, "⭐ Best Deal Nearby",
                    R.drawable.bg_status_best_deal, R.color.price_best_deal_text, 1));
            shopList.add(new ShopItem("MedPlus Diagnostics", "Domlur, Bangalore", "1.4 km away", 1.4,
                    "₹1,850", 1850, "🟢 Fair Price",
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));
            shopList.add(new ShopItem("Fortis Care Chemist", "MG Road, Bangalore", "2.8 km away", 2.8,
                    "₹2,350", 2350, "🟡 Slightly Expensive",
                    R.drawable.bg_status_slightly_expensive, R.color.price_slightly_expensive_text, 3));
            shopList.add(new ShopItem("City Wellness Care", "Whitefield, Bangalore", "4.1 km away", 4.1,
                    "₹2,690", 2690, "🔴 Overpriced",
                    R.drawable.bg_status_overpriced, R.color.price_overpriced_text, 4));

            btnBestDealViewShop.setOnClickListener(v -> openShop("Apollo Pharmacy", "Indiranagar, Bangalore", "0.6 km away"));

        } else if (CategoryActivity.CATEGORY_RESTAURANTS.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_restaurants);
            txtMarketAvg.setText("₹720");
            txtBestPrice.setText("₹620");
            txtSavings.setText("₹100");

            txtBestDealShopName.setText("Spice Route Kitchen");
            txtBestDealPrice.setText("₹620");
            txtBestDealDistance.setText("0.4 km away • Bangalore");
            txtBestDealSavingsBadge.setText("Save ₹100");

            shopList.add(new ShopItem("Spice Route Kitchen", "Indiranagar, Bangalore", "0.4 km away", 0.4,
                    "₹620", 620, "⭐ Best Deal Nearby",
                    R.drawable.bg_status_best_deal, R.color.price_best_deal_text, 1));
            shopList.add(new ShopItem("Grand Dining House", "Koramangala, Bangalore", "1.1 km away", 1.1,
                    "₹650", 650, "🟢 Fair Price",
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));
            shopList.add(new ShopItem("Royal Feast Bistro", "MG Road, Bangalore", "2.3 km away", 2.3,
                    "₹780", 780, "🟡 Slightly Expensive",
                    R.drawable.bg_status_slightly_expensive, R.color.price_slightly_expensive_text, 3));
            shopList.add(new ShopItem("The Olive Gourmet", "Whitefield, Bangalore", "3.9 km away", 3.9,
                    "₹890", 890, "🔴 Overpriced",
                    R.drawable.bg_status_overpriced, R.color.price_overpriced_text, 4));

            btnBestDealViewShop.setOnClickListener(v -> openShop("Spice Route Kitchen", "Indiranagar, Bangalore", "0.4 km away"));

        } else if (CategoryActivity.CATEGORY_EDUCATION.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_education);
            txtMarketAvg.setText("₹28,000");
            txtBestPrice.setText("₹23,000");
            txtSavings.setText("₹5,000");

            txtBestDealShopName.setText("CodeAcademy Center");
            txtBestDealPrice.setText("₹23,000");
            txtBestDealDistance.setText("1.0 km away • Bangalore");
            txtBestDealSavingsBadge.setText("Save ₹5,000");

            shopList.add(new ShopItem("CodeAcademy Center", "Indiranagar, Bangalore", "1.0 km away", 1.0,
                    "₹23,000", 23000, "⭐ Best Deal Nearby",
                    R.drawable.bg_status_best_deal, R.color.price_best_deal_text, 1));
            shopList.add(new ShopItem("TechPro Institute", "Koramangala, Bangalore", "2.3 km away", 2.3,
                    "₹24,500", 24500, "🟢 Fair Price",
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));
            shopList.add(new ShopItem("Zenith Training Hub", "HSR Layout, Bangalore", "3.2 km away", 3.2,
                    "₹29,500", 29500, "🟡 Slightly Expensive",
                    R.drawable.bg_status_slightly_expensive, R.color.price_slightly_expensive_text, 3));
            shopList.add(new ShopItem("Horizon Learning", "Whitefield, Bangalore", "4.5 km away", 4.5,
                    "₹34,000", 34000, "🔴 Overpriced",
                    R.drawable.bg_status_overpriced, R.color.price_overpriced_text, 4));

            btnBestDealViewShop.setOnClickListener(v -> openShop("CodeAcademy Center", "Indiranagar, Bangalore", "1.0 km away"));

        } else {
            // Default: Electronics (Samsung Galaxy S24)
            imgProductIcon.setImageResource(R.drawable.ic_category_electronics);
            txtMarketAvg.setText("₹74,999");
            txtBestPrice.setText("₹73,999");
            txtSavings.setText("₹1,000");

            txtBestDealShopName.setText("Mobile Hub");
            txtBestDealPrice.setText("₹73,999");
            txtBestDealDistance.setText("3.4 km away • Whitefield");
            txtBestDealSavingsBadge.setText("⭐ Best Deal Nearby");

            shopList.add(new ShopItem("Apex Digital Hub", "Indiranagar, Bangalore", "1.2 km away", 1.2,
                    "₹74,999", 74999, PriceStatusHelper.STATUS_FAIR,
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));
            shopList.add(new ShopItem("Tech World", "MG Road, Bangalore", "2.1 km away", 2.1,
                    "₹76,499", 76499, PriceStatusHelper.STATUS_SLIGHTLY_EXPENSIVE,
                    R.drawable.bg_status_slightly_expensive, R.color.price_slightly_expensive_text, 3));
            shopList.add(new ShopItem("Digital Point", "Koramangala, Bangalore", "2.8 km away", 2.8,
                    "₹78,999", 78999, PriceStatusHelper.STATUS_OVERPRICED,
                    R.drawable.bg_status_overpriced, R.color.price_overpriced_text, 4));
            shopList.add(new ShopItem("Mobile Hub", "Whitefield, Bangalore", "3.4 km away", 3.4,
                    "₹73,999", 73999, PriceStatusHelper.STATUS_BEST_DEAL,
                    R.drawable.bg_status_best_deal, R.color.price_best_deal_text, 1));
            shopList.add(new ShopItem("ElectroMart", "HSR Layout, Bangalore", "4.1 km away", 4.1,
                    "₹75,499", 75499, PriceStatusHelper.STATUS_FAIR,
                    R.drawable.bg_status_fair, R.color.price_fair_text, 2));

            btnBestDealViewShop.setOnClickListener(v -> openShop("Mobile Hub", "Whitefield, Bangalore", "3.4 km away"));
        }

        View btnViewPriceHistory = findViewById(R.id.btnViewPriceHistory);
        if (btnViewPriceHistory != null) {
            final String currentProductName = productName;
            final String currentCategoryName = categoryName;
            final String currentCategoryKey = categoryKey;
            btnViewPriceHistory.setOnClickListener(v -> {
                Intent intent = new Intent(CompareActivity.this, PriceHistoryActivity.class);
                intent.putExtra(PriceHistoryActivity.EXTRA_PRODUCT_NAME, currentProductName);
                intent.putExtra(PriceHistoryActivity.EXTRA_CATEGORY_NAME, currentCategoryName);
                intent.putExtra(PriceHistoryActivity.EXTRA_CATEGORY_KEY, currentCategoryKey);
                startActivity(intent);
            });
        }

        renderShopList();
    }

    private void setSortMode(SortMode mode) {
        currentSortMode = mode;
        updateSortChipsUi();
        renderShopList();
    }

    private void updateSortChipsUi() {
        chipSortLowestPrice.setBackgroundResource(currentSortMode == SortMode.LOWEST_PRICE ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipSortLowestPrice.setTextColor(ContextCompat.getColor(this, currentSortMode == SortMode.LOWEST_PRICE ?
                R.color.white : R.color.text_secondary));

        chipSortNearest.setBackgroundResource(currentSortMode == SortMode.NEAREST ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipSortNearest.setTextColor(ContextCompat.getColor(this, currentSortMode == SortMode.NEAREST ?
                R.color.white : R.color.text_secondary));

        chipSortBestDeal.setBackgroundResource(currentSortMode == SortMode.BEST_DEAL ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipSortBestDeal.setTextColor(ContextCompat.getColor(this, currentSortMode == SortMode.BEST_DEAL ?
                R.color.white : R.color.text_secondary));
    }

    private void renderShopList() {
        List<ShopItem> sortedList = new ArrayList<>(shopList);

        if (currentSortMode == SortMode.LOWEST_PRICE) {
            Collections.sort(sortedList, (a, b) -> Double.compare(a.priceValue, b.priceValue));
        } else if (currentSortMode == SortMode.NEAREST) {
            Collections.sort(sortedList, (a, b) -> Double.compare(a.distanceKm, b.distanceKm));
        } else if (currentSortMode == SortMode.BEST_DEAL) {
            Collections.sort(sortedList, (a, b) -> Integer.compare(a.statusScore, b.statusScore));
        }

        layoutShopListContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (ShopItem item : sortedList) {
            View itemView = inflater.inflate(R.layout.item_shop_comparison, layoutShopListContainer, false);

            TextView txtName = itemView.findViewById(R.id.txtShopName);
            TextView txtLocation = itemView.findViewById(R.id.txtShopLocation);
            TextView txtDistance = itemView.findViewById(R.id.txtShopDistance);
            TextView txtPrice = itemView.findViewById(R.id.txtShopPrice);
            TextView txtStatusBadge = itemView.findViewById(R.id.txtShopStatusBadge);

            txtName.setText(item.name);
            txtLocation.setText(item.location);
            txtDistance.setText(item.distance);
            txtPrice.setText(item.price);
            PriceStatusHelper.applyStatusBadge(txtStatusBadge, item.statusBadge);

            itemView.setOnClickListener(v -> openShop(item.name, item.location, item.distance));

            layoutShopListContainer.addView(itemView);
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
