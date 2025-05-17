package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class PaymentSuccessActivity extends BaseActivity {

    private TextView tvOrderId;
    private Button btnViewOrder, btnGoHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupBottomNavigation(R.layout.activity_payment_success);

        tvOrderId = findViewById(R.id.tv_order_id);
        btnViewOrder = findViewById(R.id.btn_view_order);
        btnGoHome = findViewById(R.id.btn_go_home);

        TextView tvPaymentAmount = findViewById(R.id.tv_payment_amount);

        String orderId = getIntent().getStringExtra("order_id");
        double amount = getIntent().getDoubleExtra("amount", 0); // Nhận đúng kiểu

        DecimalFormat formatter = new DecimalFormat("#,###");
        tvOrderId.setText("Mã đơn hàng: " + orderId);
        tvPaymentAmount.setText("Số tiền: " + formatter.format(amount) + " đồng");



        btnViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListOrderActivity.class);
            startActivity(intent);
            finish();
        });

        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
