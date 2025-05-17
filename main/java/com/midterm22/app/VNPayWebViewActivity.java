package com.midterm22.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class VNPayWebViewActivity extends AppCompatActivity {

    public static final String EXTRA_PAYMENT_URL = "payment_url";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tạo WebView bằng mã Java thay vì XML
        WebView webView = new WebView(this);
        setContentView(webView);

        // Lấy URL thanh toán từ Intent
        Intent intent = getIntent();
        String paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL);

        if (paymentUrl == null) {
            Toast.makeText(this, "Không tìm thấy URL thanh toán", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String returnUrl = Config.vnp_ReturnUrl; // Lấy từ class Config bạn đã có

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String currentUrl = request.getUrl().toString();

                if (currentUrl.contains("vnpay_return.jsp")) {
                    Uri uri = Uri.parse(currentUrl);
                    String responseCode = uri.getQueryParameter("vnp_ResponseCode");
                    String amount = uri.getQueryParameter("vnp_Amount");

                    Intent resultIntent = new Intent();

                    if ("00".equals(responseCode)) {
                        Toast.makeText(VNPayWebViewActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                        resultIntent.putExtra("payment_result", "success");
                    } else {
                        Toast.makeText(VNPayWebViewActivity.this, "Thanh toán thất bại!", Toast.LENGTH_LONG).show();
                        resultIntent.putExtra("payment_result", "failed");
                    }

                    setResult(RESULT_OK, resultIntent);
                    finish();
                    return true;
                }

                return false;
            }

        });
        // Tải trang thanh toán
        webView.loadUrl(paymentUrl);
    }
}
