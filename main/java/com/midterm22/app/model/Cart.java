package com.midterm22.app.model;


import java.util.HashMap;
import java.util.Map;

public class Cart {
    private String userId;
    private Map<String, CartItem> items;
    private String createdAt;
    private String updatedAt;

    public Cart() {
        items = new HashMap<>();
    }

    public Cart(String userId, Map<String, CartItem> items, String createdAt, String updatedAt) {
        this.userId = userId;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter and Setter methods
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, CartItem> getItems() {
        return items;
    }

    public void setItems(Map<String, CartItem> items) {
        this.items = items;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}