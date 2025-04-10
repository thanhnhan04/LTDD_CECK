package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
        loadIntentData();
        setupFirebase();
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

    private void loadIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            etName.setText(intent.getStringExtra("user_name"));
            etEmail.setText(intent.getStringExtra("user_email"));
            etPhone.setText(intent.getStringExtra("user_phone"));
            etAddress.setText(intent.getStringExtra("user_address"));
        }
    }

    private void setupFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
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

        userRef.setValue(updatedUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}