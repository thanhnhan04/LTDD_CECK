package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CardView cardCustomers, cardOrders, cardFood, cardStats;
    private TextView tvCustomerCount, tvOrderCount, tvFoodCount, tvRevenue;
    private ImageView btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Ánh xạ các view
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnProfile = findViewById(R.id.btnProfile); // Thêm dòng này để ánh xạ btnProfile

        cardCustomers = findViewById(R.id.cardCustomers);
        cardOrders = findViewById(R.id.cardOrders);
        cardFood = findViewById(R.id.cardFood);
        cardStats = findViewById(R.id.cardStats);

        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvFoodCount = findViewById(R.id.tvFoodCount);
        tvRevenue = findViewById(R.id.tvRevenue);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Thiết lập Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý sự kiện click hình người
        btnProfile.setOnClickListener(v -> {
            Intent profileIntent = new Intent(AdminActivity.this, hosoadmin.class);
            startActivity(profileIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Xử lý sự kiện click menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Đã ở trang chủ, không cần làm gì
            } else if (id == R.id.nav_qldh) {
                startActivity(new Intent(this, ManagerOrderActivity.class));
            } else if (id == R.id.nav_qlsp) {
                startActivity(new Intent(this, FoodManagementActivity.class));
            } else if (id == R.id.nav_qlkh) {
                startActivity(new Intent(this, CustomerManagementActivity.class));
            } else if (id == R.id.nav_qldt) {
                startActivity(new Intent(this, DoanhthuActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Xử lý sự kiện click các card view
        cardCustomers.setOnClickListener(v -> {
            startActivity(new Intent(this, CustomerManagementActivity.class));
        });

        cardOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, ManagerOrderActivity.class));
        });

        cardFood.setOnClickListener(v -> {
            startActivity(new Intent(this, FoodManagementActivity.class));
        });

        cardStats.setOnClickListener(v -> {
            startActivity(new Intent(this, DoanhthuActivity.class)); // Điều hướng đến DoanhthuActivity
        });

        // Tải dữ liệu thống kê
        loadStatistics();
    }

    private void loadStatistics() {
        // TODO: Thay thế bằng dữ liệu thực từ Firebase hoặc API
        tvCustomerCount.setText("125 khách hàng");
        tvOrderCount.setText("42 đơn hàng");
        tvFoodCount.setText("35 món ăn");
        tvRevenue.setText("12,450,000 VNĐ");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}