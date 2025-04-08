package com.midterm22.app.model;


public class CartItem {
    private String productId;
    private int quantity;
    private double priceAtAddition;

    public CartItem() {
    }

    public CartItem(String productId, int quantity, double priceAtAddition) {
        this.productId = productId;
        this.quantity = quantity;
        this.priceAtAddition = priceAtAddition;
    }

    // Getter and Setter methods
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPriceAtAddition() {
        return priceAtAddition;
    }

    public void setPriceAtAddition(double priceAtAddition) {
        this.priceAtAddition = priceAtAddition;
    }
}