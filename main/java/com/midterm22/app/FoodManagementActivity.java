package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Product;

import java.util.ArrayList;
import java.util.List;

public class FoodManagementActivity extends AppCompatActivity {

    private RecyclerView rvFoods;
    private FloatingActionButton btnAddFood;
    private ProductAdapter adapter;
    private List<Product> foodList;
    private DatabaseReference productRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_management);

        rvFoods = findViewById(R.id.rvFoods);
        btnAddFood = findViewById(R.id.btnAddFood);
        rvFoods.setLayoutManager(new LinearLayoutManager(this));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        foodList = new ArrayList<>();
        adapter = new ProductAdapter(this, foodList);
        rvFoods.setAdapter(adapter);

        // Firebase
        productRef = FirebaseDatabase.getInstance().getReference("products");

        loadFoods(); // Load dữ liệu sản phẩm từ Firebase

        // Sự kiện nút thêm món ăn
        btnAddFood.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditFoodActivity.class));
        });

        // Xử lý khi click vào một món ăn để sửa
        adapter.setOnFoodActionListener(new ProductAdapter.OnFoodActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(FoodManagementActivity.this, AddEditFoodActivity.class);
                intent.putExtra("productId", product.getId()); // Key phải khớp với AddEditFoodActivity
                startActivity(intent);
            }
            @Override
            public void onHide(Product product) {
                // Cập nhật Firebase để ẩn món ăn
                DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
                productRef.child("available").setValue(false);
            }
        });

    }

    // Xử lý chỉnh sửa món ăn
    public void onEdit(Product product) {
        Log.d("DEBUG_EDIT", "Đã nhấn nút sửa sản phẩm: " + product.getName());
        Intent intent = new Intent(FoodManagementActivity.this, AddEditFoodActivity.class);
        intent.putExtra("id", product.getId());
        intent.putExtra("name", product.getName());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("imageUrl", product.getImageUrl());
        startActivity(intent);
    }

    // Cập nhật trạng thái ẩn hiện của món ăn
    private void updateAvailability(Product product, boolean isAvailable) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
        productRef.child("isAvailable").setValue(isAvailable)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FoodManagementActivity.this, isAvailable ? "Món ăn đã được hiển thị!" : "Món ăn đã bị ẩn!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FoodManagementActivity.this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Tải danh sách món ăn từ Firebase
    private void loadFoods() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        product.setId(data.getKey());
                        foodList.add(product);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodManagementActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
