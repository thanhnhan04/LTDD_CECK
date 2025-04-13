package com.midterm22.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import com.midterm22.app.model.Cart;
import com.midterm22.app.model.CartItem;
import com.midterm22.app.model.Product;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    private TextView tvProductName, tvProductPrice, tvProductDescription, tvQuantity, tvTotalPrice;
    private ImageView imgProduct;
    private ImageButton btnDecrease, btnIncrease;
    private Button btnAddToCart, btnBuyNow;
    private int quantity = 1;
    private double productPrice = 35000; // mặc định, sẽ được ghi đè
    private String productId;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        context = ProductDetailActivity.this;

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

        tvQuantity.setText(String.valueOf(quantity));
        tvTotalPrice.setText(formatCurrency(quantity * productPrice));

        // Sự kiện nút tăng/giảm
        btnIncrease.setOnClickListener(v -> {
            quantity++;
            tvQuantity.setText(String.valueOf(quantity));
            updateTotal();
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotal();
            }
        });

        // Nút quay lại
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Nhận product_id từ Intent
        productId = getIntent().getStringExtra("product_id");
        if (productId == null) {
            finish();
            return;
        }

        // Load dữ liệu sản phẩm từ Firebase
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(productId);
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Product product = snapshot.getValue(Product.class);
                    if (product != null) {
                        displayProductDetails(product);

                        // Sự kiện thêm vào giỏ hàng
                        btnAddToCart.setOnClickListener(v -> {
                            CartItem item = new CartItem();
                            item.setProductId(productId);
                            item.setProductName(product.getName());
                            item.setProductImageUrl(product.getImageUrl());
                            item.setPrice(product.getPrice());
                            item.setQuantity(quantity); // lấy số lượng hiện tại
                            item.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                            Log.d("AddToCart", "Add to cart clicked for product: " + item.getProductName());
                            addToCart(item);

                            Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                        });

                        // Sự kiện mua ngay
                        btnBuyNow.setOnClickListener(v -> {
                            Intent intent = new Intent(ProductDetailActivity.this, PaymentActivity.class);
                            intent.putExtra("product_name", product.getName());
                            intent.putExtra("product_price", product.getPrice());
                            intent.putExtra("quantity", quantity);
                            intent.putExtra("total_price", quantity * product.getPrice());
                            startActivity(intent);
                        });
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

        productPrice = product.getPrice(); // gán giá sản phẩm
        updateTotal();

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

    private void addToCart(CartItem cartItem) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("AddToCart", "User is not logged in.");
            return;
        }

        String userId = currentUser.getUid();
        Cart cart = CartStorageHelper.getCart(context, userId);

        if (cart.getItems().containsKey(cartItem.getProductId())) {
            CartItem existing = cart.getItems().get(cartItem.getProductId());
            existing.setQuantity(existing.getQuantity() + cartItem.getQuantity());
        } else {
            cart.getItems().put(cartItem.getProductId(), cartItem);
        }

        CartStorageHelper.saveCart(context, cart, userId);
        Log.d("AddToCart", "Item saved. Total items: " + cart.getItems().size());
    }

    private String formatCurrency(double amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
