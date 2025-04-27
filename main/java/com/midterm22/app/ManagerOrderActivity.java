package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;

import java.util.ArrayList;
import java.util.List;

public class ManagerOrderActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private final List<Order> orders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();
    private ImageButton btnSearch;
    private ImageView btnProfile;
    private TextView optionAll, optionConfirm, optionPackaging, optionShipping, optionCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managerorder);

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

        // Xử lý sự kiện chọn mục trong Navigation Drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            String selectedStatus = "all"; // Mặc định
            switch (item.getItemId()) {
//                case R.id.nav_all:
//                    selectedStatus = "all";
//                    break;
//                case R.id.nav_confirm:
//                    selectedStatus = "pending";
//                    break;
//                case R.id.nav_packaging:
//                    selectedStatus = "processing";
//                    break;
//                case R.id.nav_shipping:
//                    selectedStatus = "shipping";
//                    break;
//                case R.id.nav_completed:
//                    selectedStatus = "completed";
//                    break;
                default:
                    Toast.makeText(this, "Chức năng chưa được hỗ trợ!", Toast.LENGTH_SHORT).show();
                    break;
            }

            // Lọc danh sách đơn hàng theo trạng thái
            filterOrders(selectedStatus);

            // Đồng bộ trạng thái chọn trong HorizontalScrollView
            updateMenuOptionSelection(selectedStatus);

            // Đóng Navigation Drawer sau khi chọn
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Ánh xạ các nút trên Toolbar
        btnSearch = findViewById(R.id.btnSearch);
        btnProfile = findViewById(R.id.btnProfile);

        // Thiết lập RecyclerView
        recyclerView = findViewById(R.id.rvOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(filteredOrders, order -> {
            // Xử lý khi nhấn "Xem chi tiết"
            Intent intent = new Intent(ManagerOrderActivity.this, OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        // Thiết lập menu options
        setupMenuOptions();

        // Xử lý nút tìm kiếm
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng tìm kiếm đang được phát triển!", Toast.LENGTH_SHORT).show();
            // TODO: Thêm logic tìm kiếm
        });

        // Xử lý nút thông tin cá nhân
        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển đến thông tin cá nhân!", Toast.LENGTH_SHORT).show();
            // TODO: Chuyển đến ProfileActivity
        });

        // Lấy dữ liệu từ Firebase
        fetchOrders();
    }

    private void fetchOrders() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orders.clear();
                filteredOrders.clear();
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        orders.add(order);
                        filteredOrders.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManagerOrderActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterOrders(String status) {
        filteredOrders.clear();
        if (status.equals("all")) {
            filteredOrders.addAll(orders);
        } else {
            for (Order order : orders) {
                if (order.getStatus().equalsIgnoreCase(status)) {
                    filteredOrders.add(order);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupMenuOptions() {
        optionAll = findViewById(R.id.optionAll);
        optionConfirm = findViewById(R.id.optionConfirm);
        optionPackaging = findViewById(R.id.optionPackaging);
        optionShipping = findViewById(R.id.optionShipping);
        optionCompleted = findViewById(R.id.optionCompleted);

        // Đặt trạng thái chọn mặc định cho "Tất cả"
        optionAll.setSelected(true);

        View.OnClickListener menuOptionClickListener = v -> {
            // Reset trạng thái chọn
            optionAll.setSelected(false);
            optionConfirm.setSelected(false);
            optionPackaging.setSelected(false);
            optionShipping.setSelected(false);
            optionCompleted.setSelected(false);

            // Đặt trạng thái chọn cho mục được nhấn
            v.setSelected(true);
        };

        optionAll.setOnClickListener(v -> {
            filterOrders("all");
            menuOptionClickListener.onClick(v);
        });
        optionConfirm.setOnClickListener(v -> {
            filterOrders("pending");
            menuOptionClickListener.onClick(v);
        });
        optionPackaging.setOnClickListener(v -> {
            filterOrders("processing");
            menuOptionClickListener.onClick(v);
        });
        optionShipping.setOnClickListener(v -> {
            filterOrders("shipping");
            menuOptionClickListener.onClick(v);
        });
        optionCompleted.setOnClickListener(v -> {
            filterOrders("completed");
            menuOptionClickListener.onClick(v);
        });
    }

    private void updateMenuOptionSelection(String status) {
        // Reset trạng thái chọn
        optionAll.setSelected(false);
        optionConfirm.setSelected(false);
        optionPackaging.setSelected(false);
        optionShipping.setSelected(false);
        optionCompleted.setSelected(false);

        // Đặt trạng thái chọn dựa trên status
        switch (status) {
            case "all":
                optionAll.setSelected(true);
                break;
            case "pending":
                optionConfirm.setSelected(true);
                break;
            case "processing":
                optionPackaging.setSelected(true);
                break;
            case "shipping":
                optionShipping.setSelected(true);
                break;
            case "completed":
                optionCompleted.setSelected(true);
                break;
        }
    }
}