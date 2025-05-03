package com.midterm22.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerDetailActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvPhone, tvAddress;
    private Button btnToggleStatus;
    private String userId;
    private DatabaseReference userRef;
    private User currentUser;
    private boolean isLocked = false;

    private RecyclerView recyclerViewOrders;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private List<Order> customerOrders = new ArrayList<>();
    private OrderAdminAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);


        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);
        recyclerViewOrders = findViewById(R.id.recyclerOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        userId = getIntent().getStringExtra("userId");
        userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        loadUserDetail();

        btnToggleStatus.setOnClickListener(view -> toggleUserStatus());

        orderAdapter = new OrderAdminAdapter(this, customerOrders, null);
        recyclerViewOrders.setAdapter(orderAdapter);
    }

    private void loadUserDetail() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    tvName.setText("Tên: " + currentUser.getName());
                    tvEmail.setText("Email: " + currentUser.getEmail());
                    tvPhone.setText("SĐT: " + currentUser.getPhone());
                    tvAddress.setText("Địa chỉ: " + currentUser.getAddress());
                    isLocked = "locked".equals(currentUser.getRole());
                    updateButtonText();


                    loadOrderHistory();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerDetailActivity.this, "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleUserStatus() {
        if (currentUser == null) return;
        String newRole = isLocked ? "customer" : "locked";
        userRef.child("role").setValue(newRole).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, isLocked ? "Mở tài khoản" : "Khóa tài khoản", Toast.LENGTH_SHORT).show();
                isLocked = !isLocked;
                updateButtonText();
            } else {
                Toast.makeText(this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonText() {
        btnToggleStatus.setText(isLocked ? "Mở tài khoản" : "Khóa tài khoản");
    }

    private void loadOrderHistory() {
        progressBar.setVisibility(View.VISIBLE);
        Log.d("CustomerDetail", "Bắt đầu tải lịch sử đơn hàng...");

        // Truy vấn đơn hàng theo customerId
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order");
        orderRef.orderByChild("customerId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        Log.d("CustomerDetail", "Dữ liệu trả về: " + snapshot.getValue());

                        customerOrders.clear();

                        if (snapshot.exists()) {
                            Log.d("CustomerDetail", "Tìm thấy dữ liệu đơn hàng.");
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Order order = child.getValue(Order.class);
                                if (order != null) {
                                    customerOrders.add(order);
                                    Log.d("CustomerDetail", "Đơn hàng thêm vào danh sách: " + order);
                                }
                            }

                            orderAdapter.notifyDataSetChanged();

                            if (customerOrders.isEmpty()) {
                                Log.d("CustomerDetail", "Không có đơn hàng trong danh sách.");
                            }

                            tvEmpty.setVisibility(customerOrders.isEmpty() ? View.VISIBLE : View.GONE); // Hiển thị thông báo nếu không có đơn hàng
                            recyclerViewOrders.setVisibility(customerOrders.isEmpty() ? View.GONE : View.VISIBLE); // Ẩn RecyclerView nếu không có đơn hàng
                        } else {
                            Log.d("CustomerDetail", "Không tìm thấy đơn hàng cho userId: " + userId);
                            tvEmpty.setVisibility(View.VISIBLE);
                            recyclerViewOrders.setVisibility(View.GONE);
                        }

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("CustomerDetail", "Lỗi khi tải dữ liệu: " + error.getMessage());
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CustomerDetailActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
