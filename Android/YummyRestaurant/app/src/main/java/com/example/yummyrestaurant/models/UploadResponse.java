package com.example.yummyrestaurant.models;

public class UploadResponse {
    private String status;
    private String path;
    private String message; // optional, for error cases

    public String getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getMessage() {
        return message;
    }
}
