package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;

import java.util.ArrayList;
import java.util.List;

public class ManagerOrderActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private OrderAdminAdapter adapter;
    private final List<Order> orders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();
    private ImageButton btnSearch;
    private ImageView btnProfile;
    private TextView optionAll, optionPending, optionConfirm, optionShipping, optionCompleted, optionCancelled;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_order);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem đơn hàng!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(ManagerOrderActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            Log.d("ManagerOrderActivity", "User logged in: " + currentUser.getUid());
        }

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Thiết lập Navigation Drawer
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, AdminActivity.class));

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
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        // Ánh xạ các nút trên Toolbar
        btnSearch = findViewById(R.id.btnSearch);
        btnProfile = findViewById(R.id.btnProfile);

        // Thiết lập RecyclerView
        recyclerView = findViewById(R.id.rvOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdminAdapter(this, filteredOrders, order -> {
            Intent intent = new Intent(ManagerOrderActivity.this, OrderDetailAdminActivity.class);
            intent.putExtra("orderId", order.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Thiết lập menu options
        setupMenuOptions();

        // Xử lý nút tìm kiếm
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng tìm kiếm đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút thông tin cá nhân
        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển đến thông tin cá nhân!", Toast.LENGTH_SHORT).show();
        });

        // Lấy dữ liệu đơn hàng từ Firebase
        fetchOrders();
    }

    private void fetchOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        // Sắp xếp theo child "createdAt" (giả sử bạn có trường này) và giới hạn 100 đơn hàng
        Query query = ordersRef.orderByChild("createdAt").limitToLast(100);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                filteredOrders.clear();

                // Đảo ngược thứ tự bằng cách lặp từ cuối lên đầu
                List<DataSnapshot> snapshots = new ArrayList<>();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    snapshots.add(orderSnapshot);
                }

                // Lặp ngược để đảo thứ tự
                for (int i = snapshots.size() - 1; i >= 0; i--) {
                    DataSnapshot orderSnapshot = snapshots.get(i);
                    try {
                        Order order = orderSnapshot.getValue(Order.class);
                        if (order != null) {
                            // Đặt trạng thái mặc định là "Đang chờ" nếu status là null
                            if (order.getStatus() == null) {
                                order.setStatus("Pending");
                                ordersRef.child(orderSnapshot.getKey()).child("status").setValue("Pending");
                            }
                            orders.add(order);
                            filteredOrders.add(order);
                        }
                    } catch (Exception e) {
                        Log.e("ManagerOrderActivity", "Error parsing order: " + orderSnapshot.getKey() + ", Error: " + e.getMessage());
                    }
                }

                if (orders.isEmpty()) {
                    Toast.makeText(ManagerOrderActivity.this, "Không có đơn hàng nào!", Toast.LENGTH_SHORT).show();
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                } else {
                    Log.w("ManagerOrderActivity", "Adapter is null, cannot notify data set changed");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerOrderActivity.this, "Lỗi khi lấy đơn hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ManagerOrderActivity", "Error loading orders: " + error.getMessage());
            }
        });
    }

    private void filterOrders(String status) {
        filteredOrders.clear();
        if (status.equals("all")) {
            filteredOrders.addAll(orders);
        } else {
            for (Order order : orders) {
                if (order.getStatus() != null && order.getStatus().equals(status)) {
                    filteredOrders.add(order);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void setupMenuOptions() {
        optionAll = findViewById(R.id.optionAll);
        optionPending = findViewById(R.id.optionPending);
        optionConfirm = findViewById(R.id.optionConfirm);
        optionShipping = findViewById(R.id.optionShipping);
        optionCompleted = findViewById(R.id.optionCompleted);
        optionCancelled = findViewById(R.id.optionCancelled);

        optionAll.setSelected(true);

        View.OnClickListener menuOptionClickListener = v -> {
            optionAll.setSelected(false);
            optionPending.setSelected(false);
            optionConfirm.setSelected(false);
            optionShipping.setSelected(false);
            optionCompleted.setSelected(false);
            optionCancelled.setSelected(false);
            v.setSelected(true);
        };

        optionAll.setOnClickListener(v -> {
            filterOrders("all");
            menuOptionClickListener.onClick(v);
        });
        optionPending.setOnClickListener(v -> {
            filterOrders("Pending");
            menuOptionClickListener.onClick(v);
        });
        optionConfirm.setOnClickListener(v -> {
            filterOrders("Confirm");
            menuOptionClickListener.onClick(v);
        });
        optionShipping.setOnClickListener(v -> {
            filterOrders("Shipping");
            menuOptionClickListener.onClick(v);
        });
        optionCompleted.setOnClickListener(v -> {
            filterOrders("Complete");
            menuOptionClickListener.onClick(v);
        });
        optionCancelled.setOnClickListener(v -> {
            filterOrders("Cancelled");
            menuOptionClickListener.onClick(v);
        });
    }

    private void updateMenuOptionSelection(String status) {
        optionAll.setSelected(false);
        optionPending.setSelected(false);
        optionConfirm.setSelected(false);
        optionShipping.setSelected(false);
        optionCompleted.setSelected(false);
        optionCancelled.setSelected(false);

        switch (status) {
            case "all":
                optionAll.setSelected(true);
                break;
            case "Pending":
                optionPending.setSelected(true);
                break;
            case "Confirm":
                optionConfirm.setSelected(true);
                break;
            case "Shipping":
                optionShipping.setSelected(true);
                break;
            case "Complete":
                optionCompleted.setSelected(true);
                break;
            case "Cancelled":
                optionCancelled.setSelected(true);
                break;
        }
    }
}