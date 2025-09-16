package com.example.yummyrestaurant.models;

public class MenuItem {
    private int id;
    private String nameEn;
    private String nameZhCN;
    private String nameZhTW;
    private String descEn;
    private String descZhCN;
    private String descZhTW;
    private double price;
    private String image_url;
    private String spice_level;
    private String tags;
    private String category;

    public String getName(String language) {
        switch (language) {
            case "zh-CN": return nameZhCN;
            case "zh-TW": return nameZhTW;
            default: return nameEn;
        }
    }

    public String getDescription(String language) {
        switch (language) {
            case "zh-CN": return descZhCN;
            case "zh-TW": return descZhTW;
            default: return descEn;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameZhCN() { return nameZhCN; }
    public void setNameZhCN(String nameZhCN) { this.nameZhCN = nameZhCN; }

    public String getNameZhTW() { return nameZhTW; }
    public void setNameZhTW(String nameZhTW) { this.nameZhTW = nameZhTW; }

    public String getDescEn() { return descEn; }
    public void setDescEn(String descEn) { this.descEn = descEn; }

    public String getDescZhCN() { return descZhCN; }
    public void setDescZhCN(String descZhCN) { this.descZhCN = descZhCN; }

    public String getDescZhTW() { return descZhTW; }
    public void setDescZhTW(String descZhTW) { this.descZhTW = descZhTW; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

    public String getSpice_level() { return spice_level; }
    public void setSpice_level(String spice_level) { this.spice_level = spice_level; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}