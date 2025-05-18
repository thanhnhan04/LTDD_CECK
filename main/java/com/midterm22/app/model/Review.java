package com.midterm22.app.model;

public class Review {
    private float rating;
    private String comment;

    public Review() {}

    public Review(float rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }
}
