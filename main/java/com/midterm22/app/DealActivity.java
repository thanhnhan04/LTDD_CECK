package com.midterm22.app;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class DealActivity extends BaseActivity {

    private RecyclerView recyclerDeals;
    private DealAdapter dealAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_deal);



        recyclerDeals = findViewById(R.id.recyclerDeals);
        recyclerDeals.setLayoutManager(new LinearLayoutManager(this));

        List<String> sampleDeals = Arrays.asList(
                "Giảm 20% cho đơn hàng từ 100.000đ.",
                "Mua 1 tặng 1 với mọi loại burger trong khung giờ 14h-16h.",
                "Freeship cho đơn hàng từ 50.000đ trong ngày Chủ Nhật.",
                "Giảm 30% khi đặt combo gà rán và nước ngọt.",
                "Tặng phiếu giảm giá 15.000đ khi đăng nhập lần đầu.",
                "Ưu đãi thành viên: Giảm 25% cho mọi đơn hàng tháng này.",
                "Mua pizza lớn được tặng pizza nhỏ miễn phí.",
                "Đặt hàng sau 21h giảm thêm 10%.",
                "Ưu đãi nhóm: Giảm 50.000đ cho đơn từ 4 người trở lên.",
                "Giảm giá combo học sinh - sinh viên từ 11h đến 13h."
        );

        dealAdapter = new DealAdapter(sampleDeals);
        recyclerDeals.setAdapter(dealAdapter);
    }
}
