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

    private enum TableShape {
        RECT,
        OVAL,
        CIRCLE
    }

    private static class TableLayout {
        final float xPercent;
        final float yPercent;
        final TableShape shape;

        TableLayout(float xPercent, float yPercent, TableShape shape) {
            this.xPercent = xPercent;
            this.yPercent = yPercent;
            this.shape = shape;
        }
    }

    private static final Map<Integer, TableLayout> FLOOR_LAYOUT = new HashMap<>();

    private static final float FLOOR_MIN_X = 14f;
    private static final float FLOOR_MAX_X = 76f;
    private static final float FLOOR_MIN_Y = 16f;
    private static final float FLOOR_MAX_Y = 70f;

    static {
        FLOOR_LAYOUT.put(36, new TableLayout(14f, 16f, TableShape.RECT));
        FLOOR_LAYOUT.put(35, new TableLayout(14f, 26f, TableShape.RECT));
        FLOOR_LAYOUT.put(34, new TableLayout(14f, 36f, TableShape.RECT));
        FLOOR_LAYOUT.put(33, new TableLayout(14f, 46f, TableShape.RECT));
        FLOOR_LAYOUT.put(32, new TableLayout(14f, 56f, TableShape.RECT));
        FLOOR_LAYOUT.put(31, new TableLayout(14f, 70f, TableShape.RECT));

        FLOOR_LAYOUT.put(23, new TableLayout(36f, 20f, TableShape.CIRCLE));
        FLOOR_LAYOUT.put(13, new TableLayout(56f, 20f, TableShape.CIRCLE));

        FLOOR_LAYOUT.put(22, new TableLayout(36f, 36f, TableShape.RECT));
        FLOOR_LAYOUT.put(21, new TableLayout(36f, 50f, TableShape.RECT));
        FLOOR_LAYOUT.put(20, new TableLayout(36f, 68f, TableShape.RECT));

        FLOOR_LAYOUT.put(12, new TableLayout(56f, 36f, TableShape.RECT));
        FLOOR_LAYOUT.put(11, new TableLayout(56f, 50f, TableShape.RECT));
        FLOOR_LAYOUT.put(10, new TableLayout(56f, 68f, TableShape.RECT));

        FLOOR_LAYOUT.put(4, new TableLayout(76f, 30f, TableShape.RECT));
        FLOOR_LAYOUT.put(3, new TableLayout(76f, 42f, TableShape.OVAL));
        FLOOR_LAYOUT.put(2, new TableLayout(76f, 54f, TableShape.OVAL));
        FLOOR_LAYOUT.put(1, new TableLayout(76f, 66f, TableShape.OVAL));
    }

    private List<Table> tables = new ArrayList<>();
    private Map<Integer, RectF> tableRectMap = new HashMap<>();
    private int selectedTableId = -1;
    private OnTableSelectedListener onTableSelectedListener;

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
    private Paint guidePaint;
    private Paint guideTextPaint;
    private Paint wallFillPaint;
    private Paint windowInnerLinePaint;
    private Paint windowFillPaint;

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

        guidePaint = new Paint();
        guidePaint.setColor(Color.parseColor("#212121"));
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(2f * displayDensity);
        guidePaint.setAntiAlias(true);

        guideTextPaint = new Paint();
        guideTextPaint.setColor(Color.parseColor("#424242"));
        guideTextPaint.setStyle(Paint.Style.FILL);
        guideTextPaint.setAntiAlias(true);
        guideTextPaint.setTextSize(13f * displayDensity);

        wallFillPaint = new Paint();
        wallFillPaint.setColor(Color.parseColor("#8B5A2B"));
        wallFillPaint.setStyle(Paint.Style.FILL);
        wallFillPaint.setAntiAlias(true);

        windowInnerLinePaint = new Paint();
        windowInnerLinePaint.setColor(Color.parseColor("#1E88E5"));
        windowInnerLinePaint.setStyle(Paint.Style.STROKE);
        windowInnerLinePaint.setStrokeWidth(1f * displayDensity);
        windowInnerLinePaint.setAntiAlias(true);

        windowFillPaint = new Paint();
        windowFillPaint.setColor(Color.parseColor("#90CAF9"));
        windowFillPaint.setStyle(Paint.Style.FILL);
        windowFillPaint.setAntiAlias(true);
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
        tableRectMap.clear();

        RectF chartArea = new RectF(
            getWidth() * 0.06f,
            getHeight() * 0.15f,
            getWidth() * 0.94f,
            getHeight() * 0.86f
        );

        drawFloorPlanReference(canvas, chartArea);

        if (tables.isEmpty()) {
            drawEmptyState(canvas);
            return;
        }

        // Draw each table
        for (Table table : tables) {
            drawTable(canvas, table, chartArea);
        }
    }

    /**
     * Draw a single table
     */
    private void drawTable(Canvas canvas, Table table, RectF chartArea) {
        TableLayout layout = FLOOR_LAYOUT.get(table.getTid());
        if (layout == null) {
            return;
        }

        float contentLeft = chartArea.left + (chartArea.width() * 0.10f);
        float contentRight = chartArea.right - (chartArea.width() * 0.10f);
        float contentTop = chartArea.top + (chartArea.height() * 0.10f);
        float contentBottom = chartArea.bottom - (chartArea.height() * 0.18f);

        float normalizedX = (layout.xPercent - FLOOR_MIN_X) / (FLOOR_MAX_X - FLOOR_MIN_X);
        float normalizedY = (layout.yPercent - FLOOR_MIN_Y) / (FLOOR_MAX_Y - FLOOR_MIN_Y);

        float cx = contentLeft + ((contentRight - contentLeft) * normalizedX);
        float cy = contentTop + ((contentBottom - contentTop) * normalizedY);

        float base = Math.min(chartArea.width(), chartArea.height());
        float rectWidth = base * 0.12f;
        float rectHeight = base * 0.08f;
        float ovalWidth = base * 0.125f;
        float ovalHeight = base * 0.09f;
        float circleDiameter = base * 0.115f;

        float width;
        float height;
        if (layout.shape == TableShape.CIRCLE) {
            width = circleDiameter;
            height = circleDiameter;
        } else if (layout.shape == TableShape.OVAL) {
            width = ovalWidth;
            height = ovalHeight;
        } else {
            width = rectWidth;
            height = rectHeight;
        }

        float left = cx - (width / 2f);
        float top = cy - (height / 2f);
        float right = cx + (width / 2f);
        float bottom = cy + (height / 2f);

        RectF tableRect = new RectF(left, top, right, bottom);
        tableRectMap.put(table.getTid(), tableRect);

        // Determine table color based on status and booking suitability
        int tableColor = getTableColor(table);
        tablePaint.setColor(tableColor);

        drawTableShape(canvas, tableRect, layout.shape, tablePaint);

        // Draw selection highlight if selected
        if (table.getTid() == selectedTableId) {
            selectedPaint.setColor(COLOR_SELECTED);
            RectF outerRect = new RectF(
                left - 4, top - 4,
                right + 4, bottom + 4
            );
            drawTableShape(canvas, outerRect, layout.shape, selectedPaint);
            
            // Redraw table on top
            drawTableShape(canvas, tableRect, layout.shape, tablePaint);
        }

        // Draw border
        drawTableShape(canvas, tableRect, layout.shape, borderPaint);

        // Draw table number and capacity
        float textSize = height * 0.42f;
        textPaint.setTextSize(textSize);

        String tableText = "T" + table.getTid();
        float textX = cx;
        float textY = cy - (textSize / 5f);

        canvas.drawText(tableText, textX, textY, textPaint);

        // Draw capacity info (smaller text)
        float capacitySize = height * 0.28f;
        textPaint.setTextSize(capacitySize);
        String capacityText = table.getCapacity() + "p";
        float capacityY = cy + (capacitySize * 0.85f);

        canvas.drawText(capacityText, textX, capacityY, textPaint);
    }

    private void drawTableShape(Canvas canvas, RectF rect, TableShape shape, Paint paint) {
        if (shape == TableShape.CIRCLE) {
            float radius = Math.min(rect.width(), rect.height()) / 2f;
            canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint);
        } else {
            canvas.drawRoundRect(rect, rect.height() * 0.25f, rect.height() * 0.25f, paint);
        }
    }

    private void drawFloorPlanReference(Canvas canvas, RectF chartArea) {
        float doorGapTop = chartArea.bottom - (chartArea.height() * 0.14f);
        float doorGapBottom = chartArea.bottom - (chartArea.height() * 0.04f);
        float rightWallX = chartArea.right;
        float leftWallX = chartArea.left + chartArea.width() * 0.04f;

        RectF leftWallArea = new RectF(
            chartArea.left,
            chartArea.top,
            leftWallX,
            chartArea.bottom
        );
        canvas.drawRect(leftWallArea, wallFillPaint);

        // Outer boundary with right-side door opening
        canvas.drawLine(chartArea.left, chartArea.top, rightWallX, chartArea.top, guidePaint);
        canvas.drawLine(chartArea.left, chartArea.top, chartArea.left, chartArea.bottom, guidePaint);
        canvas.drawLine(chartArea.left, chartArea.bottom, rightWallX, chartArea.bottom, guidePaint);
        canvas.drawLine(rightWallX, chartArea.top, rightWallX, doorGapTop, guidePaint);
        canvas.drawLine(rightWallX, doorGapBottom, rightWallX, chartArea.bottom, guidePaint);

        canvas.drawLine(leftWallX, chartArea.top, leftWallX, chartArea.bottom, guidePaint);

        guideTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Wall", chartArea.left , chartArea.top-(36f  * displayDensity), guideTextPaint);
        // Wall marker line (left side)
        canvas.drawLine(chartArea.left + (9f * displayDensity), chartArea.top , chartArea.left + (9f * displayDensity), chartArea.top - (32f * displayDensity), guidePaint);



        guideTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Window", rightWallX - (18f * displayDensity), chartArea.top - (20f * displayDensity), guideTextPaint);
        // Window marker line (right upper wall)
        float windowMarkerY = chartArea.top + (18f * displayDensity);
        canvas.drawLine(rightWallX + (4f * displayDensity), windowMarkerY, rightWallX + (16f * displayDensity), windowMarkerY, guidePaint);
        float windowTopY = chartArea.top - (18f * displayDensity);
        float windowLeftX = rightWallX - (16f * displayDensity);
        float windowRightX = rightWallX + (16f * displayDensity);
        canvas.drawLine(windowLeftX, chartArea.top, windowLeftX, windowTopY, guidePaint);
        canvas.drawLine(windowRightX, windowMarkerY, windowRightX, windowTopY, guidePaint);

        float windowInnerHorizontalY = (windowTopY + windowMarkerY) / 2f;
        float windowInnerVerticalX = (windowLeftX + windowRightX) / 2f;
        float windowInset = 3f * displayDensity;
        float windowBandTopY = windowInnerHorizontalY + 23f;
        float windowBandLeftX = chartArea.right - 18f;
        float windowBandBottomY = chartArea.bottom - 243f;
        RectF windowTopArea = new RectF(leftWallX, chartArea.top, chartArea.right, windowBandTopY);
        canvas.drawRect(windowTopArea, windowFillPaint);
        RectF windowArea = new RectF(windowBandLeftX, windowBandTopY, chartArea.right, windowBandBottomY);
        canvas.drawRect(windowArea, windowFillPaint);

        canvas.drawLine(
            chartArea.right,
            windowBandTopY,
            leftWallX,
            windowBandTopY,
            windowInnerLinePaint
        );
        canvas.drawLine(
            windowBandLeftX,
                windowBandTopY,
                windowBandLeftX,
            windowBandBottomY,
            windowInnerLinePaint
        );

        canvas.drawLine(
                windowBandLeftX,
                windowBandBottomY,
                chartArea.right,
                windowBandBottomY,
                windowInnerLinePaint
        );


        guideTextPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("Door", rightWallX - (58f * displayDensity), doorGapTop + (18f * displayDensity), guideTextPaint);

        RectF counterRect = new RectF(
                chartArea.right - (150f * displayDensity),
                chartArea.bottom - (60f * displayDensity),
                chartArea.right - (74f * displayDensity),
                chartArea.bottom - (5f * displayDensity)
        );
        canvas.drawRect(counterRect, guidePaint);

        guideTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Counter", counterRect.centerX(), counterRect.centerY() + (4f * displayDensity), guideTextPaint);

        // Door opening symbol: longer diagonal stroke connected to lower wall
        canvas.drawLine(
            rightWallX,
            chartArea.bottom -(24f*displayDensity),
            rightWallX - (24f * displayDensity),
            chartArea.bottom - (42f * displayDensity),
            guidePaint
        );
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
