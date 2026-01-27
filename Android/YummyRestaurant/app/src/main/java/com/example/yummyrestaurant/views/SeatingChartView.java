package com.example.yummyrestaurant.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.yummyrestaurant.models.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom View for rendering an interactive seating chart
 * 
 * Features:
 * - Responsive design (adapts to screen size)
 * - Color-coded table status (available, occupied, reserved)
 * - Touch interaction to select/deselect tables
 * - Visual feedback for selected table
 * - Dynamic sizing based on screen dimensions
 * 
 * Author: YummyRestaurant
 * Version: 1.0
 */
public class SeatingChartView extends View {

    private List<Table> tables = new ArrayList<>();
    private Map<Integer, RectF> tableRectMap = new HashMap<>();
    private int selectedTableId = -1;
    private OnTableSelectedListener onTableSelectedListener;

    // Layout configuration
    private static final float MARGIN_PERCENT = 5f;      // 5% margin from edges
    private static final float TABLE_SIZE_PERCENT = 4f;   // 4% of width per table
    private static final float MIN_TABLE_SIZE_DP = 40f;   // Minimum 40dp
    private static final float MAX_TABLE_SIZE_DP = 60f;   // Maximum 60dp
    private static final float MIN_MARGIN_PERCENT = 2f;   // Minimum 2% margin to ensure all tables visible

    // Colors
    private static final int COLOR_AVAILABLE = Color.parseColor("#4CAF50");    // Green
    private static final int COLOR_OCCUPIED = Color.parseColor("#F44336");     // Red
    private static final int COLOR_RESERVED = Color.parseColor("#FF9800");     // Orange
    private static final int COLOR_SELECTED = Color.parseColor("#2196F3");     // Blue
    private static final int COLOR_UNSUITABLE = Color.parseColor("#BDBDBD");   // Grey (too small)
    private static final int COLOR_TEXT = Color.WHITE;
    private static final int COLOR_BORDER = Color.parseColor("#333333");

    // Paint objects
    private Paint tablePaint;
    private Paint selectedPaint;
    private Paint textPaint;
    private Paint borderPaint;
    private Paint backgroundPaint;

    // Screen dimensions (in dp)
    private float displayDensity;

    public SeatingChartView(Context context) {
        super(context);
        init();
    }

    public SeatingChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SeatingChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize paint objects and display density
     */
    private void init() {
        displayDensity = getContext().getResources().getDisplayMetrics().density;

        // Background paint
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor("#F5F5F5"));
        backgroundPaint.setStyle(Paint.Style.FILL);

        // Table paint
        tablePaint = new Paint();
        tablePaint.setStyle(Paint.Style.FILL);
        tablePaint.setAntiAlias(true);

        // Selected table paint
        selectedPaint = new Paint();
        selectedPaint.setColor(COLOR_SELECTED);
        selectedPaint.setStyle(Paint.Style.FILL);
        selectedPaint.setAntiAlias(true);

        // Border paint
        borderPaint = new Paint();
        borderPaint.setColor(COLOR_BORDER);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2);
        borderPaint.setAntiAlias(true);

        // Text paint
        textPaint = new Paint();
        textPaint.setColor(COLOR_TEXT);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    /**
     * Override onMeasure to support bi-directional scrolling
     * Set both width and height to enable scrolling in NestedScrollView and HorizontalScrollView
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get the suggested dimensions from parent
        int suggestedWidth = MeasureSpec.getSize(widthMeasureSpec);
        int suggestedHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        // For bi-directional scrolling support:
        // Width: Set to at least 1.6x the suggested width to enable more horizontal scrolling
        // Height: Set to 0.85x the width to enable vertical scrolling
        
        int desiredWidth = (int)(suggestedWidth * 1.6f);   // 1.6x for more horizontal scroll
        int desiredHeight = (int)(desiredWidth * 0.85f);   // Maintain aspect ratio
        
        // Ensure we have at least the suggested dimensions
        if (desiredWidth < suggestedWidth) desiredWidth = suggestedWidth;
        if (desiredHeight < suggestedHeight) desiredHeight = suggestedHeight;
        
        int widthSpec = MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY);
        
        super.onMeasure(widthSpec, heightSpec);
    }

    /**
     * Set the tables to display on the seating chart
     */
    public void setTables(List<Table> tables) {
        this.tables = new ArrayList<>(tables);
        tableRectMap.clear();
        invalidate();
    }

    /**
     * Get the currently selected table
     */
    public Table getSelectedTable() {
        for (Table table : tables) {
            if (table.getTid() == selectedTableId) {
                return table;
            }
        }
        return null;
    }

    /**
     * Set the selected table by ID
     */
    public void setSelectedTable(int tableId) {
        this.selectedTableId = tableId;
        invalidate();
    }

    /**
     * Set listener for table selection events
     */
    public void setOnTableSelectedListener(OnTableSelectedListener listener) {
        this.onTableSelectedListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);

        if (tables.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        // Calculate responsive margin - ensure all tables fit on screen
        // Find min and max coordinates to calculate required space
        float minX = 100f, maxX = 0f;
        float minY = 100f, maxY = 0f;
        
        for (Table table : tables) {
            minX = Math.min(minX, table.getX());
            maxX = Math.max(maxX, table.getX());
            minY = Math.min(minY, table.getY());
            maxY = Math.max(maxY, table.getY());
        }
        
        float rangeX = maxX - minX;  // Should be ~80 (from 10 to 90)
        float rangeY = maxY - minY;  // Should be ~75 (from 10 to 85)

        // Calculate table size (responsive)
        float tableSizePx = Math.max(
            MIN_TABLE_SIZE_DP * displayDensity,
            Math.min(
                MAX_TABLE_SIZE_DP * displayDensity,
                (getWidth() * TABLE_SIZE_PERCENT) / 100f
            )
        );

        // Calculate margins dynamically to ensure all tables fit
        // We need to fit tables from minX to maxX (80% of layout) into available width
        float availableWidth = getWidth() - tableSizePx;
        float marginLeft = (availableWidth * minX) / (rangeX + 4);  // +4 for padding
        
        float availableHeight = getHeight() - tableSizePx;
        float marginTop = (availableHeight * minY) / (rangeY + 4);   // +4 for padding

        // Draw each table
        for (Table table : tables) {
            drawTable(canvas, table, marginLeft, marginTop, availableWidth, availableHeight, tableSizePx, rangeX, rangeY);
        }
    }

    /**
     * Draw a single table
     */
    private void drawTable(Canvas canvas, Table table, float marginLeft, float marginTop,
                          float availableWidth, float availableHeight, float tableSize,
                          float rangeX, float rangeY) {
        
        // Calculate position (x and y are percentages, map to screen space)
        // Normalize coordinates relative to min values (10, 10) to range space (80, 75)
        float normalizedX = (table.getX() - 10) / rangeX;  // 0 to 1
        float normalizedY = (table.getY() - 10) / rangeY;  // 0 to 1
        
        float x = marginLeft + (availableWidth * normalizedX);
        float y = marginTop + (availableHeight * normalizedY);

        // Adjust position to center the table
        float left = x - (tableSize / 2f);
        float top = y - (tableSize / 2f);
        float right = x + (tableSize / 2f);
        float bottom = y + (tableSize / 2f);

        RectF tableRect = new RectF(left, top, right, bottom);
        tableRectMap.put(table.getTid(), tableRect);

        // Determine table color based on status and booking suitability
        int tableColor = getTableColor(table);
        tablePaint.setColor(tableColor);

        // Draw table background
        canvas.drawRoundRect(tableRect, 8, 8, tablePaint);

        // Draw selection highlight if selected
        if (table.getTid() == selectedTableId) {
            selectedPaint.setColor(COLOR_SELECTED);
            RectF outerRect = new RectF(
                left - 4, top - 4,
                right + 4, bottom + 4
            );
            canvas.drawRoundRect(outerRect, 10, 10, selectedPaint);
            
            // Redraw table on top
            canvas.drawRoundRect(tableRect, 8, 8, tablePaint);
        }

        // Draw border
        canvas.drawRoundRect(tableRect, 8, 8, borderPaint);

        // Draw table number and capacity
        float textSize = tableSize * 0.4f;
        textPaint.setTextSize(textSize);

        String tableText = "T" + table.getTid();
        float textX = x;
        float textY = y - (textSize / 4f);

        canvas.drawText(tableText, textX, textY, textPaint);

        // Draw capacity info (smaller text)
        float capacitySize = tableSize * 0.3f;
        textPaint.setTextSize(capacitySize);
        String capacityText = table.getCapacity() + "p";
        float capacityY = y + (capacitySize / 2f);

        canvas.drawText(capacityText, textX, capacityY, textPaint);
    }

    /**
     * Draw empty state when no tables are available
     */
    private void drawEmptyState(Canvas canvas) {
        textPaint.setTextSize(48);
        String emptyText = "No tables available";
        canvas.drawText(emptyText, getWidth() / 2f, getHeight() / 2f, textPaint);
    }

    /**
     * Get color for table status and booking suitability
     */
    private int getColorForStatus(String status) {
        if ("occupied".equals(status)) {
            return COLOR_OCCUPIED;
        } else if ("reserved".equals(status)) {
            return COLOR_RESERVED;
        } else if ("available".equals(status)) {
            return COLOR_AVAILABLE;
        }
        return COLOR_AVAILABLE;
    }

    /**
     * Get table color, considering both status and suitability for booking
     */
    private int getTableColor(Table table) {
        // If occupied or reserved, use those colors
        if ("occupied".equals(table.getStatus()) || "reserved".equals(table.getStatus())) {
            return getColorForStatus(table.getStatus());
        }
        
        // If available but not suitable for booking (capacity too small), show grey
        if (table.isAvailable() && !table.isSuitableForBooking()) {
            return COLOR_UNSUITABLE;
        }
        
        // Otherwise use status color
        return getColorForStatus(table.getStatus());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            // Check if any table was clicked
            for (Map.Entry<Integer, RectF> entry : tableRectMap.entrySet()) {
                int tableId = entry.getKey();
                RectF rect = entry.getValue();

                if (rect.contains(x, y)) {
                    // Find the table object
                    Table clickedTable = null;
                    for (Table table : tables) {
                        if (table.getTid() == tableId) {
                            clickedTable = table;
                            break;
                        }
                    }

                    // Only allow selection of suitable tables for booking
                    if (clickedTable != null && clickedTable.isSuitableForBooking()) {
                        selectedTableId = tableId;
                        invalidate();

                        // Notify listener
                        if (onTableSelectedListener != null) {
                            onTableSelectedListener.onTableSelected(clickedTable);
                        }
                        return true;
                    } else {
                        // Table not suitable for booking
                        if (onTableSelectedListener != null) {
                            onTableSelectedListener.onTableUnavailable(tableId);
                        }
                        return true;
                    }
                }
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * Interface for table selection callbacks
     */
    public interface OnTableSelectedListener {
        void onTableSelected(Table table);
        void onTableUnavailable(int tableId);
    }
}
