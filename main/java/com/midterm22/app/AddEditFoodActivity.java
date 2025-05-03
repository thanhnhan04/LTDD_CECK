package com.midterm22.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.Category;
import com.midterm22.app.model.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditFoodActivity extends AppCompatActivity {

    private EditText edtName, edtPrice, edtDesc, edtImageUrl;
    private Spinner spnCategory;
    private ImageView imgFood;
    private Button btnSave, btnLoadImage;
    private TextView tvTitle;
    private DatabaseReference productRef, categoryRef;
    private String productId = null;
    private Uri imageUri;

    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_food);

        // Cấu hình Toolbar với nút trở về
        Toolbar toolbar = findViewById(R.id.toolbar_food);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(v -> finish());

        TextView toolbarTitle = findViewById(R.id.toolbar_title);

        tvTitle = toolbarTitle;
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDesc = findViewById(R.id.edtDesc);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        spnCategory = findViewById(R.id.spnCategory);
        imgFood = findViewById(R.id.imgFood);
        btnSave = findViewById(R.id.btnSave);
        btnLoadImage = findViewById(R.id.btnLoadImage);

        productRef = FirebaseDatabase.getInstance().getReference("products");
        categoryRef = FirebaseDatabase.getInstance().getReference("categories");

        productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            tvTitle.setText("Chỉnh sửa sản phẩm");
            loadCategoriesThenLoadProduct(productId);
        } else {
            tvTitle.setText("Thêm sản phẩm");
            loadCategories();
        }

        btnSave.setOnClickListener(v -> saveProduct());
        btnLoadImage.setOnClickListener(v -> loadImageFromUrl());
    }

    private void loadCategoriesThenLoadProduct(String productId) {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                List<String> names = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Category cat = data.getValue(Category.class);
                    cat.setId(data.getKey());
                    categoryList.add(cat);
                    names.add(cat.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditFoodActivity.this,
                        android.R.layout.simple_spinner_item, names);
                spnCategory.setAdapter(adapter);
                spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCategoryId = categoryList.get(position).getId();
                    }
                    public void onNothingSelected(AdapterView<?> parent) {}
                });

                loadProductInfo(productId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddEditFoodActivity.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCategories() {
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                List<String> names = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Category cat = data.getValue(Category.class);
                    cat.setId(data.getKey());
                    categoryList.add(cat);
                    names.add(cat.getName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddEditFoodActivity.this,
                        android.R.layout.simple_spinner_item, names);
                spnCategory.setAdapter(adapter);
                spnCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedCategoryId = categoryList.get(position).getId();
                    }
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadProductInfo(String id) {
        productRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    edtName.setText(product.getName());
                    edtPrice.setText(String.valueOf(product.getPrice()));
                    edtDesc.setText(product.getDescription());
                    edtImageUrl.setText(product.getImageUrl());

                    if (!TextUtils.isEmpty(product.getImageUrl())) {
                        Glide.with(AddEditFoodActivity.this)
                                .load(product.getImageUrl())
                                .into(imgFood);
                    }

                    for (int i = 0; i < categoryList.size(); i++) {
                        if (categoryList.get(i).getId().equals(product.getCategoryId())) {
                            spnCategory.setSelection(i);
                            break;
                        }
                    }
                }
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG_PRODUCT", "Error loading product: " + error.getMessage());
                Toast.makeText(AddEditFoodActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadImageFromUrl() {
        String url = edtImageUrl.getText().toString().trim();
        if (!TextUtils.isEmpty(url)) {
            Glide.with(AddEditFoodActivity.this)
                    .load(url)
                    .into(imgFood);
        } else {
            Toast.makeText(this, "Vui lòng nhập URL ảnh", Toast.LENGTH_SHORT).show();
        }
    }


    private void saveProduct() {
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();
        String imageUrl = edtImageUrl.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(desc) || TextUtils.isEmpty(imageUrl)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = productId != null ? productId : productRef.push().getKey();


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());


        saveProductToDatabase(id, name, desc, price, selectedCategoryId, imageUrl);
    }

    private void saveProductToDatabase(String id, String name, String desc, double price, String categoryId, String imageUrl) {
        Product product = new Product(
                id,
                name,
                desc,
                price,
                categoryId,
                imageUrl,
                true,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
        );

        productRef.child(id).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
