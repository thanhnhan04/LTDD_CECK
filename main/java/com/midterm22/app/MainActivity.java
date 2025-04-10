package com.midterm22.app;

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


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    TextView tv_name;
    RecyclerView recyclerProducts;
    ProductAdapter productAdapter;
    List<Product> productList = new ArrayList<>();
    DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting MainActivity"); // Log 1

        setupBottomNavigation(R.layout.activity_main);
        Log.d(TAG, "Bottom navigation setup completed"); // Log 2

        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);
        if (viewFlipper != null) {
            viewFlipper.startFlipping();
            Log.d(TAG, "ViewFlipper started"); // Log 3
        } else {
            Log.e(TAG, "ViewFlipper not found"); // Error log
        }

        // Kiểm tra user name
        tv_name = findViewById(R.id.tv_name);
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userName = sharedPref.getString("user_name", "");
        Log.d(TAG, "Retrieved user name: " + userName); // Log 4
        tv_name.setText(userName);

        // Kiểm tra RecyclerView
        recyclerProducts = findViewById(R.id.recyclerViewProduct);
        if (recyclerProducts == null) {
            Log.e(TAG, "RecyclerView not found");
            return;
        }

        recyclerProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        productAdapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);
        Log.d(TAG, "RecyclerView setup completed"); // Log 5

        // Kiểm tra Firebase
        try {
            productRef = FirebaseDatabase.getInstance().getReference("products");
            Log.d(TAG, "Firebase reference obtained"); // Log 6
            loadAllProducts();
        } catch (Exception e) {
            Log.e(TAG, "Firebase error: " + e.getMessage(), e); // Error log
        }
    }

    private void loadAllProducts() {
        Log.d(TAG, "Attempting to load products from Firebase"); // Log 7

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange: Data snapshot received"); // Log 8
                productList.clear();

                int productCount = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null && product.isAvailable()) {
                        productList.add(product);
                        productCount++;
                        Log.d(TAG, "Added product: " + product.getName()); // Log từng sản phẩm
                    }
                }

                Log.d(TAG, "Total products loaded: " + productCount); // Log 9
                productAdapter.setProductList(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage(), error.toException()); // Error log
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}