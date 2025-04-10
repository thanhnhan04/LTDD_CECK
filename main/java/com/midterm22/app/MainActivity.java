package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends BaseActivity {

    private TextView tv_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_main);
        ViewFlipper viewFlipper = findViewById(R.id.viewFlipper);
        viewFlipper.startFlipping();

        tv_name = findViewById(R.id.tv_name);
        String user_name = getIntent().getStringExtra("user_name");
        if (user_name != null && !user_name.isEmpty()) {
            tv_name.setText(user_name);
        }

    }
}