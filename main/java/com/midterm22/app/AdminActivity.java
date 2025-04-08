package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminActivity extends AppCompatActivity {

    private CardView cardCustomers, cardOrders, cardFood, cardStats;
    private TextView tvCustomerCount, tvOrderCount, tvFoodCount, tvRevenue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Ánh xạ view
        cardCustomers = findViewById(R.id.cardCustomers);
        cardOrders = findViewById(R.id.cardOrders);
        cardFood = findViewById(R.id.cardFood);
        cardStats = findViewById(R.id.cardStats);

        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvFoodCount = findViewById(R.id.tvFoodCount);
        tvRevenue = findViewById(R.id.tvRevenue);


        // Load dữ liệu thống kê (có thể thay bằng API call thực tế)
        loadStatistics();
    }

    private void loadStatistics() {
        // Giả lập dữ liệu - trong thực tế sẽ gọi API hoặc database
        tvCustomerCount.setText("125 khách hàng");
        tvOrderCount.setText("42 đơn hàng");
        tvFoodCount.setText("35 món ăn");
        tvRevenue.setText("12,450,000 VNĐ");
    }
}