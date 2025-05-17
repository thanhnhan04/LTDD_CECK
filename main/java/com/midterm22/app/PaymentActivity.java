package com.midterm22.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity {

    private RecyclerView rvPaymentItems;
    private PaymentAdapter paymentAdapter;
    private TextView tvTotalPrice, tvSubTotalPrice;
    private TextView edtName, edtPhone, edtAddress, edtNote;
    private TextView paymentTextView;
    private RadioGroup radioGroup;
    private DatabaseReference mDatabase;
    private Button btnOrder;
    private ActivityResultLauncher<Intent> vnpayLauncher;

    private ArrayList<CartItem> finalCartItems;
    private String currentCustomerId;
    private String pendingOrderId;

    private double finalTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        vnpayLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String paymentResult = result.getData().getStringExtra("payment_result");
                        if ("success".equals(paymentResult)) {
                            createOrder("VNPay", pendingOrderId);
                        } else {
                            Toast.makeText(PaymentActivity.this, "Thanh toán không thành công!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Ánh xạ view
        rvPaymentItems = findViewById(R.id.recyclerOrderItems);
        tvTotalPrice = findViewById(R.id.tvTotal);
        tvSubTotalPrice = findViewById(R.id.tvSubtotal);
        edtName = findViewById(R.id.tv_address);
        edtAddress = findViewById(R.id.tv_name);
        edtPhone = findViewById(R.id.tv_phone);
        edtNote = findViewById(R.id.edt_note);
        radioGroup = findViewById(R.id.radioPaymentMethods);
        paymentTextView = findViewById(R.id.paymentMethodTextView);
        btnOrder = findViewById(R.id.btn_order);

        // Load dữ liệu người dùng
        loadUserData();

        // Lấy danh sách giỏ hàng
        Intent intent = getIntent();
        finalCartItems = intent.getParcelableArrayListExtra("cart_items");
        if (finalCartItems == null) finalCartItems = new ArrayList<>();

        // Setup adapter
        paymentAdapter = new PaymentAdapter(this, finalCartItems);
        rvPaymentItems.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentItems.setAdapter(paymentAdapter);

        // Tổng tiền
        finalTotal = finalCartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        DecimalFormat formatter = new DecimalFormat("#,###");
        tvTotalPrice.setText(String.format("%sđ", formatter.format(finalTotal)));
        tvSubTotalPrice.setText(String.format("%sđ", formatter.format(finalTotal)));


        // Chọn phương thức thanh toán
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioVNPay) {
                paymentTextView.setText("VNPay");
            } else {
                paymentTextView.setText("Cash");
            }
        });

        // Nút back
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Nút đặt hàng
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

            currentCustomerId = currentUser.getUid();

            // Tạo trước orderId
            pendingOrderId = mDatabase.child("orders").push().getKey();
            if (pendingOrderId == null) {
                Toast.makeText(this, "Không thể tạo đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("VNPay".equals(paymentTextView.getText().toString())) {
                String paymentUrl = VnpayHelper.createPaymentUrl(pendingOrderId,String.valueOf((int) finalTotal), "", "vn");
                Intent intentt = new Intent(PaymentActivity.this, VNPayWebViewActivity.class);
                intentt.putExtra(VNPayWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl);
                vnpayLauncher.launch(intentt);

            } else {
                createOrder("Cash", pendingOrderId);
            }
        });


    }

    private void createOrder(String paymentMethod, String orderId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String customerId = currentUser.getUid();

        String note = edtNote.getText().toString().trim();

        Order order = new Order();
        order.setId(orderId);
        order.setCustomerId(customerId);
        order.setTotal(finalTotal);
        order.setStatus("Pending");
        order.setPaymentMethod(paymentMethod);
        order.setNote(note.isEmpty() ? "" : note);
        order.setCreatedAt(String.valueOf(System.currentTimeMillis()));
        order.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

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

            order.getItems().put(orderItemId, orderItem);
        }

        mDatabase.child("orders").child(orderId).setValue(order.toMap())
                .addOnSuccessListener(aVoid -> {
                    CartStorageHelper.clearCart(PaymentActivity.this, customerId);
                    Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, PaymentSuccessActivity.class);
                    intent.putExtra("order_id", pendingOrderId);
                    intent.putExtra("amount", finalTotal);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi đặt hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = mDatabase.child("User").child(userId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        edtName.setText(snapshot.child("name").getValue(String.class));
                        edtPhone.setText(snapshot.child("phone").getValue(String.class));
                        edtAddress.setText(snapshot.child("address").getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(PaymentActivity.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}