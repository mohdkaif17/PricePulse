package com.example.pricepulse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class InteractiveMapView extends View {

    public interface OnShopSelectedListener {
        void onShopSelected(ShopMarker shop);
    }

    public static class ShopMarker {
        public final String id;
        public final String name;
        public final String category;
        public final String location;
        public final String distance;
        public final double distanceKm;
        public final String productName;
        public final String price;
        public final double priceValue;
        public final String statusBadge;
        public final int statusConcept; // 1=Best Deal, 2=Fair, 3=Slightly High, 4=Overpriced
        public final int colorRes;
        public final float relX; // 0.0 to 1.0
        public final float relY; // 0.0 to 1.0
        public boolean isSelected = false;

        public ShopMarker(String id, String name, String category, String location, String distance, double distanceKm,
                          String productName, String price, double priceValue,
                          String statusBadge, int statusConcept, int colorRes, float relX, float relY) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.location = location;
            this.distance = distance;
            this.distanceKm = distanceKm;
            this.productName = productName;
            this.price = price;
            this.priceValue = priceValue;
            this.statusBadge = statusBadge;
            this.statusConcept = statusConcept;
            this.colorRes = colorRes;
            this.relX = relX;
            this.relY = relY;
        }
    }

    private final List<ShopMarker> markers = new ArrayList<>();
    private OnShopSelectedListener listener;

    private Paint bgPaint;
    private Paint buildingPaint;
    private Paint streetPaint;
    private Paint majorRoadPaint;
    private Paint highwayPaint;
    private Paint parkPaint;
    private Paint waterPaint;
    private Paint labelPaint;

    private Paint userLocationOuterPaint;
    private Paint userLocationInnerPaint;
    private Paint userRadiusPaint;

    private Paint pinBgPaint;
    private Paint pinBorderPaint;
    private Paint pinTextPaint;
    private Paint pinSelectedHaloPaint;

    private float density;
    private int primaryColor;
    private int fairColor;
    private int slightlyExpensiveColor;
    private int overpricedColor;
    private int bestDealColor;

    public InteractiveMapView(Context context) {
        super(context);
        init();
    }

    public InteractiveMapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InteractiveMapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        fairColor = ContextCompat.getColor(getContext(), R.color.price_fair);
        slightlyExpensiveColor = ContextCompat.getColor(getContext(), R.color.price_slightly_expensive);
        overpricedColor = ContextCompat.getColor(getContext(), R.color.price_overpriced);
        bestDealColor = ContextCompat.getColor(getContext(), R.color.price_best_deal);

        // Map Styling Paints
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bgPaint.setColor(Color.parseColor("#F1F5F9"));

        buildingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buildingPaint.setColor(Color.parseColor("#E2E8F0"));
        buildingPaint.setStyle(Paint.Style.FILL);

        streetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        streetPaint.setColor(Color.parseColor("#CBD5E1"));
        streetPaint.setStyle(Paint.Style.STROKE);
        streetPaint.setStrokeWidth(2f * density);

        majorRoadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        majorRoadPaint.setColor(Color.WHITE);
        majorRoadPaint.setStyle(Paint.Style.STROKE);
        majorRoadPaint.setStrokeWidth(5f * density);

        highwayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        highwayPaint.setColor(Color.parseColor("#FFE4B5"));
        highwayPaint.setStyle(Paint.Style.STROKE);
        highwayPaint.setStrokeWidth(6.5f * density);

        parkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        parkPaint.setColor(Color.parseColor("#E6F4EA"));
        parkPaint.setStyle(Paint.Style.FILL);

        waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        waterPaint.setColor(Color.parseColor("#E0F2FE"));
        waterPaint.setStyle(Paint.Style.FILL);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#94A3B8"));
        labelPaint.setTextSize(9f * density);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        // User Location Paints
        userRadiusPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userRadiusPaint.setColor(Color.argb(35, 30, 64, 175));

        userLocationOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userLocationOuterPaint.setColor(Color.WHITE);
        userLocationOuterPaint.setStyle(Paint.Style.FILL);

        userLocationInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        userLocationInnerPaint.setColor(primaryColor);
        userLocationInnerPaint.setStyle(Paint.Style.FILL);

        // Pin Paints
        pinBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinBgPaint.setStyle(Paint.Style.FILL);

        pinBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinBorderPaint.setColor(Color.WHITE);
        pinBorderPaint.setStyle(Paint.Style.STROKE);
        pinBorderPaint.setStrokeWidth(2f * density);

        pinTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinTextPaint.setColor(Color.WHITE);
        pinTextPaint.setTextSize(10f * density);
        pinTextPaint.setFakeBoldText(true);
        pinTextPaint.setTextAlign(Paint.Align.CENTER);

        pinSelectedHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pinSelectedHaloPaint.setStyle(Paint.Style.STROKE);
        pinSelectedHaloPaint.setStrokeWidth(3f * density);
        pinSelectedHaloPaint.setColor(primaryColor);

        loadDemoMarkers();
    }

    public void setOnShopSelectedListener(OnShopSelectedListener listener) {
        this.listener = listener;
    }

    private void loadDemoMarkers() {
        markers.clear();

        // 1. Apex Digital Hub (🟢 Fair Price - 1.2 km)
        ShopMarker m1 = new ShopMarker("1", "Apex Digital Hub", "Electronics", "Indiranagar, Bangalore", "1.2 km away", 1.2,
                "Samsung Galaxy S24", "₹74,999", 74999, "🟢 Fair Price", 2, R.color.price_fair, 0.62f, 0.38f);
        m1.isSelected = true;
        markers.add(m1);

        // 2. Tech World (🟡 Slightly Expensive - 2.1 km)
        ShopMarker m2 = new ShopMarker("2", "Tech World", "Electronics", "MG Road, Bangalore", "2.1 km away", 2.1,
                "Samsung Galaxy S24", "₹76,499", 76499, "🟡 Slightly Expensive", 3, R.color.price_slightly_expensive, 0.35f, 0.30f);
        markers.add(m2);

        // 3. Digital Point (🔴 Overpriced - 2.8 km)
        ShopMarker m3 = new ShopMarker("3", "Digital Point", "Electronics", "Koramangala, Bangalore", "2.8 km away", 2.8,
                "Samsung Galaxy S24", "₹78,999", 78999, "🔴 Overpriced", 4, R.color.price_overpriced, 0.28f, 0.72f);
        markers.add(m3);

        // 4. Mobile Hub (⭐ Best Deal Nearby - 3.4 km)
        ShopMarker m4 = new ShopMarker("4", "Mobile Hub", "Electronics", "Whitefield, Bangalore", "3.4 km away", 3.4,
                "Samsung Galaxy S24", "₹73,999", 73999, "⭐ Best Deal Nearby", 1, R.color.price_best_deal, 0.74f, 0.65f);
        markers.add(m4);

        // 5. ElectroMart (🟢 Fair Price - 4.1 km)
        ShopMarker m5 = new ShopMarker("5", "ElectroMart", "Electronics", "HSR Layout, Bangalore", "4.1 km away", 4.1,
                "Samsung Galaxy S24", "₹75,499", 75499, "🟢 Fair Price", 2, R.color.price_fair, 0.82f, 0.40f);
        markers.add(m5);

        // Additional Category Markers for Category Filter
        ShopMarker m6 = new ShopMarker("6", "Indiranagar Home Rentals", "Rent & Real Estate", "Indiranagar, Bangalore", "1.2 km away", 1.2,
                "2 BHK Flat Rent", "₹38,000 /mo", 38000, "🟢 Fair Price", 2, R.color.price_fair, 0.48f, 0.22f);
        markers.add(m6);

        ShopMarker m7 = new ShopMarker("7", "Apex Motorworks", "Vehicles", "Indiranagar, Bangalore", "1.5 km away", 1.5,
                "Royal Enfield 350", "₹1,42,000", 142000, "⭐ Best Deal Nearby", 1, R.color.price_best_deal, 0.18f, 0.48f);
        markers.add(m7);

        ShopMarker m8 = new ShopMarker("8", "Apollo Pharmacy", "Healthcare", "Domlur, Bangalore", "0.6 km away", 0.6,
                "Health Checkup Kit", "₹1,790", 1790, "⭐ Best Deal Nearby", 1, R.color.price_best_deal, 0.52f, 0.58f);
        markers.add(m8);

        ShopMarker m9 = new ShopMarker("9", "Spice Route Kitchen", "Restaurants", "Indiranagar, Bangalore", "0.4 km away", 0.4,
                "Veg Thali Combo", "₹620", 620, "🟢 Fair Price", 2, R.color.price_fair, 0.42f, 0.42f);
        markers.add(m9);

        ShopMarker m10 = new ShopMarker("10", "CodeAcademy Center", "Education", "Koramangala, Bangalore", "1.0 km away", 1.0,
                "Java Programming Course", "₹23,000", 23000, "🟢 Fair Price", 2, R.color.price_fair, 0.58f, 0.78f);
        markers.add(m10);
    }

    public List<ShopMarker> getMarkers() {
        return markers;
    }

    public void selectMarker(String shopId) {
        ShopMarker selected = null;
        for (ShopMarker m : markers) {
            if (m.id.equals(shopId)) {
                m.isSelected = true;
                selected = m;
            } else {
                m.isSelected = false;
            }
        }
        invalidate();
        if (selected != null && listener != null) {
            listener.onShopSelected(selected);
        }
    }

    public void selectMarkerByName(String shopName) {
        if (shopName == null) return;
        ShopMarker selected = null;
        for (ShopMarker m : markers) {
            if (shopName.toLowerCase().contains(m.name.toLowerCase()) || m.name.toLowerCase().contains(shopName.toLowerCase())) {
                m.isSelected = true;
                selected = m;
            } else {
                m.isSelected = false;
            }
        }
        if (selected != null) {
            invalidate();
            if (listener != null) {
                listener.onShopSelected(selected);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultHeight = (int) (300 * density);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(defaultHeight, heightSize);
        } else {
            height = defaultHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        // 1. Draw Map Background Canvas
        canvas.drawRect(0, 0, width, height, bgPaint);

        // 2. Draw Vector Parks & Lake / Water zones
        drawParkZone(canvas, width * 0.05f, height * 0.1f, width * 0.28f, height * 0.35f);
        drawParkZone(canvas, width * 0.72f, height * 0.12f, width * 0.95f, height * 0.38f);
        drawWaterZone(canvas, width * 0.40f, height * 0.78f, width * 0.68f, height * 0.94f);

        // 3. Draw Street Grid
        drawStreetNetwork(canvas, width, height);

        // 4. Draw User Location Indicator (Center-Left: ~0.45, 0.50)
        float userX = width * 0.48f;
        float userY = height * 0.50f;

        // Radius circle
        canvas.drawCircle(userX, userY, 48f * density, userRadiusPaint);

        // Outer white ring
        canvas.drawCircle(userX, userY, 10f * density, userLocationOuterPaint);
        // Inner blue dot
        canvas.drawCircle(userX, userY, 6f * density, userLocationInnerPaint);

        // User Pill Label
        drawUserPill(canvas, userX, userY - 14f * density);

        // 5. Draw Shop Markers
        for (ShopMarker marker : markers) {
            float markerX = width * marker.relX;
            float markerY = height * marker.relY;

            drawShopPin(canvas, marker, markerX, markerY);
        }
    }

    private void drawParkZone(Canvas canvas, float left, float top, float right, float bottom) {
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 16f * density, 16f * density, parkPaint);
        canvas.drawText("Cubbon Park", rect.centerX(), rect.centerY() + 3f * density, labelPaint);
    }

    private void drawWaterZone(Canvas canvas, float left, float top, float right, float bottom) {
        RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 20f * density, 20f * density, waterPaint);
        canvas.drawText("Ulsoor Lake", rect.centerX(), rect.centerY() + 3f * density, labelPaint);
    }

    private void drawStreetNetwork(Canvas canvas, float width, float height) {
        // Minor local streets
        for (int i = 1; i <= 6; i++) {
            float y = (height / 7) * i;
            canvas.drawLine(0, y, width, y, streetPaint);
        }
        for (int i = 1; i <= 6; i++) {
            float x = (width / 7) * i;
            canvas.drawLine(x, 0, x, height, streetPaint);
        }

        // Major avenues (White roads)
        canvas.drawLine(0, height * 0.42f, width, height * 0.42f, majorRoadPaint);
        canvas.drawLine(width * 0.38f, 0, width * 0.38f, height, majorRoadPaint);
        canvas.drawLine(width * 0.70f, 0, width * 0.70f, height, majorRoadPaint);

        // Arterial Highway (Diagonal curved highway)
        Path highwayPath = new Path();
        highwayPath.moveTo(0, height * 0.15f);
        highwayPath.quadTo(width * 0.5f, height * 0.35f, width, height * 0.85f);
        canvas.drawPath(highwayPath, highwayPaint);
    }

    private void drawUserPill(Canvas canvas, float cx, float cy) {
        String text = "📍 You (Bangalore)";
        float textWidth = pinTextPaint.measureText(text);
        float pillW = textWidth + 12f * density;
        float pillH = 18f * density;

        RectF pillRect = new RectF(cx - pillW / 2f, cy - pillH, cx + pillW / 2f, cy);
        pinBgPaint.setColor(primaryColor);
        canvas.drawRoundRect(pillRect, 9f * density, 9f * density, pinBgPaint);
        canvas.drawRoundRect(pillRect, 9f * density, 9f * density, pinBorderPaint);

        canvas.drawText(text, cx, cy - 5f * density, pinTextPaint);
    }

    private void drawShopPin(Canvas canvas, ShopMarker marker, float cx, float cy) {
        int markerColor;
        switch (marker.statusConcept) {
            case 1: // Best Deal
                markerColor = bestDealColor;
                break;
            case 3: // Slightly Expensive
                markerColor = slightlyExpensiveColor;
                break;
            case 4: // Overpriced
                markerColor = overpricedColor;
                break;
            case 2: // Fair Price
            default:
                markerColor = fairColor;
                break;
        }

        String priceText = marker.price;
        String nameText = marker.name;

        float priceTextW = pinTextPaint.measureText(priceText);
        float pillW = priceTextW + 16f * density;
        float pillH = 22f * density;

        // Selected halo
        if (marker.isSelected) {
            canvas.drawCircle(cx, cy, 14f * density, pinSelectedHaloPaint);
        }

        // Pin Stem & Base Circle
        pinBgPaint.setColor(markerColor);
        canvas.drawCircle(cx, cy, 7f * density, pinBgPaint);
        canvas.drawCircle(cx, cy, 7f * density, pinBorderPaint);

        // Upper Price Badge Bubble
        float bubbleBottom = cy - 8f * density;
        float bubbleTop = bubbleBottom - pillH;
        RectF bubbleRect = new RectF(cx - pillW / 2f, bubbleTop, cx + pillW / 2f, bubbleBottom);

        canvas.drawRoundRect(bubbleRect, 11f * density, 11f * density, pinBgPaint);
        canvas.drawRoundRect(bubbleRect, 11f * density, 11f * density, pinBorderPaint);

        canvas.drawText(priceText, cx, bubbleBottom - 6f * density, pinTextPaint);

        // Shop Name Label below marker
        textPaint.setColor(marker.isSelected ? Color.parseColor("#0F172A") : Color.parseColor("#475569"));
        textPaint.setFakeBoldText(marker.isSelected);
        canvas.drawText(nameText, cx, cy + 14f * density, textPaint);
    }

    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG) {{
        setTextSize(10f * density);
        setTextAlign(Paint.Align.CENTER);
    }};

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();
            float width = getWidth();
            float height = getHeight();

            float touchRadius = 32f * density;

            for (ShopMarker m : markers) {
                float markerX = width * m.relX;
                float markerY = height * m.relY;

                float dx = touchX - markerX;
                float dy = touchY - markerY;
                if (Math.sqrt(dx * dx + dy * dy) <= touchRadius) {
                    selectMarker(m.id);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
