package com.example.pricepulse.models;

import java.io.Serializable;

public class Shop implements Serializable {
    private String name;
    private String category;
    private String distance;
    private String description;
    private String rating;
    private int iconResId;
    private int badgeBgColor;
    private boolean isVerified;
    private String activeDealsCount;

    public Shop(String name, String category, String distance, String description, String rating, int iconResId, int badgeBgColor, boolean isVerified, String activeDealsCount) {
        this.name = name;
        this.category = category;
        this.distance = distance;
        this.description = description;
        this.rating = rating;
        this.iconResId = iconResId;
        this.badgeBgColor = badgeBgColor;
        this.isVerified = isVerified;
        this.activeDealsCount = activeDealsCount;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDistance() {
        return distance;
    }

    public String getDescription() {
        return description;
    }

    public String getRating() {
        return rating;
    }

    public int getIconResId() {
        return iconResId;
    }

    public int getBadgeBgColor() {
        return badgeBgColor;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getActiveDealsCount() {
        return activeDealsCount;
    }
}
