package com.example.pricepulse;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PriceTrendChartView extends View {

    public static class DataPoint {
        public final String label;
        public final double price;
        public final String displayPrice;

        public DataPoint(String label, double price, String displayPrice) {
            this.label = label;
            this.price = price;
            this.displayPrice = displayPrice;
        }
    }

    private final List<DataPoint> dataPoints = new ArrayList<>();

    private Paint linePaint;
    private Paint fillPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint priceTagPaint;
    private Paint priceTagBgPaint;
    private Paint pointOuterPaint;
    private Paint pointInnerPaint;
    private Paint lowestPointPaint;

    private int primaryColor;
    private int fairColor;
    private int textColor;
    private int gridColor;

    private float density;

    public PriceTrendChartView(Context context) {
        super(context);
        init();
    }

    public PriceTrendChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PriceTrendChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;

        primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        fairColor = ContextCompat.getColor(getContext(), R.color.price_fair);
        textColor = ContextCompat.getColor(getContext(), R.color.text_secondary);
        gridColor = ContextCompat.getColor(getContext(), R.color.divider);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(primaryColor);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3f * density);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setPathEffect(new CornerPathEffect(8f * density));

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f * density);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{4f * density, 4f * density}, 0));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(11f * density);
        textPaint.setTextAlign(Paint.Align.CENTER);

        priceTagPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        priceTagPaint.setColor(Color.WHITE);
        priceTagPaint.setTextSize(10f * density);
        priceTagPaint.setTextAlign(Paint.Align.CENTER);
        priceTagPaint.setFakeBoldText(true);

        priceTagBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        priceTagBgPaint.setColor(primaryColor);
        priceTagBgPaint.setStyle(Paint.Style.FILL);

        pointOuterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointOuterPaint.setColor(Color.WHITE);
        pointOuterPaint.setStyle(Paint.Style.FILL);

        pointInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointInnerPaint.setColor(primaryColor);
        pointInnerPaint.setStyle(Paint.Style.FILL);

        lowestPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lowestPointPaint.setColor(fairColor);
        lowestPointPaint.setStyle(Paint.Style.FILL);

        loadDefaultData();
    }

    public void loadDefaultData() {
        dataPoints.clear();
        dataPoints.add(new DataPoint("Jan", 79999, "₹79,999"));
        dataPoints.add(new DataPoint("Feb", 78499, "₹78,499"));
        dataPoints.add(new DataPoint("Mar", 81999, "₹81,999"));
        dataPoints.add(new DataPoint("Apr", 76999, "₹76,999"));
        dataPoints.add(new DataPoint("May", 74999, "₹74,999"));
        dataPoints.add(new DataPoint("Jun", 72999, "₹72,999"));
        invalidate();
    }

    public void setData(List<DataPoint> points) {
        dataPoints.clear();
        if (points != null) {
            dataPoints.addAll(points);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defaultHeight = (int) (220 * density);
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

        if (dataPoints.isEmpty()) {
            return;
        }

        float width = getWidth();
        float height = getHeight();

        float paddingLeft = 44f * density;
        float paddingRight = 24f * density;
        float paddingTop = 32f * density;
        float paddingBottom = 30f * density;

        float chartWidth = width - paddingLeft - paddingRight;
        float chartHeight = height - paddingTop - paddingBottom;

        if (chartWidth <= 0 || chartHeight <= 0) {
            return;
        }

        // Find Min and Max
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        int lowestIndex = 0;
        int highestIndex = 0;

        for (int i = 0; i < dataPoints.size(); i++) {
            double price = dataPoints.get(i).price;
            if (price < minPrice) {
                minPrice = price;
                lowestIndex = i;
            }
            if (price > maxPrice) {
                maxPrice = price;
                highestIndex = i;
            }
        }

        // Add 15% head/foot room
        double priceRange = maxPrice - minPrice;
        if (priceRange <= 0) {
            priceRange = maxPrice * 0.1;
        }
        double visualMin = minPrice - priceRange * 0.15;
        double visualMax = maxPrice + priceRange * 0.15;
        double visualRange = visualMax - visualMin;

        // Draw 3 Grid Lines and Y-Axis Price Labels
        int gridLevels = 3;
        for (int i = 0; i <= gridLevels; i++) {
            float y = paddingTop + (chartHeight / gridLevels) * i;
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint);

            double labelPrice = visualMax - (visualRange / gridLevels) * i;
            String priceLabel = formatShortPrice(labelPrice);
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(priceLabel, paddingLeft - 8f * density, y + 4f * density, textPaint);
        }

        // Calculate (X, Y) Coordinates for points
        float[] xCoords = new float[dataPoints.size()];
        float[] yCoords = new float[dataPoints.size()];

        float stepX = chartWidth / (dataPoints.size() - 1);
        for (int i = 0; i < dataPoints.size(); i++) {
            xCoords[i] = paddingLeft + i * stepX;
            double price = dataPoints.get(i).price;
            yCoords[i] = (float) (paddingTop + (1.0 - (price - visualMin) / visualRange) * chartHeight);
        }

        // Create Path for Line and Gradient Fill
        Path linePath = new Path();
        Path fillPath = new Path();

        linePath.moveTo(xCoords[0], yCoords[0]);
        fillPath.moveTo(xCoords[0], height - paddingBottom);
        fillPath.lineTo(xCoords[0], yCoords[0]);

        for (int i = 1; i < dataPoints.size(); i++) {
            linePath.lineTo(xCoords[i], yCoords[i]);
            fillPath.lineTo(xCoords[i], yCoords[i]);
        }

        fillPath.lineTo(xCoords[dataPoints.size() - 1], height - paddingBottom);
        fillPath.close();

        // Apply Gradient Shader to Fill
        int alphaPrimary = Color.argb(45, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor));
        int transparentPrimary = Color.argb(0, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor));
        fillPaint.setShader(new LinearGradient(0, paddingTop, 0, height - paddingBottom,
                alphaPrimary, transparentPrimary, Shader.TileMode.CLAMP));

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Draw Data Points, X-Axis Month Labels, and Price Callout
        for (int i = 0; i < dataPoints.size(); i++) {
            float x = xCoords[i];
            float y = yCoords[i];
            DataPoint point = dataPoints.get(i);

            // Month Label at bottom
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(point.label, x, height - 8f * density, textPaint);

            // Points
            boolean isLowest = (i == lowestIndex);
            boolean isHighest = (i == highestIndex);

            float outerRadius = (isLowest || isHighest) ? 6f * density : 4.5f * density;
            float innerRadius = (isLowest || isHighest) ? 3.5f * density : 2.5f * density;

            canvas.drawCircle(x, y, outerRadius, pointOuterPaint);
            if (isLowest) {
                canvas.drawCircle(x, y, innerRadius, lowestPointPaint);
            } else {
                canvas.drawCircle(x, y, innerRadius, pointInnerPaint);
            }

            // Draw Price Callout Pill on Lowest (Best Price) and Highest (Peak)
            if (isLowest || isHighest || i == dataPoints.size() - 1) {
                drawPricePill(canvas, point.displayPrice, x, y - 10f * density, isLowest);
            }
        }
    }

    private void drawPricePill(Canvas canvas, String text, float cx, float cy, boolean isLowest) {
        float textWidth = priceTagPaint.measureText(text);
        float pillPaddingH = 6f * density;
        float pillHeight = 16f * density;
        float pillWidth = textWidth + pillPaddingH * 2;

        float left = cx - pillWidth / 2f;
        float top = cy - pillHeight;
        float right = cx + pillWidth / 2f;
        float bottom = cy;

        // Keep inside bounds
        if (left < 4f * density) {
            float shift = 4f * density - left;
            left += shift;
            right += shift;
        } else if (right > getWidth() - 4f * density) {
            float shift = right - (getWidth() - 4f * density);
            left -= shift;
            right -= shift;
        }

        RectF rect = new RectF(left, top, right, bottom);
        priceTagBgPaint.setColor(isLowest ? fairColor : primaryColor);
        canvas.drawRoundRect(rect, 4f * density, 4f * density, priceTagBgPaint);

        canvas.drawText(text, rect.centerX(), bottom - 4f * density, priceTagPaint);
    }

    private String formatShortPrice(double price) {
        if (price >= 100000) {
            return String.format("₹%.1fL", price / 100000.0);
        } else if (price >= 1000) {
            return String.format("₹%.0fk", price / 1000.0);
        } else {
            return String.format("₹%.0f", price);
        }
    }
}
