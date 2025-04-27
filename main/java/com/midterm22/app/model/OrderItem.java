package com.midterm22.app.model;

public class OrderItem {
    private String productName;
    private double unitPrice;
    private int quantity;

    // Constructor rỗng (Firebase cần)
    public OrderItem() {
    }

    // Constructor đầy đủ (bạn đang dùng)
    public OrderItem(String productName, double unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // Getter và Setter
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
