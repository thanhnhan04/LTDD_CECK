package com.midterm22.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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


public class MainActivity extends BaseActivity implements ProductAdapter.OnProductClickListener {
    private static final String TAG = "MainActivity";
    TextView tv_name;
    RecyclerView recyclerProducts,recyclerCombo;
    ProductAdapter productAdapter,comboAdapter;
    List<Product> productList = new ArrayList<>();
    List<Product> comboList = new ArrayList<>();
    DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainActivity");

        setupBottomNavigation(R.layout.activity_main);
        Log.d(TAG, "Bottom navigation setup completed");

        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);
        if (viewFlipper != null) {
            viewFlipper.startFlipping();
            Log.d(TAG, "ViewFlipper started");
        } else {
            Log.e(TAG, "ViewFlipper not found");
        }

        tv_name = findViewById(R.id.tv_name);
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userName = sharedPref.getString("user_name", "");
        Log.d(TAG, "Retrieved user name: " + userName);
        tv_name.setText(userName);

        recyclerProducts = findViewById(R.id.recyclerViewProduct);
        if (recyclerProducts == null) {
            Log.e(TAG, "RecyclerView not found");
            return;
        }

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productAdapter = new ProductAdapter(this, productList, this);
        recyclerProducts.setAdapter(productAdapter);
        Log.d(TAG, "RecyclerView setup completed");
        recyclerCombo = findViewById(R.id.recyclerViewCombo);
        recyclerCombo.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        comboAdapter = new ProductAdapter(this, comboList, this);
        recyclerCombo.setAdapter(comboAdapter);

        try {
            productRef = FirebaseDatabase.getInstance().getReference("products");
            Log.d(TAG, "Firebase reference obtained");
            loadAllProducts();
        } catch (Exception e) {
            Log.e(TAG, "Firebase error: " + e.getMessage(), e);
        }
        loadComboProducts();

    }

    private void loadAllProducts() {
        Log.d(TAG, "Attempting to load products from Firebase");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Data snapshot received");
                productList.clear();

                int productCount = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null && product.isAvailable()) {
                        String name = product.getName().toLowerCase();
                        if (!name.contains("combo")) {
                            productList.add(product);
                            productCount++;
                            Log.d(TAG, "Added product: " + product.getName());
                        } else {
                            Log.d(TAG, "Skipped combo product: " + product.getName());
                        }
                    }
                }

                Log.d(TAG, "Total non-combo products loaded: " + productCount);
                productAdapter.setProductList(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage(), error.toException());
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComboProducts() {
        Log.d(TAG, "Loading combo products...");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comboList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null && product.isAvailable()) {
                        // Lọc sản phẩm có chứa chữ "Combo" trong tên
                        if (product.getName().toLowerCase().contains("combo")) {
                            comboList.add(product);
                            Log.d(TAG, "Combo found: " + product.getName());
                        }
                    }
                }
                comboAdapter.setProductList(comboList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled (combo): " + error.getMessage(), error.toException());
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class); // thay CurrentActivity bằng tên activity hiện tại
        // Nếu muốn truyền dữ liệu, thêm vào intent.putExtra()
        intent.putExtra("product_id", product.getId()); // ví dụ truyền id món ăn
        startActivity(intent);
    }
}