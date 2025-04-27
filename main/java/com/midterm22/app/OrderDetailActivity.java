package com.midterm22.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.OrderItemAdapter;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.OrderItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvTotalAmount, tvTotalAmountFooter, tvCustomerName, tvCustomerPhone, tvCustomerAddress;
    private RecyclerView rvOrderItems;
    private Button btnCancelOrder, btnUpdateStatus;
    private OrderItemAdapter itemAdapter;
    private final List<OrderItem> orderItems = new ArrayList<>();
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Ánh xạ view
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTotalAmountFooter = findViewById(R.id.tvTotalAmountFooter);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);

        // Thiết lập RecyclerView cho items
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new OrderItemAdapter(orderItems);
        rvOrderItems.setAdapter(itemAdapter);

        // Lấy orderId từ Intent
        String orderId = getIntent().getStringExtra("orderId");
        if (orderId != null) {
            fetchOrderDetails(orderId);
        }

        // Xử lý nút "Hủy hàng"
        btnCancelOrder.setOnClickListener(v -> {
            if (currentOrder != null) {
                updateOrderStatus("cancelled");
            }
        });

        // Xử lý nút "Cập nhật trạng thái"
        btnUpdateStatus.setOnClickListener(v -> {
            if (currentOrder != null) {
                String currentStatus = currentOrder.getStatus().toLowerCase();
                String newStatus;
                switch (currentStatus) {
                    case "pending":
                        newStatus = "processing";
                        break;
                    case "processing":
                        newStatus = "shipping";
                        break;
                    case "shipping":
                        newStatus = "completed";
                        break;
                    default:
                        Toast.makeText(this, "Đơn hàng đã hoàn thành hoặc bị hủy.", Toast.LENGTH_SHORT).show();
                        return;
                }
                updateOrderStatus(newStatus);
            }
        });
    }

    private void fetchOrderDetails(String orderId) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentOrder = snapshot.getValue(Order.class);
                if (currentOrder != null) {
                    // Thông tin đơn hàng
                    tvOrderId.setText(currentOrder.getId());
                    tvOrderDate.setText(currentOrder.getCreatedAt());
                    String totalText = String.format(Locale.getDefault(), "%.0f đ", currentOrder.getTotal());
                    tvTotalAmount.setText(totalText);
                    tvTotalAmountFooter.setText(totalText);

                    // Thông tin khách hàng
                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentOrder.getCustomerId());
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.child("name").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            String address = snapshot.child("address").getValue(String.class);

                            tvCustomerName.setText(name != null ? name : "Unknown");
                            tvCustomerPhone.setText(phone != null ? phone : "N/A");
                            tvCustomerAddress.setText(address != null ? address : "N/A");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvCustomerName.setText("Unknown");
                            tvCustomerPhone.setText("N/A");
                            tvCustomerAddress.setText("N/A");
                        }
                    });

                    // Danh sách sản phẩm
                    orderItems.clear();
                    if (currentOrder.getItems() != null) {
                        orderItems.addAll(currentOrder.getItems().values());
                    }
                       itemAdapter.notifyDataSetChanged();

                    // Cập nhật trạng thái nút dựa trên status
//                    updateButtonVisibility();
                } else {
                    Toast.makeText(OrderDetailActivity.this, "Không tìm thấy đơn hàng.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateOrderStatus(String newStatus) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(currentOrder.getId());
        orderRef.child("status").setValue(newStatus).addOnSuccessListener(aVoid -> {
            currentOrder.setStatus(newStatus);
            Toast.makeText(OrderDetailActivity.this, "Cập nhật trạng thái thành công: " + newStatus, Toast.LENGTH_SHORT).show();
//            updateButtonVisibility();
        }).addOnFailureListener(e -> {
            Toast.makeText(OrderDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

//    private void updateButtonVisibility() {
//        if (currentOrder == null) return;
//
//        String status = currentOrder.getStatus().toLowerCase();
//        switch (status) {
//            case "pending":
//                btnCancelOrder.setVisibility(View.VISIBLE);
//                btnUpdateStatus.setVisibility(View.VISIBLE);
//                btnUpdateStatus.setText("Cập nhật trạng thái");
//                break;
//            case "processing":
//                btnCancelOrder.setVisibility(View.VISIBLE);
//                btnUpdateStatus.setVisibility(View.VISIBLE);
//                btnUpdateStatus.setText("Cập nhật trạng thái");
//                break;
//            case "shipping":
//                btnCancelOrder.setVisibility(View.GONE);
//                btnUpdateStatus.setVisibility(View.VISIBLE);
//                btnUpdateStatus.setText("Cập nhật trạng thái");
//                break;
//            case "completed":
//            case "cancelled":
//                btnCancelOrder.setVisibility(View.GONE);
//                btnUpdateStatus.setVisibility(View.GONE);
//                break;
//            default:
//                btnCancelOrder.setVisibility(View.VISIBLE);
//                btnUpdateStatus.setVisibility(View.VISIBLE);
//                break;
//        }
//    }
}