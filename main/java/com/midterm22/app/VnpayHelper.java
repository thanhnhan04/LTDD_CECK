package com.midterm22.app;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class VnpayHelper {

    public static String createPaymentUrl(String orderID, String amountStr, String bankCode, String locale) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount = Long.parseLong(amountStr) * 100;

        String vnp_TxnRef = orderID;
        String vnp_IpAddr = "127.0.0.1"; // Hoặc lấy IP động nếu cần
        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", (locale != null && !locale.isEmpty()) ? locale : "vn");
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Định dạng ngày giờ đúng GMT+7
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        TimeZone tzVN = TimeZone.getTimeZone("Asia/Ho_Chi_Minh"); // ✅ Chính xác hơn "Etc/GMT+7"
        formatter.setTimeZone(tzVN);

// vnp_CreateDate
        Calendar calCreate = Calendar.getInstance(tzVN);
        String vnp_CreateDate = formatter.format(calCreate.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

// vnp_ExpireDate = create + 15 phút
        Calendar calExpire = (Calendar) calCreate.clone();
        calExpire.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calExpire.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

// In log kiểm tra
        System.out.println("✅ vnp_CreateDate: " + vnp_CreateDate);
        System.out.println("✅ vnp_ExpireDate: " + vnp_ExpireDate);


        // Build URL
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        try {
            for (int i = 0; i < fieldNames.size(); i++) {
                String fieldName = fieldNames.get(i);
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, "US-ASCII"));
                    query.append(URLEncoder.encode(fieldName, "US-ASCII"))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, "US-ASCII"));
                    if (i < fieldNames.size() - 1) {
                        hashData.append('&');
                        query.append('&');
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnp_SecureHash);
        return Config.vnp_PayUrl + "?" + query;
    }
}
