package com.midterm22.app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvSubtotal, tvShippingFee, tvTotal, tvOrderNote, tvPaymentMethod;
    private RecyclerView recyclerOrderItems;
    private Button btnCancelOrder, btnReorder, btnReceiveorder, btnReview;
    private CardView layoutRated;
    private TextView tvRatedComment;
    private RatingBar tvRatedScore;
    private OrderItemAdapter adapter;
    private List<OrderItem> orderItems = new ArrayList<>();
    private DatabaseReference ordersRef, orderItemsRef;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
        // Nhận orderId từ Intent
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadOrderDetails();
        setupButtons();
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvTotal = findViewById(R.id.tvTotal);
        recyclerOrderItems = findViewById(R.id.recyclerOrderItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        btnReorder = findViewById(R.id.btnReorder);
        btnReceiveorder = findViewById(R.id.btnReceiveOrder);
        btnReview = findViewById(R.id.btnReview);
        tvOrderNote = findViewById(R.id.tvOrderNote);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);

        layoutRated = findViewById(R.id.layoutRated);
        tvRatedScore = findViewById(R.id.ratingBarRated);
        tvRatedComment = findViewById(R.id.tvRatedComment);


        ordersRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderItemsRef = FirebaseDatabase.getInstance().getReference("orderItems");
    }
    private void checkReview(String orderId) {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(orderId);
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Đã đánh giá
                    Long rating = snapshot.child("rating").getValue(Long.class); // hoặc Integer
                    String comment = snapshot.child("comment").getValue(String.class);

                    layoutRated.setVisibility(View.VISIBLE);
                    tvRatedScore.setRating(rating);
                    tvRatedComment.setText("Phản hồi: "+comment);

                    btnReview.setVisibility(View.GONE);
                } else {
                    // Chưa đánh giá
                    layoutRated.setVisibility(View.GONE);
                    btnReview.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderDetail", "Lỗi khi kiểm tra đánh giá: " + error.getMessage());
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new OrderItemAdapter(this, orderItems);
        recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerOrderItems.setAdapter(adapter);
    }

    private void loadOrderDetails() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Order order = snapshot.getValue(Order.class);
                if (order != null) {
                    displayOrderInfo(order);

                    // Xử lý khi items là Map
                    if (order.getItems() != null) {
                        orderItems.clear();
                        orderItems.addAll(order.getItems().values()); // Lấy tất cả values từ Map
                        adapter.notifyDataSetChanged();

                        Log.d("OrderDetail", "Loaded " + order.getItems().size() + " items from order");
                    } else {
                        Log.d("OrderDetail", "Order items is null");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("OrderDetail", "Error loading order: " + error.getMessage());
            }
        });
    }

    private void displayOrderInfo(Order order) {
        tvOrderId.setText("Đơn hàng #" + order.getId().substring(0, 6).toUpperCase());
        String createdAt = order.getCreatedAt();
        String formattedDate = createdAt != null ? createdAt : "N/A";
        try {
            long timestamp = Long.parseLong(createdAt);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            formattedDate = sdf.format(new Date(timestamp));
        } catch (NumberFormatException e) {
            try {
                String[] parts = createdAt.split(" ");
                String[] dateParts = parts[0].split("-");
                formattedDate = String.format("%s/%s/%s %s", dateParts[2], dateParts[1], dateParts[0], parts[1].substring(0, 5));
            } catch (Exception ex) {
                Log.w("OrderAdapter", "Error formatting date: " + createdAt);
            }
        }
        // Ghi chú
        String note = order.getNote();
        tvOrderNote.setText(note != null && !note.isEmpty() ? note : "Không có ghi chú");

        tvOrderDate.setText("Ngày đặt: " + formattedDate);

        // Định dạng tiền tệ
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvSubtotal.setText(format.format(order.getTotal()));
        tvTotal.setText(format.format(order.getTotal()));
        // Phương thức thanh toán
        String payment = order.getPaymentMethod();
        tvPaymentMethod.setText(payment != null && !payment.isEmpty() ? payment : "Chưa rõ");
        // Hiển thị trạng thái
        setOrderStatusUI(order.getStatus());
        if ("Complete".equals(order.getStatus())) {
            checkReview(orderId);
        }
    }

    private void setOrderStatusUI(String status) {
        String statusText;
        int bgColor;

        switch (status) {
            case "Pending":
                statusText = "CHỜ XỬ LÝ";
                bgColor = 0xFFFB8C00; // Orange
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnReorder.setVisibility(View.GONE);
                break;
            case "Confirm":
                statusText = "XÁC NHẬN";
                bgColor = 0xFF4CAF50;
                btnCancelOrder.setVisibility(View.VISIBLE);
                btnReorder.setVisibility(View.GONE);
                break;
            case "Shipping":
                statusText = "ĐANG GIAO";
                bgColor = 0xFF1976D2; // Blue
                btnReceiveorder.setVisibility(View.VISIBLE);
                btnReorder.setVisibility(View.GONE);
                break;
            case "Complete":
                statusText = "HOÀN THÀNH";
                bgColor = 0xFF388E3C; // Green
                btnReview.setVisibility(View.VISIBLE);
                btnReorder.setVisibility(View.GONE);
                break;
            case "Cancelled":
                statusText = "ĐÃ HỦY";
                bgColor = 0xFFD32F2F; // Red
                break;
            default:
                statusText = status.toUpperCase();
                bgColor = 0xFF000000; // Black
        }

        tvOrderStatus.setText(statusText);
        tvOrderStatus.setBackgroundColor(bgColor);
    }

    private void setupButtons() {
        btnCancelOrder.setOnClickListener(v -> cancelOrder());
        btnReorder.setOnClickListener(v -> reorder());
        btnReceiveorder.setOnClickListener(v -> receiveorder());
        btnReview.setOnClickListener(v -> openReviewDialog());

    }
    private void openReviewDialog() {
        ReviewDialogFragment dialog = ReviewDialogFragment.newInstance(orderId);
        dialog.show(getSupportFragmentManager(), "ReviewDialog");
    }

    private void cancelOrder() {
        ordersRef.child("status").setValue("Cancelled")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi hủy đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void reorder() {
        ordersRef.child("status").setValue("Pending")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi đặt lại đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void receiveorder() {
        ordersRef.child("status").setValue("Complete")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã nhận hàng thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi nhận đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}