package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.midterm22.app.model.Cart;
import com.midterm22.app.model.CartItem;
import com.midterm22.app.R;

import java.util.ArrayList;
import java.util.Map;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvSelectedItems;
    private TextView tvTotalPrice;
    private CartAdapter cartAdapter;
    private Cart cart;
    private ImageButton btnBack;
    private Button btnBuyNow;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Kiểm tra nếu người dùng chưa đăng nhập
        if (currentUser == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu người dùng chưa đăng nhập
            return;
        }

        // Khởi tạo các thành phần UI
        btnBack = findViewById(R.id.btn_back);
        btnBuyNow = findViewById(R.id.btn_buy_now);
        tvTotalPrice = findViewById(R.id.tv_total_price);

        // Xử lý sự kiện khi quay lại
        btnBack.setOnClickListener(v -> finish());

        // Lấy giỏ hàng từ CartStorageHelper
        cart = CartStorageHelper.getCart(this, currentUser.getUid());

        if (cart == null || cart.getItems().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng của bạn trống. Vui lòng chọn sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị sản phẩm trong giỏ hàng
        Map<String, CartItem> items = cart.getItems();
        for (CartItem item : items.values()) {
            Log.d("CartCheck", "Item: " + item.getProductName() + ", quantity: " + item.getQuantity());
        }

        // Khởi tạo RecyclerView cho giỏ hàng
        cartAdapter = new CartAdapter(cart.getItems());
        rvSelectedItems = findViewById(R.id.rv_selected_items);
        rvSelectedItems.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedItems.setAdapter(cartAdapter);

        // Lắng nghe sự thay đổi tổng giá trị trong giỏ hàng
        cartAdapter.setOnCartTotalChangedListener(this::updateTotalPrice);

        // Cập nhật tổng giá trị ban đầu
        updateTotalPrice();

        // Xử lý sự kiện khi nhấn nút "Mua ngay"
        btnBuyNow.setOnClickListener(v -> {
            // Kiểm tra nếu giỏ hàng không trống
            if (cart.getItems().isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn trống. Không thể tiếp tục thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Chuyển dữ liệu sang PaymentActivity
            Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
            intent.putParcelableArrayListExtra("cart_items", new ArrayList<>(cart.getItems().values())); // Truyền danh sách món ăn
            startActivity(intent);
        });
    }

    private void updateTotalPrice(double v) {
        double total = 0;
        for (CartItem item : cart.getItems().values()) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format("%.0fđ", total));
    }

    // Cập nhật tổng giá trị trong giỏ hàng
    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cart.getItems().values()) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format("%.0fđ", total));
    }
}
