package com.midterm22.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.CartItem;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.OrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private RecyclerView rvPaymentItems;
    private PaymentAdapter paymentAdapter;
    private TextView tvTotalPrice, tvSubTotalPrice;
    private TextView edtName, edtPhone, edtAddress;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Map views
        rvPaymentItems = findViewById(R.id.recyclerOrderItems);
        tvTotalPrice = findViewById(R.id.tvTotal);
        tvSubTotalPrice = findViewById(R.id.tvSubtotal);
        edtName = findViewById(R.id.tv_address);
        edtAddress = findViewById(R.id.tv_name);
        edtPhone = findViewById(R.id.tv_phone);

        // Load user data from Firebase
        loadUserData();

        // Get cart items from Intent
        Intent intent = getIntent();
        ArrayList<CartItem> cartItems = intent.getParcelableArrayListExtra("cart_items");

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Setup RecyclerView adapter
        paymentAdapter = new PaymentAdapter(this, cartItems);
        rvPaymentItems.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentItems.setAdapter(paymentAdapter);

        // Calculate total price
        double total = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();

        tvTotalPrice.setText(String.format(" %.0fđ", total));
        tvSubTotalPrice.setText(String.format(" %.0fđ", total));

        // Back button listener
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Order button listener
        Button btnOrder = findViewById(R.id.btn_order);
        ArrayList<CartItem> finalCartItems = cartItems;
        btnOrder.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String address = edtAddress.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
                return;
            }

            String customerId = currentUser.getUid();
            String orderId = mDatabase.child("orders").push().getKey();
            if (orderId == null) {
                Toast.makeText(this, "Không thể tạo đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            double finalTotal = finalCartItems.stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            // Tạo đơn hàng
            Order order = new Order();
            order.setId(orderId);
            order.setCustomerId(customerId);
            order.setTotal(finalTotal);
            order.setStatus("Pending");
            order.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            order.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

            // Thêm các OrderItem vào order.getItems()
            for (CartItem cartItem : finalCartItems) {
                String orderItemId = mDatabase.child("orderItems").push().getKey();
                if (orderItemId == null) continue;

                OrderItem orderItem = new OrderItem();
                orderItem.setId(orderItemId);
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setProductName(cartItem.getProductName());
                orderItem.setProductImageUrl(cartItem.getProductImageUrl());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(cartItem.getPrice());
                orderItem.setTotalPrice(cartItem.getPrice() * cartItem.getQuantity());

                // Thêm vào Order.items
                order.getItems().put(orderItemId, orderItem);
            }

            // Lưu đơn hàng lên Firebase
            mDatabase.child("orders").child(orderId).setValue(order.toMap())
                    .addOnSuccessListener(aVoid -> {
                        CartStorageHelper.clearCart(PaymentActivity.this, currentUser.getUid());

                        Toast.makeText(PaymentActivity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                        Intent menuIntent = new Intent(PaymentActivity.this, MenuActivity.class);
                        menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(menuIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PaymentActivity.this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = mDatabase.child("User").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        String address = dataSnapshot.child("address").getValue(String.class);

                        edtName.setText("Tên: "+name);
                        edtPhone.setText("Số điện thoại: "+phone);
                        edtAddress.setText("Địa chỉ: "+address);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(PaymentActivity.this, "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
