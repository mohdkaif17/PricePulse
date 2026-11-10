package com.example.pricepulse;

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

import java.util.ArrayList;
import java.util.List;

public class PriceHistoryActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_NAME = "extra_product_name";
    public static final String EXTRA_CATEGORY_NAME = "extra_category_name";
    public static final String EXTRA_PRODUCT_DESCRIPTION = "extra_product_description";
    public static final String EXTRA_CATEGORY_KEY = "extra_category_key";

    private enum TimeRange {
        RANGE_7D,
        RANGE_30D,
        RANGE_3M,
        RANGE_6M,
        RANGE_1Y
    }

    private TimeRange selectedRange = TimeRange.RANGE_6M;
    private PriceTrendChartView chartPriceTrend;

    private TextView chipRange7D;
    private TextView chipRange30D;
    private TextView chipRange3M;
    private TextView chipRange6M;
    private TextView chipRange1Y;

    private String categoryKey = CategoryActivity.CATEGORY_ELECTRONICS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_price_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.priceHistoryRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupViews();
        loadProductHistoryData();
    }

    private void setupViews() {
        // Back navigation
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Save / bookmark action
        findViewById(R.id.btnSaveTop).setOnClickListener(v ->
                Toast.makeText(this, "Price alert saved", Toast.LENGTH_SHORT).show());

        chartPriceTrend = findViewById(R.id.chartPriceTrend);

        chipRange7D = findViewById(R.id.chipRange7D);
        chipRange30D = findViewById(R.id.chipRange30D);
        chipRange3M = findViewById(R.id.chipRange3M);
        chipRange6M = findViewById(R.id.chipRange6M);
        chipRange1Y = findViewById(R.id.chipRange1Y);

        chipRange7D.setOnClickListener(v -> setTimeRange(TimeRange.RANGE_7D));
        chipRange30D.setOnClickListener(v -> setTimeRange(TimeRange.RANGE_30D));
        chipRange3M.setOnClickListener(v -> setTimeRange(TimeRange.RANGE_3M));
        chipRange6M.setOnClickListener(v -> setTimeRange(TimeRange.RANGE_6M));
        chipRange1Y.setOnClickListener(v -> setTimeRange(TimeRange.RANGE_1Y));
    }

    private void loadProductHistoryData() {
        String productName = getIntent().getStringExtra(EXTRA_PRODUCT_NAME);
        String categoryName = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        categoryKey = getIntent().getStringExtra(EXTRA_CATEGORY_KEY);

        // Safe fallback handling
        if (productName == null || productName.trim().isEmpty()) {
            productName = "Samsung Galaxy S24";
        }
        if (categoryName == null || categoryName.trim().isEmpty()) {
            categoryName = getString(R.string.category_electronics);
        }
        if (categoryKey == null || categoryKey.trim().isEmpty()) {
            categoryKey = CategoryActivity.CATEGORY_ELECTRONICS;
        }

        TextView txtProductName = findViewById(R.id.txtHistoryProductName);
        TextView txtCategoryBadge = findViewById(R.id.txtHistoryCategoryBadge);
        ImageView imgProductIcon = findViewById(R.id.imgHistoryProductIcon);

        txtProductName.setText(productName);
        txtCategoryBadge.setText(categoryName);

        TextView txtCurrentPrice = findViewById(R.id.txtHistoryCurrentPrice);
        TextView txtMarketAvg = findViewById(R.id.txtHistoryMarketAvg);
        TextView txtPriceDiff = findViewById(R.id.txtHistoryPriceDiff);

        TextView txtSummaryHighest = findViewById(R.id.txtSummaryHighest);
        TextView txtSummaryLowest = findViewById(R.id.txtSummaryLowest);
        TextView txtSummaryAverage = findViewById(R.id.txtSummaryAverage);
        TextView txtSummaryCurrent = findViewById(R.id.txtSummaryCurrent);

        TextView txtTrendPercentageBadge = findViewById(R.id.txtTrendPercentageBadge);
        TextView txtTrendInsightDesc = findViewById(R.id.txtTrendInsightDesc);

        if (CategoryActivity.CATEGORY_REAL_ESTATE.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_real_estate);
            txtCurrentPrice.setText("₹36,500 /mo");
            txtMarketAvg.setText("₹39,500 /mo");
            txtPriceDiff.setText("₹3,000 /mo below market average (Fair Deal)");

            txtSummaryHighest.setText("₹41,000 /mo");
            txtSummaryLowest.setText("₹36,500 /mo");
            txtSummaryAverage.setText("₹38,500 /mo");
            txtSummaryCurrent.setText("₹36,500 /mo");

            txtTrendPercentageBadge.setText("7.6% decrease");
            txtTrendInsightDesc.setText("Rental rates in this zone have softened compared to previous quarterly records.");

        } else if (CategoryActivity.CATEGORY_VEHICLES.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_vehicles);
            txtCurrentPrice.setText("₹1,42,000");
            txtMarketAvg.setText("₹1,52,000");
            txtPriceDiff.setText("₹10,000 below market average (Fair Deal)");

            txtSummaryHighest.setText("₹1,58,000");
            txtSummaryLowest.setText("₹1,42,000");
            txtSummaryAverage.setText("₹1,49,500");
            txtSummaryCurrent.setText("₹1,42,000");

            txtTrendPercentageBadge.setText("6.5% decrease");
            txtTrendInsightDesc.setText("Market prices have trended downward over the last 6 months.");

        } else if (CategoryActivity.CATEGORY_HEALTHCARE.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_healthcare);
            txtCurrentPrice.setText("₹1,790");
            txtMarketAvg.setText("₹2,200");
            txtPriceDiff.setText("₹410 below market average (Fair Deal)");

            txtSummaryHighest.setText("₹2,350");
            txtSummaryLowest.setText("₹1,790");
            txtSummaryAverage.setText("₹2,050");
            txtSummaryCurrent.setText("₹1,790");

            txtTrendPercentageBadge.setText("18.6% decrease");
            txtTrendInsightDesc.setText("Generic and competitive dispensary pricing has reduced average costs.");

        } else if (CategoryActivity.CATEGORY_RESTAURANTS.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_restaurants);
            txtCurrentPrice.setText("₹620");
            txtMarketAvg.setText("₹720");
            txtPriceDiff.setText("₹100 below market average (Fair Deal)");

            txtSummaryHighest.setText("₹780");
            txtSummaryLowest.setText("₹620");
            txtSummaryAverage.setText("₹690");
            txtSummaryCurrent.setText("₹620");

            txtTrendPercentageBadge.setText("13.8% decrease");
            txtTrendInsightDesc.setText("Promotional and combo dining benchmarks are currently favorable.");

        } else if (CategoryActivity.CATEGORY_EDUCATION.equals(categoryKey)) {
            imgProductIcon.setImageResource(R.drawable.ic_category_education);
            txtCurrentPrice.setText("₹23,000");
            txtMarketAvg.setText("₹28,000");
            txtPriceDiff.setText("₹5,000 below market average (Fair Deal)");

            txtSummaryHighest.setText("₹29,500");
            txtSummaryLowest.setText("₹23,000");
            txtSummaryAverage.setText("₹26,000");
            txtSummaryCurrent.setText("₹23,000");

            txtTrendPercentageBadge.setText("17.8% decrease");
            txtTrendInsightDesc.setText("Early-enrollment fee benchmarks show substantial savings.");

        } else {
            // Default: Electronics (Samsung Galaxy S24)
            imgProductIcon.setImageResource(R.drawable.ic_category_electronics);
            txtCurrentPrice.setText("₹74,999");
            txtMarketAvg.setText("₹79,999");
            txtPriceDiff.setText("₹5,000 below peak price (Fair Deal)");

            txtSummaryHighest.setText("₹79,999");
            txtSummaryLowest.setText("₹74,999");
            txtSummaryAverage.setText("₹77,415");
            txtSummaryCurrent.setText("₹74,999");

            txtTrendPercentageBadge.setText("₹5,000 drop");
            txtTrendInsightDesc.setText("Price dropped by ₹5,000 since January.");
        }

        renderHistoricalPricesList();
        updateChartData();
    }

    private static class HistoricalPriceItem {
        final String month;
        final String price;
        final String badge;
        final int badgeBgRes;
        final int badgeTextColorRes;

        HistoricalPriceItem(String month, String price, String badge, int badgeBgRes, int badgeTextColorRes) {
            this.month = month;
            this.price = price;
            this.badge = badge;
            this.badgeBgRes = badgeBgRes;
            this.badgeTextColorRes = badgeTextColorRes;
        }
    }

    private void renderHistoricalPricesList() {
        LinearLayout layoutContainer = findViewById(R.id.layoutHistoricalPricesContainer);
        if (layoutContainer == null) return;
        layoutContainer.removeAllViews();

        List<HistoricalPriceItem> list = new ArrayList<>();
        list.add(new HistoricalPriceItem("January 2026", "₹79,999", PriceStatusHelper.STATUS_OVERPRICED, R.drawable.bg_status_overpriced, R.color.price_overpriced_text));
        list.add(new HistoricalPriceItem("February 2026", "₹78,499", PriceStatusHelper.STATUS_FAIR, R.drawable.bg_status_fair, R.color.price_fair_text));
        list.add(new HistoricalPriceItem("March 2026", "₹77,999", PriceStatusHelper.STATUS_FAIR, R.drawable.bg_status_fair, R.color.price_fair_text));
        list.add(new HistoricalPriceItem("April 2026", "₹76,999", PriceStatusHelper.STATUS_FAIR, R.drawable.bg_status_fair, R.color.price_fair_text));
        list.add(new HistoricalPriceItem("May 2026", "₹75,999", PriceStatusHelper.STATUS_FAIR, R.drawable.bg_status_fair, R.color.price_fair_text));
        list.add(new HistoricalPriceItem("June 2026", "₹74,999", PriceStatusHelper.STATUS_BEST_DEAL, R.drawable.bg_status_best_deal, R.color.price_best_deal_text));

        LayoutInflater inflater = LayoutInflater.from(this);
        for (HistoricalPriceItem item : list) {
            View cardView = inflater.inflate(R.layout.item_historical_price, layoutContainer, false);
            TextView txtMonth = cardView.findViewById(R.id.txtHistoryMonth);
            TextView txtPrice = cardView.findViewById(R.id.txtHistoryPrice);
            TextView txtBadge = cardView.findViewById(R.id.txtHistoryBadge);

            txtMonth.setText(item.month);
            txtPrice.setText(item.price);
            PriceStatusHelper.applyStatusBadge(txtBadge, item.badge);

            layoutContainer.addView(cardView);
        }
    }

    private void setTimeRange(TimeRange range) {
        selectedRange = range;
        updateTimeRangeChipsUi();
        updateChartData();
    }

    private void updateTimeRangeChipsUi() {
        chipRange7D.setBackgroundResource(selectedRange == TimeRange.RANGE_7D ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipRange7D.setTextColor(ContextCompat.getColor(this, selectedRange == TimeRange.RANGE_7D ?
                R.color.white : R.color.text_secondary));

        chipRange30D.setBackgroundResource(selectedRange == TimeRange.RANGE_30D ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipRange30D.setTextColor(ContextCompat.getColor(this, selectedRange == TimeRange.RANGE_30D ?
                R.color.white : R.color.text_secondary));

        chipRange3M.setBackgroundResource(selectedRange == TimeRange.RANGE_3M ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipRange3M.setTextColor(ContextCompat.getColor(this, selectedRange == TimeRange.RANGE_3M ?
                R.color.white : R.color.text_secondary));

        chipRange6M.setBackgroundResource(selectedRange == TimeRange.RANGE_6M ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipRange6M.setTextColor(ContextCompat.getColor(this, selectedRange == TimeRange.RANGE_6M ?
                R.color.white : R.color.text_secondary));

        chipRange1Y.setBackgroundResource(selectedRange == TimeRange.RANGE_1Y ?
                R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipRange1Y.setTextColor(ContextCompat.getColor(this, selectedRange == TimeRange.RANGE_1Y ?
                R.color.white : R.color.text_secondary));
    }

    private void updateChartData() {
        List<PriceTrendChartView.DataPoint> points = new ArrayList<>();

        if (CategoryActivity.CATEGORY_REAL_ESTATE.equals(categoryKey)) {
            switch (selectedRange) {
                case RANGE_7D:
                    points.add(new PriceTrendChartView.DataPoint("Day 1", 37000, "₹37k"));
                    points.add(new PriceTrendChartView.DataPoint("Day 3", 36800, "₹36.8k"));
                    points.add(new PriceTrendChartView.DataPoint("Day 5", 36500, "₹36.5k"));
                    points.add(new PriceTrendChartView.DataPoint("Today", 36500, "₹36.5k"));
                    break;
                case RANGE_30D:
                    points.add(new PriceTrendChartView.DataPoint("W1", 38000, "₹38k"));
                    points.add(new PriceTrendChartView.DataPoint("W2", 37500, "₹37.5k"));
                    points.add(new PriceTrendChartView.DataPoint("W3", 37000, "₹37k"));
                    points.add(new PriceTrendChartView.DataPoint("W4", 36500, "₹36.5k"));
                    break;
                case RANGE_3M:
                    points.add(new PriceTrendChartView.DataPoint("Apr", 39000, "₹39k"));
                    points.add(new PriceTrendChartView.DataPoint("May", 37500, "₹37.5k"));
                    points.add(new PriceTrendChartView.DataPoint("Jun", 36500, "₹36.5k"));
                    break;
                case RANGE_1Y:
                    points.add(new PriceTrendChartView.DataPoint("Jul", 42000, "₹42k"));
                    points.add(new PriceTrendChartView.DataPoint("Oct", 40500, "₹40.5k"));
                    points.add(new PriceTrendChartView.DataPoint("Jan", 39500, "₹39.5k"));
                    points.add(new PriceTrendChartView.DataPoint("Apr", 38000, "₹38k"));
                    points.add(new PriceTrendChartView.DataPoint("Jun", 36500, "₹36.5k"));
                    break;
                case RANGE_6M:
                default:
                    points.add(new PriceTrendChartView.DataPoint("Jan", 39500, "₹39.5k"));
                    points.add(new PriceTrendChartView.DataPoint("Feb", 38500, "₹38.5k"));
                    points.add(new PriceTrendChartView.DataPoint("Mar", 41000, "₹41k"));
                    points.add(new PriceTrendChartView.DataPoint("Apr", 38000, "₹38k"));
                    points.add(new PriceTrendChartView.DataPoint("May", 37000, "₹37k"));
                    points.add(new PriceTrendChartView.DataPoint("Jun", 36500, "₹36.5k"));
                    break;
            }
        } else {
            // Default: Electronics
            switch (selectedRange) {
                case RANGE_7D:
                    points.add(new PriceTrendChartView.DataPoint("Day 1", 75499, "₹75,499"));
                    points.add(new PriceTrendChartView.DataPoint("Day 3", 75200, "₹75,200"));
                    points.add(new PriceTrendChartView.DataPoint("Day 5", 74999, "₹74,999"));
                    points.add(new PriceTrendChartView.DataPoint("Today", 74999, "₹74,999"));
                    break;
                case RANGE_30D:
                    points.add(new PriceTrendChartView.DataPoint("W1", 76999, "₹76,999"));
                    points.add(new PriceTrendChartView.DataPoint("W2", 75999, "₹75,999"));
                    points.add(new PriceTrendChartView.DataPoint("W3", 75200, "₹75,200"));
                    points.add(new PriceTrendChartView.DataPoint("W4", 74999, "₹74,999"));
                    break;
                case RANGE_3M:
                    points.add(new PriceTrendChartView.DataPoint("Apr", 76999, "₹76,999"));
                    points.add(new PriceTrendChartView.DataPoint("May", 75999, "₹75,999"));
                    points.add(new PriceTrendChartView.DataPoint("Jun", 74999, "₹74,999"));
                    break;
                case RANGE_1Y:
                    points.add(new PriceTrendChartView.DataPoint("Jul", 84999, "₹84,999"));
                    points.add(new PriceTrendChartView.DataPoint("Oct", 82500, "₹82,500"));
                    points.add(new PriceTrendChartView.DataPoint("Jan", 79999, "₹79,999"));
                    points.add(new PriceTrendChartView.DataPoint("Apr", 76999, "₹76,999"));
                    points.add(new PriceTrendChartView.DataPoint("Jun", 74999, "₹74,999"));
                    break;
                case RANGE_6M:
                default:
                    points.add(new PriceTrendChartView.DataPoint("Jan", 79999, "₹79,999"));
                    points.add(new PriceTrendChartView.DataPoint("Feb", 78499, "₹78,499"));
                    points.add(new PriceTrendChartView.DataPoint("Mar", 77999, "₹77,999"));
                    points.add(new PriceTrendChartView.DataPoint("Apr", 76999, "₹76,999"));
                    points.add(new PriceTrendChartView.DataPoint("May", 75999, "₹75,999"));
                    points.add(new PriceTrendChartView.DataPoint("Jun", 74999, "₹74,999"));
                    break;
            }
        }

        chartPriceTrend.setData(points);
    }
}
