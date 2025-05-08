package com.midterm22.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class VNPayFakeActivity extends AppCompatActivity {

    TextView tvTransferNote, tvAmount, tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpay_fake);

        // Nhận dữ liệu từ Intent
        String orderId = getIntent().getStringExtra("order_id");
        double totalAmount = getIntent().getDoubleExtra("total_amount", 0.0);

        // Ánh xạ view
        tvTransferNote = findViewById(R.id.tv_transfer_note);
        tvAmount = findViewById(R.id.tv_amount);
        tvStatus = findViewById(R.id.tv_status);

        // Cập nhật nội dung hiển thị theo orderId và amount
        tvTransferNote.setText("Nội dung: THANHTOAN_DONHANG_" + orderId);
        tvAmount.setText(String.format("Số tiền: %,.0f VND", totalAmount));
        tvStatus.setText("Đang xử lý thanh toán VNPay...");

        // Giả lập xử lý trong 5 giây
        new Handler().postDelayed(() -> {
            boolean isSuccess = Math.random() < 0.9;

            Intent resultIntent = new Intent();
            resultIntent.putExtra("payment_result", isSuccess ? "success" : "fail");
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }, 5000);
    }
}
