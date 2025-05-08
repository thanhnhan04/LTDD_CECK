package com.midterm22.app.model;

import java.util.HashMap;
import java.util.Map;

public class Order {
    private String id;
    private String customerId;
    private Map<String, OrderItem> items;
    private double total;
    private String status; // "pending", "processing", "shipping", "completed", "cancelled"
    private String createdAt;
    private String updatedAt;
    private String note;
    private String paymentMethod; // Mới thêm trường này

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

    public Order(String id, String customerId, Map<String, OrderItem> items, double total, String status, String createdAt, String updatedAt, String note, String paymentMethod) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.total = total;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.note = note;
        this.paymentMethod = paymentMethod; // Khởi tạo paymentMethod
    }

    // Getter và Setter methods
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    // Getter và Setter cho paymentMethod
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    // Chuyển thành Map để lưu lên Firebase
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("customerId", customerId);

        // Chuyển các OrderItem thành Map
        Map<String, Object> itemsMap = new HashMap<>();
        for (Map.Entry<String, OrderItem> entry : items.entrySet()) {
            itemsMap.put(entry.getKey(), entry.getValue().toMap());
        }
        map.put("items", itemsMap);

        map.put("total", total);
        map.put("status", status);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        map.put("note", note);
        map.put("paymentMethod", paymentMethod); // Thêm trường paymentMethod vào Map
        return map;
    }
}
