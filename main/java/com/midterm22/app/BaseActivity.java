package com.midterm22.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    // Khai báo
    private TextView cartBadge;
    private int cartItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Layout chính sẽ được thiết lập trong setupBottomNavigation
    }

    protected void setupBottomNavigation(int layoutResId) {
        super.setContentView(R.layout.base_activity);

        // Ánh xạ view
        cartBadge = findViewById(R.id.cart_badge);
        ImageButton cartButton = findViewById(R.id.cart_button);

        // Xử lý sự kiện click giỏ hàng
        cartButton.setOnClickListener(v -> {
            if (!isCurrentActivity(CartActivity.class)) {
                navigateToActivity(CartActivity.class);
            }
        });

        // Khởi tạo số lượng (có thể lấy từ database hoặc shared preferences)
        updateCartCount(getCartItemCountFromStorage());

        FrameLayout contentFrame = findViewById(R.id.content_frame);
        LayoutInflater.from(this).inflate(layoutResId, contentFrame, true);

        // Lấy tham chiếu các nút điều hướng
        LinearLayout home = findViewById(R.id.home_id);
        LinearLayout menu = findViewById(R.id.menu_id);
        LinearLayout deal = findViewById(R.id.deal_id);
        LinearLayout more = findViewById(R.id.more_id);

        // Xử lý sự kiện cho nút Home
        home.setOnClickListener(v -> {
            if (!isCurrentActivity(MainActivity.class)) {
                navigateToActivity(MainActivity.class);
            }
        });

        // Xử lý sự kiện cho nút Menu
        menu.setOnClickListener(v -> {
            if (!isCurrentActivity(MenuActivity.class)) {
                navigateToActivity(MenuActivity.class);
            }
        });

        // Xử lý sự kiện cho nút Deal/Khuyến mãi
        deal.setOnClickListener(v -> {
            if (!isCurrentActivity(DealActivity.class)) {
                navigateToActivity(DealActivity.class);
            }
        });

        // Xử lý sự kiện cho nút More
        more.setOnClickListener(v -> {
            if (!isCurrentActivity(MoreActivity.class)) {
                navigateToActivity(MoreActivity.class);
            }
        });

        WebView chatbotWebView = findViewById(R.id.chatbotWebView);
        ImageButton chatbotButton = findViewById(R.id.chatbot_button);
        ImageButton closeChatButton = findViewById(R.id.closeChatButton);

        chatbotButton.setOnClickListener(v -> {
            chatbotWebView.setVisibility(View.VISIBLE);
            closeChatButton.setVisibility(View.VISIBLE);
            chatbotWebView.getSettings().setJavaScriptEnabled(true);
            chatbotWebView.setWebViewClient(new WebViewClient());
            chatbotWebView.loadUrl("https://mobileapp-ccbd4.web.app/#/chat");
        });

        closeChatButton.setOnClickListener(v -> {
            chatbotWebView.setVisibility(View.GONE);
            closeChatButton.setVisibility(View.GONE);
        });


    }
    @Override
    public void onBackPressed() {
        WebView chatbotWebView = findViewById(R.id.chatbotWebView);
        if (chatbotWebView.getVisibility() == View.VISIBLE) {
            chatbotWebView.setVisibility(View.GONE); // Ẩn WebView thay vì thoát app
        } else {
            super.onBackPressed(); // Thoát như bình thường
        }
    }

    // Phương thức cập nhật số lượng giỏ hàng
    public void updateCartCount(int count) {
        cartItemCount = count;
        if (cartBadge != null) {
            if (count > 0) {
                cartBadge.setVisibility(View.VISIBLE);
                cartBadge.setText(String.valueOf(count));

                // Tự động lưu số lượng khi cập nhật
                saveCartItemCountToStorage(count);
            } else {
                cartBadge.setVisibility(View.GONE);
            }
        }
    }

    // Phương thức thêm sản phẩm vào giỏ
    public void addToCart() {
        cartItemCount++;
        updateCartCount(cartItemCount);
    }

    // Phương thức xóa sản phẩm khỏi giỏ
    public void removeFromCart() {
        if (cartItemCount > 0) {
            cartItemCount--;
            updateCartCount(cartItemCount);
        }
    }

    // Phương thức lấy số lượng từ SharedPreferences
    private int getCartItemCountFromStorage() {
        return getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .getInt("CART_ITEM_COUNT", 0);
    }

    // Phương thức lưu số lượng vào SharedPreferences
    private void saveCartItemCountToStorage(int count) {
        getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                .edit()
                .putInt("CART_ITEM_COUNT", count)
                .apply();
    }

    // Kiểm tra Activity hiện tại
    private boolean isCurrentActivity(Class<?> activityClass) {
        return this.getClass().equals(activityClass);
    }

    // Phương thức chuyển Activity
    protected void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    // Lấy số lượng hiện tại trong giỏ hàng
    public int getCurrentCartCount() {
        return cartItemCount;
    }
}