package com.midterm22.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class MoreActivity extends BaseActivity {
    private TextView tv_name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_more);

        tv_name = findViewById(R.id.tv_name);
//        String user_name = getIntent().getStringExtra("user_name");
//        if (user_name != null && !user_name.isEmpty()) {
//            tv_name.setText(user_name);
//        }
        SharedPreferences sharedPref = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userName = sharedPref.getString("user_name", "");
        tv_name.setText(userName);
    }
}