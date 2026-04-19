package com.example.yummyrestaurant.inventory;

import java.util.ArrayList;
import java.util.List;

public class ShortageImpactItem {
    public static class ActivityLog {
        public int logId;
        public String logDate;
        public String logType;
        public String details;

        public ActivityLog(int logId, String logDate, String logType, String details) {
            this.logId = logId;
            this.logDate = logDate;
            this.logType = logType;
            this.details = details;
        }
    }

    public int mid;
    public String ingredientName;
    public String unit;
    public double currentQty;
    public double reorderLevel;
    public double weeklyConsumed;
    public double avgDailyConsumed;
    public int recentActivityCount;
    public String latestLogType;
    public String latestLogDate;
    public String latestLogDetails;
    public List<ActivityLog> recentLogs = new ArrayList<>();

    public ShortageImpactItem(int mid, String ingredientName, String unit, double currentQty,
                              double reorderLevel, double weeklyConsumed, double avgDailyConsumed,
                              int recentActivityCount, String latestLogType, String latestLogDate,
                              String latestLogDetails) {
        this.mid = mid;
        this.ingredientName = ingredientName;
        this.unit = unit;
        this.currentQty = currentQty;
        this.reorderLevel = reorderLevel;
        this.weeklyConsumed = weeklyConsumed;
        this.avgDailyConsumed = avgDailyConsumed;
        this.recentActivityCount = recentActivityCount;
        this.latestLogType = latestLogType;
        this.latestLogDate = latestLogDate;
        this.latestLogDetails = latestLogDetails;
    }
}