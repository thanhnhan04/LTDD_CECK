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

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.OrderItem;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvQuantity, tvTotalPrice;
    private EditText edtName, edtPhone;
    private DatabaseReference mDatabase;

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Map views
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvTotalPrice = findViewById(R.id.txt_total);



        edtName = findViewById(R.id.edt_name);
        edtPhone = findViewById(R.id.edt_phone);

        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        double productPrice = intent.getDoubleExtra("product_price", 0);
        int quantity = intent.getIntExtra("quantity", 1);
        double totalPrice = intent.getDoubleExtra("total_price", 0);


        sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        tvProductName.setText(productName);
        tvProductPrice.setText(formatCurrency(productPrice));
        tvQuantity.setText(String.valueOf(quantity));
        tvTotalPrice.setText(formatCurrency(totalPrice));

        loadUserData();

        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        Button btnOrder = findViewById(R.id.btn_order);
        btnOrder.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if (name.isEmpty() || phone.isEmpty()) {
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

            Order order = new Order();
            order.setId(orderId);
            order.setCustomerId(customerId);
            order.setTotal(totalPrice);
            order.setStatus("pending");
            order.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            order.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

            OrderItem item = new OrderItem();
            item.setId(orderId + "_1");
            item.setProductId("product_id");
            item.setProductName(productName);
            item.setUnitPrice(productPrice);
            item.setQuantity(quantity);
            item.setTotalPrice(productPrice * quantity);

            order.getItems().put(item.getId(), item);

            mDatabase.child("orders").child(orderId).setValue(order)
                    .addOnSuccessListener(aVoid -> {
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
        String userName = sharedPref.getString("user_name", "");
        edtName.setText(userName);
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
