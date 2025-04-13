package com.midterm22.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Order;
import com.midterm22.app.model.Product;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends BaseActivity implements ProductAdapter.OnProductClickListener {

    // UI Components
    TextView tab_must_try, tab_burger, tab_pizza, tab_fries, tab_drink,
            tab_combo, tab_chicken, tab_noodles; // Thêm 3 TextView mới
    RecyclerView recyclerProducts;
    ProductAdapter productAdapter;

    List<Product> productList = new ArrayList<>();
    DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_menu);

        // Initialize Views
        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Load default data
        loadAllProducts();
    }

    private void initViews() {
        tab_must_try = findViewById(R.id.tab_must_try);
        tab_burger = findViewById(R.id.tab_burger);
        tab_pizza = findViewById(R.id.tab_pizza);
        tab_fries = findViewById(R.id.tab_fries);
        tab_drink = findViewById(R.id.tab_drink);

        // Thêm 3 tab mới (đảm bảo đã có trong layout XML)
        tab_combo = findViewById(R.id.tab_combo);
        tab_chicken = findViewById(R.id.tab_chicken);
        tab_noodles = findViewById(R.id.tab_noodles);

        recyclerProducts = findViewById(R.id.recycler_product);
    }

    private void setupRecyclerView() {
        recyclerProducts.setHasFixedSize(true);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2)); // 2 cột
        productAdapter = new ProductAdapter(this, productList,this);
        recyclerProducts.setAdapter(productAdapter);
        productRef = FirebaseDatabase.getInstance().getReference("products");
    }

    private void setupClickListeners() {
        // Các tab cũ
        tab_must_try.setOnClickListener(v -> loadAllProducts());
        tab_burger.setOnClickListener(v -> {
            resetAllTabs();
            tab_burger.setTextColor(getColor(R.color.black));
            tab_burger.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_burger");
        });
        tab_pizza.setOnClickListener(v -> {
            resetAllTabs();
            tab_pizza.setTextColor(getColor(R.color.black));
            tab_pizza.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_pizza");
        });
        tab_fries.setOnClickListener(v -> {
            resetAllTabs();
            tab_fries.setTextColor(getColor(R.color.black));
            tab_fries.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_fries");
        });
        tab_drink.setOnClickListener(v -> {
            resetAllTabs();
            tab_drink.setTextColor(getColor(R.color.black));
            tab_drink.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_drinks");
        });
        tab_combo.setOnClickListener(v -> {
            resetAllTabs();
            tab_combo.setTextColor(getColor(R.color.black));
            tab_combo.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_combo");
        });
        tab_chicken.setOnClickListener(v -> {
            resetAllTabs();
            tab_chicken.setTextColor(getColor(R.color.black));
            tab_chicken.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_chicken");
        });

        tab_noodles.setOnClickListener(v -> {
            resetAllTabs();
            tab_noodles.setTextColor(getColor(R.color.black));
            tab_noodles.setTypeface(null, Typeface.BOLD);
            loadProductsByCategory("cat_noodles");
        });
//        tab_pizza.setOnClickListener(v -> loadProductsByCategory("cat_pizza"));
//        tab_fries.setOnClickListener(v -> loadProductsByCategory("cat_fries"));
//        tab_drink.setOnClickListener(v -> loadProductsByCategory("cat_drinks"));
//
//        // 3 tab mới
//        tab_combo.setOnClickListener(v -> loadProductsByCategory("cat_combo"));
//        tab_chicken.setOnClickListener(v -> loadProductsByCategory("cat_chicken"));
//        tab_noodles.setOnClickListener(v -> loadProductsByCategory("cat_noodles"));

    }

    private void loadProductsByCategory(String categoryId) {
        showProgressBar();

        productRef.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        hideProgressBar();
                        productList.clear();

                        if (!snapshot.exists()) {
                            showEmptyState();
                            return;
                        }

                        for (DataSnapshot data : snapshot.getChildren()) {
                            Product product = data.getValue(Product.class);
                            if (product != null && product.isAvailable()) {
                                productList.add(product);
                            }
                        }

                        if (productList.isEmpty()) {
                            showEmptyState();
                        } else {
                            productAdapter.setProductList(productList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        hideProgressBar();
                        Log.e("FIREBASE_ERROR", "Failed to load products: " + error.getMessage());
                        Toast.makeText(MenuActivity.this,
                                "Lỗi tải danh sách món: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadAllProducts() {
        showProgressBar();

        resetAllTabs();
        tab_must_try.setTextColor(getColor(R.color.black)); // Màu chữ đậm
        tab_must_try.setTypeface(null, Typeface.BOLD);      // In đậm

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideProgressBar();
                productList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null && product.isAvailable()) {
                        productList.add(product);
                    }
                }

                if (productList.isEmpty()) {
                    showEmptyState();
                } else {
                    productAdapter.setProductList(productList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideProgressBar();
                Toast.makeText(MenuActivity.this,
                        "Lỗi tải tất cả món: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
    private void resetAllTabs() {
        TextView[] tabs = { tab_must_try, tab_burger, tab_pizza, tab_fries, tab_drink, tab_combo, tab_chicken, tab_noodles };
        for (TextView tab : tabs) {
            tab.setTextColor(getColor(R.color.gray)); // hoặc "#888888"
            tab.setTypeface(null, Typeface.NORMAL);   // bỏ in đậm
        }
    }

    // Helper methods
    private void showProgressBar() {
        // Thêm code hiển thị progress bar nếu cần
    }

    private void hideProgressBar() {
        // Thêm code ẩn progress bar nếu cần
    }

    private void showEmptyState() {
        // Hiển thị layout "Không có món nào" (ví dụ: TextView hoặc ImageView)
        Toast.makeText(this, "Không có món nào trong danh mục này", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(MenuActivity.this, ProductDetailActivity.class); // thay CurrentActivity bằng tên activity hiện tại
        // Nếu muốn truyền dữ liệu, thêm vào intent.putExtra()
        intent.putExtra("product_id", product.getId()); // ví dụ truyền id món ăn
        startActivity(intent);
    }
}