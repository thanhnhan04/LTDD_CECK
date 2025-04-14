package com.midterm22.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.User;

public class CustomerDetailActivity extends AppCompatActivity {
    private TextView tvName, tvEmail, tvPhone, tvAddress;
    private Button btnToggleStatus;
    private String userId;
    private DatabaseReference userRef;
    private User currentUser;
    private boolean isLocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);

        userId = getIntent().getStringExtra("userId");
        userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);

        loadUserDetail();

        btnToggleStatus.setOnClickListener(view -> toggleUserStatus());
    }

    private void loadUserDetail() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    tvName.setText("Tên: " + currentUser.getName());
                    tvEmail.setText("Email: " + currentUser.getEmail());
                    tvPhone.setText("SĐT: " + currentUser.getPhone());
                    tvAddress.setText("Địa chỉ: " + currentUser.getAddress());
                    isLocked = "locked".equals(currentUser.getRole());
                    updateButtonText();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerDetailActivity.this, "Không tải được thông tin", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleUserStatus() {
        if (currentUser == null) return;
        String newRole = isLocked ? "customer" : "locked";
        userRef.child("role").setValue(newRole).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, isLocked ? "Mở tài khoản" : "Khóa tài khoản", Toast.LENGTH_SHORT).show();
                isLocked = !isLocked;
                updateButtonText();
            } else {
                Toast.makeText(this, "Thao tác thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateButtonText() {
        btnToggleStatus.setText(isLocked ? "Mở tài khoản" : "Khóa tài khoản");
    }
}
