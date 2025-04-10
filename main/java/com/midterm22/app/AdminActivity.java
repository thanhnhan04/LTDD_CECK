package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý sự kiện click menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Trang chính
            } else if (id == R.id.nav_logout) {
                // Đăng xuất
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        cardCustomers = findViewById(R.id.cardCustomers);
        cardOrders = findViewById(R.id.cardOrders);
        cardFood = findViewById(R.id.cardFood);
        cardStats = findViewById(R.id.cardStats);

        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvFoodCount = findViewById(R.id.tvFoodCount);
        tvRevenue = findViewById(R.id.tvRevenue);

        loadStatistics();
    }

    private void loadStatistics() {
        tvCustomerCount.setText("125 khách hàng");
        tvOrderCount.setText("42 đơn hàng");
        tvFoodCount.setText("35 món ăn");
        tvRevenue.setText("12,450,000 VNĐ");
    }
}
