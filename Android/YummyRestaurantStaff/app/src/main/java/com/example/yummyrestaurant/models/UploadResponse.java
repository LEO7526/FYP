package com.example.yummyrestaurant.models;

public class UploadResponse {
    private String status;
    private String path;
    private String imageUrl;
    private String fileName;
    private String message; // optional, for error cases

    public String getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMessage() {
        return message;
    }
}
