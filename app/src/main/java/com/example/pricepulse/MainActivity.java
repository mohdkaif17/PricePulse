package com.example.pricepulse;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pricepulse.activities.LoginActivity;
import com.example.pricepulse.data.remote.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupPulsePostListeners();
        setupCategoryClickListeners();
        setupNearbyDealsListener();
        setupDealCardListeners();
        setupBottomNavigation();
    }

    private void setupPulsePostListeners() {
        View btnPulsePost = findViewById(R.id.btnPulsePostAction);
        if (btnPulsePost != null) {
            btnPulsePost.setOnClickListener(v -> {
                SessionManager sessionManager = new SessionManager(MainActivity.this);
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(MainActivity.this, "Please log in to submit a PulsePost", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, PulsePostActivity.class);
                    startActivity(intent);
                }
            });
        }

        View btnMyPulsePosts = findViewById(R.id.btnMyPulsePosts);
        if (btnMyPulsePosts != null) {
            btnMyPulsePosts.setOnClickListener(v -> {
                SessionManager sessionManager = new SessionManager(MainActivity.this);
                if (!sessionManager.isLoggedIn()) {
                    Toast.makeText(MainActivity.this, "Please log in to view your PulsePosts", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, ManageListingsActivity.class);
                    intent.putExtra("initial_filter", "MY_LISTINGS");
                    startActivity(intent);
                }
            });
        }
    }

    private void setupBottomNavigation() {
        View navMap = findViewById(R.id.navItemMap);
        if (navMap != null) {
            navMap.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            });
        }

        View navProfile = findViewById(R.id.navItemProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupNearbyDealsListener() {
        View btnViewAll = findViewById(R.id.btnViewAllNearbyDeals);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ShopActivity.class);
                startActivity(intent);
            });
        }

        View layoutSearchBar = findViewById(R.id.layoutSearchBar);
        if (layoutSearchBar != null) {
            layoutSearchBar.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupDealCardListeners() {
        View cardDeal1 = findViewById(R.id.cardDeal1);
        View cardDeal2 = findViewById(R.id.cardDeal2);
        View cardDeal3 = findViewById(R.id.cardDeal3);
        View cardDeal4 = findViewById(R.id.cardDeal4);

        if (cardDeal1 != null) {
            cardDeal1.setOnClickListener(v -> openCompareProduct("Samsung Galaxy S24 (256GB)", CategoryActivity.CATEGORY_ELECTRONICS));
        }
        if (cardDeal2 != null) {
            cardDeal2.setOnClickListener(v -> openCompareProduct("2 BHK Apartment Monthly Rent", CategoryActivity.CATEGORY_REAL_ESTATE));
        }
        if (cardDeal3 != null) {
            cardDeal3.setOnClickListener(v -> openCompareProduct("Sony WH-1000XM5 ANC Headphones", CategoryActivity.CATEGORY_ELECTRONICS));
        }
        if (cardDeal4 != null) {
            cardDeal4.setOnClickListener(v -> openCompareProduct("Premium Synthetic Engine Oil (4L)", CategoryActivity.CATEGORY_VEHICLES));
        }
    }

    private void openCompareProduct(String productName, String categoryKey) {
        Intent intent = new Intent(MainActivity.this, CompareActivity.class);
        intent.putExtra(CompareActivity.EXTRA_PRODUCT_NAME, productName);
        intent.putExtra(CompareActivity.EXTRA_CATEGORY_NAME, categoryKey);
        intent.putExtra(CompareActivity.EXTRA_CATEGORY_KEY, categoryKey);
        startActivity(intent);
    }

    private void setupCategoryClickListeners() {
        View cardElectronics = findViewById(R.id.cardCategoryElectronics);
        View cardRealEstate = findViewById(R.id.cardCategoryRealEstate);
        View cardVehicles = findViewById(R.id.cardCategoryVehicles);
        View cardHealthcare = findViewById(R.id.cardCategoryHealthcare);
        View cardRestaurants = findViewById(R.id.cardCategoryRestaurants);
        View cardEducation = findViewById(R.id.cardCategoryEducation);
        View cardHotels = findViewById(R.id.cardCategoryHotels);
        View cardHomeServices = findViewById(R.id.cardCategoryHomeServices);
        View cardFitness = findViewById(R.id.cardCategoryFitness);
        View cardShopping = findViewById(R.id.cardCategoryShopping);

        if (cardHotels != null) {
            cardHotels.setOnClickListener(v -> openCategory(1L, CategoryActivity.CATEGORY_HOTELS));
        }
        if (cardElectronics != null) {
            cardElectronics.setOnClickListener(v -> openCategory(2L, CategoryActivity.CATEGORY_ELECTRONICS));
        }
        if (cardVehicles != null) {
            cardVehicles.setOnClickListener(v -> openCategory(3L, CategoryActivity.CATEGORY_VEHICLES));
        }
        if (cardHealthcare != null) {
            cardHealthcare.setOnClickListener(v -> openCategory(4L, CategoryActivity.CATEGORY_HEALTHCARE));
        }
        if (cardEducation != null) {
            cardEducation.setOnClickListener(v -> openCategory(5L, CategoryActivity.CATEGORY_EDUCATION));
        }
        if (cardRealEstate != null) {
            cardRealEstate.setOnClickListener(v -> openCategory(6L, CategoryActivity.CATEGORY_REAL_ESTATE));
        }
        if (cardRestaurants != null) {
            cardRestaurants.setOnClickListener(v -> openCategory(7L, CategoryActivity.CATEGORY_RESTAURANTS));
        }
        if (cardHomeServices != null) {
            cardHomeServices.setOnClickListener(v -> openCategory(8L, CategoryActivity.CATEGORY_HOME_SERVICES));
        }
        if (cardFitness != null) {
            cardFitness.setOnClickListener(v -> openCategory(9L, CategoryActivity.CATEGORY_FITNESS));
        }
        if (cardShopping != null) {
            cardShopping.setOnClickListener(v -> openCategory(10L, CategoryActivity.CATEGORY_SHOPPING));
        }
    }

    private void openCategory(long categoryId, String categoryName) {
        Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_ID, categoryId);
        intent.putExtra("category", categoryName);
        intent.putExtra(CategoryActivity.EXTRA_CATEGORY_KEY, categoryName);
        startActivity(intent);
    }
}