package com.midterm22.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.OrderItem;
import com.midterm22.app.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvProductDescription, tvQuantity, tvTotalPrice;
    private ImageView imgProduct;
    private Button btnDecrease, btnIncrease, btnAddToCart, btnBuyNow;
    private int quantity = 1;
    private double productPrice = 35000; // giả định
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Ánh xạ view
        tvProductName = findViewById(R.id.tv_product_name);
        tvProductPrice = findViewById(R.id.tv_product_price);
        tvProductDescription = findViewById(R.id.tv_product_description);
        tvQuantity = findViewById(R.id.tv_quantity);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        imgProduct = findViewById(R.id.img_product);

        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBuyNow = findViewById(R.id.btn_buy_now);

        // Sự kiện nút
        btnDecrease.setOnClickListener(v -> changeQuantity(-1));
        btnIncrease.setOnClickListener(v -> changeQuantity(1));

        // Cập nhật giao diện
        updateTotal();

        // Nút quay lại
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Nhận orderId từ Intent
        productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            finish();
            return;
        }

        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        displayProductDetails(product);
                    }
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void displayProductDetails(Product product) {
        tvProductName.setText(product.getName());
        tvProductPrice.setText(formatCurrency(product.getPrice()));
        tvProductDescription.setText(product.getDescription());

        productPrice = product.getPrice(); // Gán giá để tính tổng
        updateTotal();

        // Load ảnh nếu có (giả sử dùng Glide)
        if (product.getImageUrl() != null) {
            Glide.with(this).load(product.getImageUrl()).into(imgProduct);
        }
    }

    private void changeQuantity(int delta) {
        quantity += delta;
        if (quantity < 1) quantity = 1;
        tvQuantity.setText(String.valueOf(quantity));
        updateTotal();
    }

    private void updateTotal() {
        double total = quantity * productPrice;
        tvTotalPrice.setText(formatCurrency(total));
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
