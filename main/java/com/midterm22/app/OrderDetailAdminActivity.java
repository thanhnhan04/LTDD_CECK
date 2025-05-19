package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailAdminActivity extends AppCompatActivity {

    private TextView tvOrderId, tvOrderDate, tvTotalAmount, tvCustomerName, tvCustomerPhone, tvCustomerAddress, tvReviewComment, tvOrderNote, tvPaymentMethod;
    private RecyclerView rvOrderItems;
    private Button btnCancelOrder;
    private Spinner spinnerUpdateStatus;
    private ImageButton btnSearch;
    private ImageView btnProfile;
    private RatingBar ratingBarReview;
    private CardView layoutReview;
    private OrderItemAdapter itemAdapter;
    private List<OrderItem> orderItems = new ArrayList<>();
    private String orderId;
    private List<String> statusList = Arrays.asList("Pending", "Confirm", "Shipping", "Complete", "Cancelled");
    private String currentStatus; // Lưu trạng thái hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail_admin);

        // Nhận orderId từ Intent
        Intent intent = getIntent();
        orderId = intent.getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ các view từ XML
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvCustomerAddress = findViewById(R.id.tvCustomerAddress);
        tvReviewComment = findViewById(R.id.tvReviewComment);
        tvOrderNote = findViewById(R.id.tvOrderNote);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
        spinnerUpdateStatus = findViewById(R.id.spinnerUpdateStatus);
//        btnSearch = findViewById(R.id.btnSearch);
        btnProfile = findViewById(R.id.btnProfile);
        ratingBarReview = findViewById(R.id.ratingBarReview);
        layoutReview = findViewById(R.id.layoutReview);

        // Thiết lập RecyclerView cho danh sách sản phẩm
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new OrderItemAdapter(this, orderItems);
        rvOrderItems.setAdapter(itemAdapter);

        // Thiết lập Spinner cho trạng thái đơn hàng
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusList);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdateStatus.setAdapter(statusAdapter);

        // Xử lý nút thông tin cá nhân
        btnProfile.setOnClickListener(v -> {
            Toast.makeText(this, "Chuyển đến thông tin cá nhân!", Toast.LENGTH_SHORT).show();
        });

        // Lấy dữ liệu đơn hàng từ Firebase
        fetchOrderDetails();

        // Xử lý nút Hủy đơn hàng
        btnCancelOrder.setOnClickListener(v -> cancelOrder());

        // Xử lý sự kiện chọn trạng thái từ Spinner
        spinnerUpdateStatus.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                String selectedStatus = statusList.get(position);
                updateOrderStatus(selectedStatus);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                // Không làm gì khi không có trạng thái nào được chọn
            }
        });
    }

    private void fetchOrderDetails() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Order order = snapshot.getValue(Order.class);
                if (order == null) {
                    Toast.makeText(OrderDetailAdminActivity.this, "Không tìm thấy đơn hàng!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // Hiển thị thông tin đơn hàng
                tvOrderId.setText(order.getId());
                tvTotalAmount.setText(formatCurrency(order.getTotal()));

                // Định dạng ngày
                String createdAt = order.getCreatedAt();
                String formattedDate = createdAt != null ? createdAt : "N/A";
                try {
                    long timestamp = Long.parseLong(createdAt);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    formattedDate = sdf.format(new Date(timestamp));
                } catch (NumberFormatException e) {
                    Log.w("OrderDetailActivity", "Error formatting date: " + createdAt);
                }
                tvOrderDate.setText(formattedDate);

                // Hiển thị ghi chú món ăn
                String note = order.getNote() != null ? order.getNote() : "";
                tvOrderNote.setText(note);

                // Hiển thị phương thức thanh toán
                String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod() : "N/A";
                tvPaymentMethod.setText(paymentMethod);

                // Lưu và hiển thị trạng thái hiện tại trong Spinner
                currentStatus = order.getStatus() != null ? order.getStatus() : "Pending";
                int statusIndex = statusList.indexOf(currentStatus);
                if (statusIndex != -1) {
                    spinnerUpdateStatus.setSelection(statusIndex);
                } else {
                    spinnerUpdateStatus.setSelection(0); // Mặc định là "Pending"
                }

                // Lấy thông tin khách hàng
                fetchCustomerDetails(order.getCustomerId());

                // Lấy danh sách sản phẩm
                fetchOrderItems();

                // Nếu trạng thái là "Complete", lấy thông tin đánh giá
                if ("Complete".equals(currentStatus)) {
                    fetchOrderReview();
                } else {
                    layoutReview.setVisibility(CardView.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailAdminActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderDetailActivity", "Error loading order: " + error.getMessage());
            }
        });
    }

    private void fetchCustomerDetails(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            tvCustomerName.setText("Khách hàng không xác định");
            tvCustomerPhone.setText("N/A");
            tvCustomerAddress.setText("N/A");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User").child(customerId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    tvCustomerName.setText(name != null ? name : "N/A");
                    tvCustomerPhone.setText(phone != null ? phone : "N/A");
                    tvCustomerAddress.setText(address != null ? address : "N/A");
                } else {
                    tvCustomerName.setText("Khách hàng không tồn tại");
                    tvCustomerPhone.setText("N/A");
                    tvCustomerAddress.setText("N/A");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvCustomerName.setText("Lỗi: " + error.getMessage());
                tvCustomerPhone.setText("N/A");
                tvCustomerAddress.setText("N/A");
                Log.e("OrderDetailActivity", "Error loading customer: " + error.getMessage());
            }
        });
    }

    private void fetchOrderItems() {
        DatabaseReference itemsRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId).child("items");
        itemsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderItems.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    OrderItem item = itemSnapshot.getValue(OrderItem.class);
                    if (item != null) {
                        orderItems.add(item);
                    }
                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailAdminActivity.this, "Lỗi khi lấy danh sách sản phẩm: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderDetailActivity", "Error loading order items: " + error.getMessage());
            }
        });
    }

    private void fetchOrderReview() {
        DatabaseReference reviewRef = FirebaseDatabase.getInstance().getReference("reviews").child(orderId);
        reviewRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String comment = snapshot.child("comment").getValue(String.class);
                    Float rating = snapshot.child("rating").getValue(Float.class);

                    // Hiển thị thông tin đánh giá
                    layoutReview.setVisibility(CardView.VISIBLE);
                    tvReviewComment.setText(comment != null ? comment : "Chưa có bình luận");
                    ratingBarReview.setRating(rating != null ? rating : 0);
                } else {
                    layoutReview.setVisibility(CardView.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailAdminActivity.this, "Lỗi khi lấy đánh giá: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderDetailActivity", "Error loading review: " + error.getMessage());
                layoutReview.setVisibility(CardView.GONE);
            }
        });
    }

    private void cancelOrder() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.child("status").setValue("Cancelled").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(OrderDetailAdminActivity.this, "Đơn hàng đã bị hủy!", Toast.LENGTH_SHORT).show();
                spinnerUpdateStatus.setSelection(statusList.indexOf("Cancelled"));
                finish();
            } else {
                Toast.makeText(OrderDetailAdminActivity.this, "Lỗi khi hủy đơn hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStatus(String newStatus) {
        // Kiểm tra nếu trạng thái mới không hợp lệ (quay ngược lại trạng thái trước)
        int currentIndex = statusList.indexOf(currentStatus);
        int newIndex = statusList.indexOf(newStatus);

        if (newIndex < currentIndex) {
            Toast.makeText(OrderDetailAdminActivity.this, "Không thể quay lại trạng thái trước!", Toast.LENGTH_SHORT).show();
            // Đặt lại Spinner về trạng thái hiện tại
            spinnerUpdateStatus.setSelection(currentIndex);
            return;
        }

        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        orderRef.child("status").setValue(newStatus).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(OrderDetailAdminActivity.this, "Cập nhật trạng thái thành công: " + newStatus, Toast.LENGTH_SHORT).show();
                currentStatus = newStatus; // Cập nhật trạng thái hiện tại
                if (newStatus.equals("Cancelled")) {
                    finish();
                } else if (newStatus.equals("Complete")) {
                    fetchOrderReview(); // Lấy đánh giá khi trạng thái chuyển thành Complete
                }
            } else {
                Toast.makeText(OrderDetailAdminActivity.this, "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                // Đặt lại Spinner về trạng thái hiện tại nếu cập nhật thất bại
                spinnerUpdateStatus.setSelection(currentIndex);
            }
        });
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}