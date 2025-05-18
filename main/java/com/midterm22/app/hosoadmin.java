package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class hosoadmin extends AppCompatActivity {

    private TextView tvAdminName, tvAdminRole, tvFullName, tvEmail, tvPhone, tvAddress, tvCreatedAt;
    private Button btnLogout;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosoadmin);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // If user is not logged in, redirect to login screen
            Toast.makeText(this, "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(hosoadmin.this, LoginActivity.class); // Replace with your login activity
            startActivity(intent);
            finish();
            return;
        }

        // Reference to the logged-in user's data in Firebase
        String userId = currentUser.getUid();
        userRef = FirebaseDatabase.getInstance().getReference("User").child(userId);

        // Initialize views
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminRole = findViewById(R.id.tvAdminRole);
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvCreatedAt = findViewById(R.id.tvCreatedAt);
        btnLogout = findViewById(R.id.btnLogout);

        // Fetch user data from Firebase
        fetchUserData();

        // Set up logout button
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void fetchUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve user data
                    String fullName = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String phone = snapshot.child("phone").getValue(String.class);
                    String address = snapshot.child("address").getValue(String.class);
                    String createdAt = snapshot.child("createdAt").getValue(String.class);
                    String role = snapshot.child("role").getValue(String.class);

                    // Populate the TextViews
                    tvAdminName.setText(fullName != null ? fullName : "N/A");
                    tvAdminRole.setText(role != null ? role : "Admin");
                    tvFullName.setText(fullName != null ? fullName : "N/A");
                    tvEmail.setText(email != null ? email : "N/A");
                    tvPhone.setText(phone != null ? phone : "N/A");
                    tvAddress.setText(address != null ? address : "N/A");
                    tvCreatedAt.setText(createdAt != null ? createdAt : "N/A");
                } else {
                    Toast.makeText(hosoadmin.this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(hosoadmin.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(hosoadmin.this, LoginActivity.class); // Replace with your login activity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
