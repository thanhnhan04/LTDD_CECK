package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm22.app.model.Cart;
import com.midterm22.app.model.CartItem;
import com.midterm22.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class CartActivity extends AppCompatActivity {
    private RecyclerView rvSelectedItems;
    private TextView tvTotalPrice;
    private CartAdapter cartAdapter;
    private Cart cart;
    ImageButton btnBack;
    Button btnBuyNow;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            return;
        }

         btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        cart = CartStorageHelper.getCart(this, currentUser.getUid());
        Map<String, CartItem> items = cart.getItems();

        for (CartItem item : items.values()) {
            Log.d("CartCheck", "Item: " + item.getProductName() + ", quantity: " + item.getQuantity());
        }

        tvTotalPrice = findViewById(R.id.tv_total_price);

        cartAdapter = new CartAdapter(cart.getItems());
        rvSelectedItems = findViewById(R.id.rv_selected_items);
        rvSelectedItems.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedItems.setAdapter(cartAdapter);

        cartAdapter.setOnCartTotalChangedListener(total -> {
            updateTotalPrice();
        });

        updateTotalPrice();

         btnBuyNow = findViewById(R.id.btn_buy_now);
        btnBuyNow.setOnClickListener(v -> {
            Intent paymentIntent = new Intent(CartActivity.this, PaymentActivity.class);
            double totalPrice = 0;
            StringBuilder productNames = new StringBuilder();

            for (CartItem item : cart.getItems().values()) {
                totalPrice += item.getPrice() * item.getQuantity();
                productNames.append(item.getProductName())
                        .append(" x")
                        .append(item.getQuantity())
                        .append("\n");
            }

            paymentIntent.putExtra("total_price", totalPrice);
            paymentIntent.putExtra("product_name", productNames.toString().trim()); // Truyền tên món ăn gộp

            startActivity(paymentIntent);
        });

    }

    private void updateTotalPrice() {
        double total = 0;
        for (CartItem item : cart.getItems().values()) {
            total += item.getPrice() * item.getQuantity();
        }
        tvTotalPrice.setText(String.format("%.0fđ", total));
    }
}
