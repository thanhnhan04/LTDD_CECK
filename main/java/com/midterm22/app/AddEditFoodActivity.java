package com.midterm22.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.midterm22.app.model.Category;
import com.midterm22.app.model.Product;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddEditFoodActivity extends AppCompatActivity {

    private EditText edtName, edtPrice, edtDesc;
    private Spinner spnCategory;
    private ImageView imgFood;
    private Button btnSelectImage, btnSave;
    private TextView tvTitle;
    private DatabaseReference productRef, categoryRef;
    private StorageReference storageRef;
    private String productId = null;
    private Uri imageUri;

    private List<Category> categoryList = new ArrayList<>();
    private String selectedCategoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_food);
        tvTitle = findViewById(R.id.toolbar_title);
        // Ánh xạ view
        edtName = findViewById(R.id.edtName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDesc = findViewById(R.id.edtDesc);
        spnCategory = findViewById(R.id.spnCategory);
        imgFood = findViewById(R.id.imgFood);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSave = findViewById(R.id.btnSave);

        productRef = FirebaseDatabase.getInstance().getReference("products");
        storageRef = FirebaseStorage.getInstance().getReference("food_images");
         categoryRef = FirebaseDatabase.getInstance().getReference("categories");

        loadCategories();
        productId = getIntent().getStringExtra("productId");
        if (productId != null) {
            tvTitle.setText("Chỉnh sửa sản phẩm");
            loadProductInfo(productId); // Tải thông tin sản phẩm nếu là chỉnh sửa
        } else {
            tvTitle.setText("Thêm sản phẩm");
        }
        if (productId != null) {
            loadProductInfo(productId);
        }

        btnSelectImage.setOnClickListener(v -> selectImage());
        btnSave.setOnClickListener(v -> saveProduct());
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
    private boolean isNewImage = false;
    private String oldImageUrl = "";

    private void loadProductInfo(String id) {
        productRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    edtName.setText(product.getName());
                    edtPrice.setText(String.valueOf(product.getPrice()));
                    edtDesc.setText(product.getDescription());
                    oldImageUrl = product.getImageUrl(); // Lưu URL ảnh cũ

                    if (!TextUtils.isEmpty(oldImageUrl)) {
                        Glide.with(AddEditFoodActivity.this)
                                .load(oldImageUrl)
                                .into(imgFood);
                    }

                    // Reset cờ ảnh mới
                    isNewImage = false;
                }
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("DEBUG_PRODUCT", "Error loading product: " + error.getMessage());
                Toast.makeText(AddEditFoodActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Thêm các hằng số và biến mới
    private static final int PICK_IMAGE_REQUEST = 1001;
    private boolean isNewImageSelected = false;
    private String existingImageUrl = "";

    // Sửa phương thức selectImage
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), PICK_IMAGE_REQUEST);
    }

    // Sửa onActivityResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                // Hiển thị preview ảnh
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                imgFood.setImageBitmap(bitmap);
                isNewImageSelected = true;
            } catch (IOException e) {
                Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void saveProduct() {
        String name = edtName.getText().toString().trim();
        String priceStr = edtPrice.getText().toString().trim();
        String desc = edtDesc.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(desc)) {
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

        // Lấy ID hiện tại hoặc tạo mới
        String id = productId != null ? productId : productRef.push().getKey();

        // Lấy categoryId từ Intent nếu có
        String categoryId = getIntent().getStringExtra("categoryId"); // Hoặc lấy từ nơi khác nếu đã gán sẵn

        // Lấy ngày giờ hiện tại
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        if (isNewImageSelected && imageUri != null) {
            // Upload ảnh mới
            StorageReference fileRef = storageRef.child(id + "_" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                saveProductToDatabase(id, name, desc, price, selectedCategoryId, uri.toString());
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Giữ nguyên ảnh cũ
            saveProductToDatabase(id, name, desc, price, selectedCategoryId, existingImageUrl);
        }
    }

    // Sửa phương thức saveProductToDatabase
    private void saveProductToDatabase(String id, String name, String desc, double price, String categoryId, String imageUrl) {
        // Tạo đối tượng Product mới
        Product product = new Product(
                id,
                name,
                desc,
                price,
                categoryId,
                imageUrl, // Sử dụng URL mới hoặc cũ
                true,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())
        );

        // Lưu vào Firebase Realtime Database
        productRef.child(id).setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish(); // Thoát activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi khi lưu sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
