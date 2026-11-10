package com.example.pricepulse.models;

public class CategoryProduct {
    private final String name;
    private final String category;
    private final String price;
    private final String priceStatus;
    private final String marketAvg;
    private final int iconResId;
    private final int badgeBgColor;
    private final boolean isFairPrice;

    public CategoryProduct(String name, String category, String price, String priceStatus, String marketAvg, int iconResId, int badgeBgColor, boolean isFairPrice) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.priceStatus = priceStatus;
        this.marketAvg = marketAvg;
        this.iconResId = iconResId;
        this.badgeBgColor = badgeBgColor;
        this.isFairPrice = isFairPrice;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public String getPriceStatus() {
        return priceStatus;
    }

    public String getMarketAvg() {
        return marketAvg;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getBadgeBgColor() {
        return badgeBgColor;
    }

    public boolean isFairPrice() {
        return isFairPrice;
    }
}
