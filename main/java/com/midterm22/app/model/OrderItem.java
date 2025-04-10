package com.midterm22.app.model;


import java.util.HashMap;
import java.util.Map;

public class OrderItem {
    private String id;
    private String productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public OrderItem() {
    }

    public OrderItem(String id, String productId, String productName, String productImageUrl,  int quantity, double unitPrice, double totalPrice) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter methods
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    // Cập nhật phương thức toMap()
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("productId", productId);
        map.put("productName", productName);
        map.put("productImageUrl", productImageUrl); // Thêm dòng này
        map.put("quantity", quantity);
        map.put("unitPrice", unitPrice);
        map.put("totalPrice", totalPrice);
        return map;
    }


}
