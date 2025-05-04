package com.midterm22.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;

public class MoreActivity extends BaseActivity {
    private TextView tv_name;
    private LinearLayout logoutButton,deal_id, lout_list_order, edit_account, lout_support,inf_policy;
    private SharedPreferences sharedPref;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_more);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        initViews();

        // Load user data
        loadUserData();

        // Setup click listeners
        setupClickListeners();
    }

    private void initViews() {
        tv_name = findViewById(R.id.tv_name);
        logoutButton = findViewById(R.id.lout_log_out);
        lout_list_order = findViewById(R.id.lout_list_order);
        lout_support = findViewById(R.id.lout_support);
        edit_account = findViewById(R.id.edit_account);
        inf_policy = findViewById(R.id.inf_policy);
        deal_id = findViewById(R.id.deal_id);
        sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        String userName = sharedPref.getString("user_name", "");
        tv_name.setText(userName);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> logout());

        lout_list_order.setOnClickListener(v -> navigateToListOrderActivity());
        lout_support.setOnClickListener(v -> navigateToSupportActivity());
        edit_account.setOnClickListener(v -> navigateToEditProfile());
        inf_policy.setOnClickListener(v-> navigateToPlicyActivity());
        deal_id.setOnClickListener(v-> navigateToDealActivity());
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    private void navigateToListOrderActivity() {
        Intent intent = new Intent(this, ListOrderActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToSupportActivity() {
        Intent intent = new Intent(this, SupportActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    private void navigateToPlicyActivity() {
        Intent intent = new Intent(this, PolicyActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    private void navigateToDealActivity() {
        Intent intent = new Intent(this, DealActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    // Firebase logout
                    mAuth.signOut();

                    // Clear shared preferences
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.clear();
                    editor.apply();

                    redirectToLogin();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh user data when returning from other activities
        loadUserData();
    }
}