package com.example.yummyrestaurant.inventory;

public class RestockDecisionRequest {
    public int recommendation_id;
    public String decision;
    public int staff_id;
    public String note;
    public int apply_stock;

    public RestockDecisionRequest(int recommendationId, String decision, int staffId, String note, boolean applyStock) {
        this.recommendation_id = recommendationId;
        this.decision = decision;
        this.staff_id = staffId;
        this.note = note;
        this.apply_stock = applyStock ? 1 : 0;
    }
}
