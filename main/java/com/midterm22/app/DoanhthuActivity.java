package com.midterm22.app;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

public class DoanhthuActivity extends AppCompatActivity {

    private TextView totalOrders, totalRevenue, avgOrderValue;
    private BarChart barChart;
    private HorizontalBarChart horizontalBarChart;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doanhthu);

        // Ánh xạ các thành phần giao diện
        totalOrders       = findViewById(R.id.total_orders);
        totalRevenue      = findViewById(R.id.total_revenue);
        avgOrderValue     = findViewById(R.id.avg_order_value);
        barChart          = findViewById(R.id.bar_chart);
        horizontalBarChart= findViewById(R.id.horizontal_bar_chart);

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ordersRef = databaseReference.child("orders");

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int totalOrdersCount    = 0;
                double totalRevenueValue= 0.0;

                // Nếu không có dữ liệu
                if (!dataSnapshot.exists()) {
                    Log.d("Firebase", "Không tìm thấy node 'orders'");
                }

                // Duyệt qua từng đơn hàng
                for (DataSnapshot orderSnap : dataSnapshot.getChildren()) {
                    // Lấy trường 'total' dưới dạng Double để hỗ trợ giá trị là số thực
                    Double orderPrice = orderSnap.child("total").getValue(Double.class);

                    if (orderPrice == null) {
                        orderPrice = 0.0;  // Gán 0 nếu giá trị null
                    }

                    Log.d("Firebase", "OrderID=" + orderSnap.getKey() + "  total=" + orderPrice);

                    if (orderPrice > 0) {
                        totalOrdersCount++;
                        totalRevenueValue += orderPrice;
                    }
                }

                // Tính trung bình
                double avgValue = (totalOrdersCount > 0)
                        ? totalRevenueValue / totalOrdersCount
                        : 0.0;

                // In log kết quả
                Log.d("Firebase", "Total Orders: " + totalOrdersCount);
                Log.d("Firebase", "Total Revenue: " + totalRevenueValue);
                Log.d("Firebase", "Avg Order Value: " + avgValue);

                // Định dạng số với dấu phân cách
                String formattedTotalRevenue = formatNumber(totalRevenueValue);
                String formattedAvgValue = formatNumber(avgValue);

                // Cập nhật UI
                totalOrders.setText(String.valueOf(totalOrdersCount));
                totalRevenue.setText(formattedTotalRevenue + " đ");
                avgOrderValue.setText(formattedAvgValue + " đ");

                // Cập nhật biểu đồ doanh thu
                updateBarChart(totalRevenueValue);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("FirebaseError", "Failed to read 'orders'.", error.toException());
            }
        });
    }

    // Hàm cập nhật biểu đồ doanh thu
    private void updateBarChart(double totalRevenueValue) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f, (float) totalRevenueValue));

        // Tạo một dữ liệu BarDataSet
        BarDataSet barDataSet = new BarDataSet(barEntries, "Doanh thu");
        barDataSet.setColor(Color.parseColor("#FF5722"));
        barDataSet.setValueTextColor(Color.WHITE);  // Màu chữ hiển thị giá trị

        // Tạo BarData và gán cho biểu đồ
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Cập nhật biểu đồ
        barChart.invalidate();  // Làm mới biểu đồ
    }

    // Hàm định dạng số với dấu phân cách hàng nghìn
    private String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###"); // Định dạng số có dấu phân cách
        return decimalFormat.format(value);
    }
}
