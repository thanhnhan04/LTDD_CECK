package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavigation(int layoutResId) {
        // Set layout cha chứa navigation bar
        super.setContentView(R.layout.base_activity);

        // Gắn layout riêng vào content_frame
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        LayoutInflater.from(this).inflate(layoutResId, contentFrame, true);

        // Gán sự kiện các nút điều hướng
        LinearLayout home = findViewById(R.id.home_id);
        LinearLayout menu = findViewById(R.id.menu_id);
        LinearLayout deal = findViewById(R.id.deal_id);
        LinearLayout more = findViewById(R.id.more_id);

        home.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        menu.setOnClickListener(v -> {
            startActivity(new Intent(this, MenuActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Mở nếu có các activity này
//        deal.setOnClickListener(v -> {
//            startActivity(new Intent(this, DealActivity.class));
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        });
//
//        more.setOnClickListener(v -> {
//            startActivity(new Intent(this, MoreActivity.class));
//            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
//        });
    }
}
