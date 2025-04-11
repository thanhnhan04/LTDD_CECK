package com.midterm22.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MoreActivity extends BaseActivity {
    private TextView tv_name;
    private LinearLayout logoutButton, lout_list_order, edit_account;
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
        edit_account = findViewById(R.id.edit_account);
        sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
    }

    private void loadUserData() {
        String userName = sharedPref.getString("user_name", "");
        tv_name.setText(userName);
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> logout());

        lout_list_order.setOnClickListener(v -> navigateToListOrderActivity());

        edit_account.setOnClickListener(v -> navigateToEditProfile());
    }

    private void navigateToEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);

        // Pass current user data to EditProfileActivity
        intent.putExtra("user_name", sharedPref.getString("user_name", ""));
        intent.putExtra("user_email", sharedPref.getString("user_email", ""));
        intent.putExtra("user_phone", sharedPref.getString("user_phone", ""));
        intent.putExtra("user_address", sharedPref.getString("user_address", ""));

        startActivity(intent);

        // Add transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void navigateToListOrderActivity() {
        Intent intent = new Intent(this, ListOrderActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void logout() {
        // Firebase logout
        mAuth.signOut();

        // Clear shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        redirectToLogin();
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