package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ListOrderActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private OrderAdapter orderAdapter;

    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();

    private DatabaseReference ordersRef;
    private String currentUserId;
    private ValueEventListener ordersListener;

    private TextView optionAll, optionPending, optionConfirm, optionShipping, optionCompleted, optionCancelled;

    // Sử dụng Executor để xử lý dữ liệu trên background thread
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        initViews();
        setupRecyclerView();
        setupMenuOptions();

        // Kiểm tra trạng thái đăng nhập trước khi load orders
        if (checkUserAuthentication()) {
            loadOrders();
        }
    }

    private boolean checkUserAuthentication() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }

    private void initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        optionAll = findViewById(R.id.optionAll);
        optionPending = findViewById(R.id.optionPending);
        optionConfirm = findViewById(R.id.optionConfirm);
        optionShipping = findViewById(R.id.optionShipping);
        optionCompleted = findViewById(R.id.optionCompleted);
        optionCancelled = findViewById(R.id.optionCancelled);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, filteredOrders, this);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        recyclerOrders.setVisibility(View.GONE);

        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                executor.execute(() -> {
                    List<Order> tempOrders = new ArrayList<>();

                    if (!snapshot.exists()) {
                        runOnUiThread(() -> showEmptyState());
                        return;
                    }

                    for (DataSnapshot data : snapshot.getChildren()) {
                        Order order = data.getValue(Order.class);
                        if (order != null) {
                            order.setId(data.getKey());
                            tempOrders.add(order);
                        }
                    }

                    // Cập nhật UI trên main thread
                    runOnUiThread(() -> {
                        allOrders.clear();
                        allOrders.addAll(tempOrders);
                        filterOrders("all");
                        progressBar.setVisibility(View.GONE);
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ListOrderActivity.this,
                            "Lỗi tải đơn hàng: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    showEmptyState();
                });
            }
        };

        ordersRef.orderByChild("customerId").equalTo(currentUserId)
                .addValueEventListener(ordersListener);
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void filterOrders(String status) {
        filteredOrders.clear();

        if (status.equals("all")) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(status)) {
                    filteredOrders.add(order);
                }
            }
        }

        Collections.sort(filteredOrders, (o1, o2) -> {
            try {
                long t1 = Long.parseLong(o1.getCreatedAt());
                long t2 = Long.parseLong(o2.getCreatedAt());
                return Long.compare(t2, t1);
            } catch (Exception e) {
                return 0;
            }
        });

        if (filteredOrders.isEmpty()) {
            showEmptyState();
        } else {
            recyclerOrders.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        orderAdapter.notifyDataSetChanged();
        updateMenuOptionSelection(status);
    }


    private void setupMenuOptions() {
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

    @Override
    public void onOrderClick(Order order) {
        if (order != null && order.getId() != null) {
            Intent intent = new Intent(this, OrderDetailActivity.class);
            intent.putExtra("order_id", order.getId());
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký listener khi Activity bị hủy
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
    }
}