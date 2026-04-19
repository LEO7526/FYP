package com.example.yummyrestaurant.models;

/**
 * Model for booking information including table position and current status
 */
public class Booking {
    private int bid;
    private int tid;
    private String bkcname;
    private String bktel;
    private String bdate;
    private String btime;
    private int pnum;
    private int status; // 0=Cancelled, 1=Pending, 2=Done, 3=Cancelled
    private String purpose;
    private String remark;
    private int capacity;
    private float x_position;
    private float y_position;

    public Booking(int bid, int tid, String bkcname, String bktel, String bdate, String btime,
                   int pnum, int status, String purpose, String remark, int capacity,
                   float x_position, float y_position) {
        this.bid = bid;
        this.tid = tid;
        this.bkcname = bkcname;
        this.bktel = bktel;
        this.bdate = bdate;
        this.btime = btime;
        this.pnum = pnum;
        this.status = status;
        this.purpose = purpose;
        this.remark = remark;
        this.capacity = capacity;
        this.x_position = x_position;
        this.y_position = y_position;
    }

    // Getters
    public int getBid() { return bid; }
    public int getTid() { return tid; }
    public String getBkcname() { return bkcname; }
    public String getBktel() { return bktel; }
    public String getBdate() { return bdate; }
    public String getBtime() { return btime; }
    public int getPnum() { return pnum; }
    public int getStatus() { return status; }
    public String getPurpose() { return purpose; }
    public String getRemark() { return remark; }
    public int getCapacity() { return capacity; }
    public float getX_position() { return x_position; }
    public float getY_position() { return y_position; }
    public void setStatus(int status) { this.status = status; }

    public String getStatusText() {
        switch (status) {
            case 0:
            case 3:
                return "Cancelled";
            case 1:
                return "Pending";
            case 2:
                return "Done/Checked-in";
            default:
                return "Unknown";
        }
    }
}
