package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class hosoadmin extends AppCompatActivity {
    private DatabaseReference userRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosoadmin);

        // Ánh xạ view
        ImageView ivAvatar = findViewById(R.id.ivAvatar);
        TextView tvAdminName = findViewById(R.id.tvAdminName);
        TextView tvAdminRole = findViewById(R.id.tvAdminRole);
        TextView tvAdminTitle = findViewById(R.id.tvAdminTitle);
        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvEmail = findViewById(R.id.tvEmail);
        TextView tvPhone = findViewById(R.id.tvPhone);
        TextView tvJoinDate = findViewById(R.id.tvJoinDate);
        Switch switchPush = findViewById(R.id.switchPush);
        Switch switchEmail = findViewById(R.id.switchEmail);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Lấy ID user hiện tại
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            // Load ảnh đại diện từ Firebase Auth (nếu có)
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.ic_person)
                        .into(ivAvatar);
            }
        } else {
            // Chưa đăng nhập, quay về màn hình đăng nhập
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Khởi tạo Firebase reference
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

        // Load dữ liệu user
        loadUserData();

        // Xử lý switch thông báo
        switchPush.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userRef.child("pushNotification").setValue(isChecked)
                    .addOnFailureListener(e -> {
                        Toast.makeText(hosoadmin.this,
                                "Cập nhật thông báo đẩy thất bại", Toast.LENGTH_SHORT).show();
                        switchPush.setChecked(!isChecked); // Rollback trạng thái
                    });
        });

        switchEmail.setOnCheckedChangeListener((buttonView, isChecked) -> {
            userRef.child("emailNotification").setValue(isChecked)
                    .addOnFailureListener(e -> {
                        Toast.makeText(hosoadmin.this,
                                "Cập nhật thông báo email thất bại", Toast.LENGTH_SHORT).show();
                        switchEmail.setChecked(!isChecked); // Rollback trạng thái
                    });
        });

        // Xử lý nút đăng xuất
        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu từ Firebase
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);
                    String joinDate = snapshot.child("joinDate").getValue(String.class);
                    String title = snapshot.child("title").getValue(String.class);
                    Boolean pushNotification = snapshot.child("pushNotification").getValue(Boolean.class);
                    Boolean emailNotification = snapshot.child("emailNotification").getValue(Boolean.class);

                    // Điền dữ liệu vào giao diện
                    TextView tvAdminName = findViewById(R.id.tvAdminName);
                    TextView tvAdminRole = findViewById(R.id.tvAdminRole);
                    TextView tvAdminTitle = findViewById(R.id.tvAdminTitle);
                    TextView tvFullName = findViewById(R.id.tvFullName);
                    TextView tvEmail = findViewById(R.id.tvEmail);
                    TextView tvPhone = findViewById(R.id.tvPhone);
                    TextView tvJoinDate = findViewById(R.id.tvJoinDate);
                    Switch switchPush = findViewById(R.id.switchPush);
                    Switch switchEmail = findViewById(R.id.switchEmail);

                    if (name != null) {
                        tvAdminName.setText(name);
                        tvFullName.setText(name);
                    }
                    if (role != null) tvAdminRole.setText(role);
                    if (title != null) tvAdminTitle.setText(title);
                    if (email != null) tvEmail.setText(email);
                    if (phone != null) tvPhone.setText(phone);
                    if (joinDate != null) tvJoinDate.setText(joinDate);

                    if (pushNotification != null) {
                        switchPush.setChecked(pushNotification);
                    }
                    if (emailNotification != null) {
                        switchEmail.setChecked(emailNotification);
                    }
                } else {
                    Toast.makeText(hosoadmin.this,
                            "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(hosoadmin.this,
                        "Lỗi khi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        // Hủy bỏ các listener khi activity bị hủy
        if (userRef != null) {
            userRef.removeEventListener((ValueEventListener) this);
        }
        super.onDestroy();
    }
}