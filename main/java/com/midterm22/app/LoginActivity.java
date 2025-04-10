package com.midterm22.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.User;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private EditText edtEmail, edtPass;
    private Button btnLogin, btnRegister;
    private ProgressDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("User");

        edtEmail = findViewById(R.id.edt_email);
        edtPass = findViewById(R.id.edt_pass);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        loadingDialog = new ProgressDialog(this);
        loadingDialog.setMessage("Please wait...");
        loadingDialog.setCancelable(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String pass = edtPass.getText().toString().trim();

                if (email.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    loginUser(email, pass);
                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void loginUser(String email, String password) {
        loadingDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                fetchUserData(firebaseUser.getUid());
                            } else {
                                loadingDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserData(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loadingDialog.dismiss();
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getRole() != null && !user.getRole().isEmpty()) {
                        navigateToRole(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid user role", Toast.LENGTH_SHORT).show();
                        signOut();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "User not found in database", Toast.LENGTH_SHORT).show();
                    signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadingDialog.dismiss();
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                signOut();
            }
        });
    }

    private void navigateToRole(User user) {
        String role = user.getRole();
        String name = user.getName();

        // Lưu username vào SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("user_name", name);
        editor.apply();

        Intent intent;
        if ("customer".equals(role)) {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        } else if ("admin".equals(role)) {
            intent = new Intent(LoginActivity.this, AdminActivity.class);
        } else {
            Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
            //signOut();
            return;
        }

        startActivity(intent);
        finish();
    }
    public void signOut() {
        // Xóa username khỏi SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear(); // hoặc editor.remove("user_name");
        editor.apply();

        // Đăng xuất khỏi Firebase
        FirebaseAuth.getInstance().signOut();

        // Quay về LoginActivity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa hết ngăn xếp Activity
        startActivity(intent);
        finish();
    }

}
