package com.midterm22.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Product;

import java.util.ArrayList;
import java.util.List;

public class MenuActivity extends BaseActivity {

    TextView tab_must_try, tab_burger, tab_pizza, tab_fries, tab_drink;
    RecyclerView recyclerProducts;
    ProductAdapter productAdapter;
    List<Product> productList = new ArrayList<>();
    DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_menu);

        tab_must_try = findViewById(R.id.tab_must_try);
        tab_burger = findViewById(R.id.tab_burger);
        tab_pizza = findViewById(R.id.tab_pizza);
        tab_fries = findViewById(R.id.tab_fries);
        tab_drink = findViewById(R.id.tab_drink);

        recyclerProducts = findViewById(R.id.recycler_product);
        recyclerProducts.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);
        productRef = FirebaseDatabase.getInstance().getReference("products");
        loadAllProducts();

        tab_must_try.setOnClickListener(v -> loadAllProducts());
        tab_burger.setOnClickListener(v -> loadProductsByCategory("cat_burger"));
        tab_pizza.setOnClickListener(v -> loadProductsByCategory("cat_pizza"));
        tab_fries.setOnClickListener(v -> loadProductsByCategory("cat_fries"));
        tab_drink.setOnClickListener(v -> loadProductsByCategory("cat_drinks"));
    }

    private void loadProductsByCategory(String categoryId) {
        productRef.orderByChild("categoryId").equalTo(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Product product = data.getValue(Product.class);
                            if (product != null && product.isAvailable()) {
                                Log.d("DEBUG_PRODUCT", "Name: " + product.getName() + ", Image: " + product.getImageUrl());
                                productList.add(product);
                            }
                        }
                        Log.d("DEBUG_LIST_SIZE", "Product list size: " + productList.size()); // Thêm dòng này
                        productAdapter.setProductList(productList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FIREBASE_ERROR", error.getMessage()); // Ghi log lỗi chi tiết
                        Toast.makeText(MenuActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void loadAllProducts() {
        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null && product.isAvailable()) {
                        productList.add(product);
                    }
                }
                productAdapter.setProductList(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MenuActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
