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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AdminActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CardView cardCustomers, cardOrders, cardFood, cardStats;
    private TextView tvCustomerCount, tvOrderCount, tvFoodCount, tvRevenue;
    private ImageView btnProfile;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Gắn view
        Toolbar toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        btnProfile = findViewById(R.id.btnProfile);

        cardCustomers = findViewById(R.id.cardCustomers);
        cardOrders = findViewById(R.id.cardOrders);
        cardFood = findViewById(R.id.cardFood);
        cardStats = findViewById(R.id.cardStats);

        tvCustomerCount = findViewById(R.id.tvCustomerCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvFoodCount = findViewById(R.id.tvFoodCount);
        tvRevenue = findViewById(R.id.tvRevenue);


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Icon Profile
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, hosoadmin.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });


        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Trang chủ, không chuyển
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


        cardCustomers.setOnClickListener(v -> startActivity(new Intent(this, CustomerManagementActivity.class)));
        cardOrders.setOnClickListener(v -> startActivity(new Intent(this, ManagerOrderActivity.class)));
        cardFood.setOnClickListener(v -> startActivity(new Intent(this, FoodManagementActivity.class)));
        cardStats.setOnClickListener(v -> startActivity(new Intent(this, DoanhthuActivity.class)));


        loadStatistics();
    }

    private void loadStatistics() {
        databaseReference.child("User").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long countUsers = snapshot.getChildrenCount();
                tvCustomerCount.setText(countUsers + " khách hàng");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                tvCustomerCount.setText("0 khách hàng");
            }
        });
        databaseReference.child("orders").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int monthlyOrdersCount = 0;
                double monthlyRevenueValue = 0;

                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // Lưu ý: MONTH bắt đầu từ 0

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    String status = orderSnap.child("status").getValue(String.class);
                    if (status != null && status.equalsIgnoreCase("complete")) {
                        // Lấy ngày đặt hàng, giả sử bạn lưu ngày dưới dạng timestamp hoặc yyyy-MM-dd
                        String orderDateStr = orderSnap.child("createdAt").getValue(String.class);
                        // Parse orderDateStr thành năm, tháng (nếu định dạng yyyy-MM-dd)
                        if (orderDateStr != null && orderDateStr.length() >= 7) {
                            int orderYear = Integer.parseInt(orderDateStr.substring(0,4));
                            int orderMonth = Integer.parseInt(orderDateStr.substring(5,7));

                            Double orderPrice = orderSnap.child("total").getValue(Double.class);
                            if (orderYear == currentYear && orderMonth == currentMonth && orderPrice != null && orderPrice > 0) {
                                monthlyOrdersCount++;
                                monthlyRevenueValue += orderPrice;
                            }
                        }
                    }
                }

                tvOrderCount.setText(monthlyOrdersCount + " đơn hàng tháng này");
                tvRevenue.setText(String.format("%,.0f VNĐ", monthlyRevenueValue));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                tvOrderCount.setText("0 đơn hàng");
                tvRevenue.setText("0 VNĐ");
            }
        });


        databaseReference.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int countAvailableFoods = 0;
                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    Boolean available = productSnap.child("available").getValue(Boolean.class);
                    if (available != null && available) {
                        countAvailableFoods++;
                    }
                }
                tvFoodCount.setText(countAvailableFoods + " món ăn");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                tvFoodCount.setText("0 món ăn");
            }
        });

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
