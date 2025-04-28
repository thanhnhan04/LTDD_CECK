package com.midterm22.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.midterm22.app.model.User;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etAddress;
    private Button btnUpdate;
    private DatabaseReference userRef;
    private String userId;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        loadUserData();
        setupUpdateButton();
    }

    private void initViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);

            // Lấy thông tin người dùng từ Firebase Realtime Database
            userRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String phone = dataSnapshot.child("phone").getValue(String.class);
                        String address = dataSnapshot.child("address").getValue(String.class);

                        // Hiển thị dữ liệu lên các trường nhập liệu
                        etName.setText(name);
                        etEmail.setText(email);
                        etPhone.setText(phone);
                        etAddress.setText(address);
                    }
                } else {
                    Toast.makeText(this, "Lỗi khi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            finish(); // Hoặc điều hướng người dùng về màn hình đăng nhập
        }
    }

    private void setupUpdateButton() {
        btnUpdate.setOnClickListener(v -> updateProfile());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setName(name);
        updatedUser.setEmail(email);
        updatedUser.setPhone(phone);
        updatedUser.setAddress(address);
        updatedUser.setRole("customer");
        updatedUser.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

        // Cập nhật vào Firebase Realtime Database
        userRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    // Nếu người dùng thay đổi email trong Firebase Auth, cập nhật email.
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null && !user.getEmail().equals(email)) {
                        user.updateEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Cập nhật email thành công", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(this, "Cập nhật email thất bại: " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
