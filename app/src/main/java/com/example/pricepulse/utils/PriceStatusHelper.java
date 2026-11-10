package com.example.pricepulse.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.pricepulse.R;

/**
 * Centralized helper utility for Price Pulse status indicators and badges.
 * Formats status strings, background drawables, text colors, icons, and dot colors consistently across the app.
 */
public class PriceStatusHelper {

    public static final String STATUS_FAIR = "🟢 Fair Price";
    public static final String STATUS_SLIGHTLY_EXPENSIVE = "🟡 Slightly Expensive";
    public static final String STATUS_OVERPRICED = "🔴 Overpriced";
    public static final String STATUS_BEST_DEAL = "⭐ Best Deal Nearby";

    public enum StatusType {
        FAIR_PRICE,
        SLIGHTLY_EXPENSIVE,
        OVERPRICED,
        BEST_DEAL
    }

    public static StatusType parseStatus(String statusStr) {
        if (statusStr == null) return StatusType.FAIR_PRICE;
        String lower = statusStr.toLowerCase();
        if (lower.contains("best deal")) {
            return StatusType.BEST_DEAL;
        } else if (lower.contains("slightly") || lower.contains("high")) {
            return StatusType.SLIGHTLY_EXPENSIVE;
        } else if (lower.contains("overpriced") || lower.contains("peak")) {
            return StatusType.OVERPRICED;
        } else {
            return StatusType.FAIR_PRICE;
        }
    }

    public static String getFormattedStatusText(StatusType type) {
        switch (type) {
            case BEST_DEAL:
                return STATUS_BEST_DEAL;
            case SLIGHTLY_EXPENSIVE:
                return STATUS_SLIGHTLY_EXPENSIVE;
            case OVERPRICED:
                return STATUS_OVERPRICED;
            case FAIR_PRICE:
            default:
                return STATUS_FAIR;
        }
    }

    public static int getBackgroundResource(StatusType type) {
        switch (type) {
            case BEST_DEAL:
                return R.drawable.bg_status_best_deal;
            case SLIGHTLY_EXPENSIVE:
                return R.drawable.bg_status_slightly_expensive;
            case OVERPRICED:
                return R.drawable.bg_status_overpriced;
            case FAIR_PRICE:
            default:
                return R.drawable.bg_status_fair;
        }
    }

    public static int getTextColorRes(StatusType type) {
        switch (type) {
            case BEST_DEAL:
                return R.color.price_best_deal_text;
            case SLIGHTLY_EXPENSIVE:
                return R.color.price_slightly_expensive_text;
            case OVERPRICED:
                return R.color.price_overpriced_text;
            case FAIR_PRICE:
            default:
                return R.color.price_fair_text;
        }
    }

    public static int getMainColorRes(StatusType type) {
        switch (type) {
            case BEST_DEAL:
                return R.color.price_best_deal;
            case SLIGHTLY_EXPENSIVE:
                return R.color.price_slightly_expensive;
            case OVERPRICED:
                return R.color.price_overpriced;
            case FAIR_PRICE:
            default:
                return R.color.price_fair;
        }
    }

    public static int getIconResource(StatusType type) {
        switch (type) {
            case BEST_DEAL:
                return R.drawable.ic_deal_sparkle;
            case FAIR_PRICE:
                return R.drawable.ic_verified_badge;
            case SLIGHTLY_EXPENSIVE:
            case OVERPRICED:
            default:
                return R.drawable.ic_deal_sparkle;
        }
    }

    /**
     * Applies standard status badge styling (background, text color, formatted text) to a TextView.
     */
    public static void applyStatusBadge(TextView textView, String statusStr) {
        if (textView == null) return;
        Context context = textView.getContext();
        StatusType type = parseStatus(statusStr);

        textView.setText(getFormattedStatusText(type));
        textView.setBackgroundResource(getBackgroundResource(type));
        textView.setTextColor(ContextCompat.getColor(context, getTextColorRes(type)));
    }

    /**
     * Applies status styling to a container Layout, TextView, optional Icon, and optional Dot.
     */
    public static void applyStatusPill(Context context, View containerView, TextView textView, ImageView iconView, View dotView, String statusStr) {
        StatusType type = parseStatus(statusStr);
        if (containerView != null) {
            containerView.setBackgroundResource(getBackgroundResource(type));
        }
        if (textView != null) {
            textView.setText(getFormattedStatusText(type));
            textView.setTextColor(ContextCompat.getColor(context, getTextColorRes(type)));
        }
        if (iconView != null) {
            iconView.setImageResource(getIconResource(type));
            iconView.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context, getMainColorRes(type))));
        }
        if (dotView != null) {
            dotView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, getMainColorRes(type))));
        }
    }
}
