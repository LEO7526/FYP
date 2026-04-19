package com.example.yummyrestaurant.models;

public class MyBooking {
    private final int bid;
    private final int cid;
    private final String bookingName;
    private final String bookingTel;
    private final int tableId;
    private final String bookingDate;
    private final String bookingTime;
    private final int peopleCount;
    private final String purpose;
    private final String remark;
    private final int status;

    public MyBooking(int bid, int cid, String bookingName, String bookingTel, int tableId,
                     String bookingDate, String bookingTime, int peopleCount,
                     String purpose, String remark, int status) {
        this.bid = bid;
        this.cid = cid;
        this.bookingName = bookingName;
        this.bookingTel = bookingTel;
        this.tableId = tableId;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.peopleCount = peopleCount;
        this.purpose = purpose;
        this.remark = remark;
        this.status = status;
    }

    public int getBid() {
        return bid;
    }

    public int getCid() {
        return cid;
    }

    public String getBookingName() {
        return bookingName;
    }

    public String getBookingTel() {
        return bookingTel;
    }

    public int getTableId() {
        return tableId;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getBookingTime() {
        return bookingTime;
    }

    public int getPeopleCount() {
        return peopleCount;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getRemark() {
        return remark;
    }

    public int getStatus() {
        return status;
    }

    public boolean isCancelled() {
        return status == 0;
    }
}
