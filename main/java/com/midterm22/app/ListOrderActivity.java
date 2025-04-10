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
import com.midterm22.app.OrderAdapter;
import com.midterm22.app.model.Order;

import java.util.ArrayList;
import java.util.List;

public class ListOrderActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {

    private RecyclerView recyclerOrders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private OrderAdapter orderAdapter;
    private List<Order> orderList = new ArrayList<>();
    private DatabaseReference ordersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_order);
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        initViews();
        setupRecyclerView();
        loadOrders();
    }

    private void initViews() {
        recyclerOrders = findViewById(R.id.recyclerOrders);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this, orderList, this);
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrders.setAdapter(orderAdapter);
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ordersRef.orderByChild("customerId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();

                        if (!snapshot.exists()) {
                            showEmptyState();
                            return;
                        }

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Order order = data.getValue(Order.class);
                            if (order != null) {
                                order.setId(data.getKey()); // Lưu ID đơn hàng
                                orderList.add(order);
                            }
                        }

                        if (orderList.isEmpty()) {
                            showEmptyState();
                        } else {
                            orderAdapter.notifyDataSetChanged();
                        }
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ListOrderActivity.this,
                                "Lỗi tải đơn hàng: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showEmptyState() {
        tvEmpty.setVisibility(View.VISIBLE);
        recyclerOrders.setVisibility(View.GONE);
    }

    @Override
    public void onOrderClick(Order order) {
        // Xử lý khi click vào đơn hàng
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }
}