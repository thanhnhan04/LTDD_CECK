package com.midterm22.app.model;

public class OrderItem {
    private String productName;
    private double unitPrice;
    private int quantity;
    private Order order; // Thêm trường để liên kết với Order

    // Constructor
    public OrderItem(String productName, double unitPrice, int quantity, Order order) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.order = order;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}