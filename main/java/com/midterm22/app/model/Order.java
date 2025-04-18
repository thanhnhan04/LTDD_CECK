package com.midterm22.app.model;


import java.util.HashMap;
import java.util.Map;

public class Order {
    private String id;
    private String customerId;
    private Map<String, OrderItem> items;
    private double total;
    private String status; // "pending", "processing", "completed", "cancelled"
    private String createdAt;
    private String updatedAt;

    public Order() {
        items = new HashMap<>();
    }

    public Order(String id, String customerId, Map<String, OrderItem> items, double total, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getter and Setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Map<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(Map<String, OrderItem> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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