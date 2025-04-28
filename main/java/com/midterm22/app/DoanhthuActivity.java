package com.midterm22.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DoanhthuActivity extends AppCompatActivity {
    private ImageButton btnSearch;
    private ImageView btnProfile;
    private DrawerLayout drawerLayout;
    private TextView monthlyOrders, monthlyRevenue, dailyOrders, dailyRevenue;
    private BarChart barChart;
    private Button btnToggleChart;
    private LinearLayout notificationsContainer;
    private View bar10h, bar15h, bar16h, bar18h;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doanhthu);
        drawerLayout = findViewById(R.id.drawer_layout);
        monthlyOrders = findViewById(R.id.monthly_orders);
        monthlyRevenue = findViewById(R.id.monthly_revenue);
        dailyOrders = findViewById(R.id.daily_orders);
        dailyRevenue = findViewById(R.id.daily_revenue);
        barChart = findViewById(R.id.bar_chart);
        btnToggleChart = findViewById(R.id.btnToggleChart);
        notificationsContainer = findViewById(R.id.notificationsContainer);
        bar10h = findViewById(R.id.bar_10h);
        bar15h = findViewById(R.id.bar_15h);
        bar16h = findViewById(R.id.bar_16h);
        bar18h = findViewById(R.id.bar_18h);

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ordersRef = databaseReference.child("orders");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.navigationView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, AdminActivity.class));
            } else if (id == R.id.nav_qldh) {
                startActivity(new Intent(this, ManagerOrderActivity.class));
            } else if (id == R.id.nav_qlsp) {
                startActivity(new Intent(this, FoodManagementActivity.class));
            } else if (id == R.id.nav_qlkh) {
                startActivity(new Intent(this, CustomerManagementActivity.class));
            } else if (id == R.id.nav_qldt) {
                startActivity(new Intent(this, DoanhthuActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });
        btnSearch = findViewById(R.id.btnSearch);
        btnProfile = findViewById(R.id.btnProfile);
        btnSearch.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng tìm kiếm đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this,hosoadmin.class); // Thay ProfileActivity bằng activity của bạn
            startActivity(intent);
        });

        btnToggleChart.setOnClickListener(v -> {
            if (barChart.getVisibility() == View.GONE) {
                barChart.setVisibility(View.VISIBLE);
                btnToggleChart.setText("Ẩn biểu đồ");
            } else {
                barChart.setVisibility(View.GONE);
                btnToggleChart.setText("Xem biểu đồ");
            }
        });

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int monthlyOrdersCount = 0;
                double monthlyRevenueValue = 0.0;
                int dailyOrdersCount = 0;
                double dailyRevenueValue = 0.0;
                int[] peakHourCounts = new int[4]; // 10h, 15h, 16h, 18h
                ArrayList<Order> ordersList = new ArrayList<>();

                // Lấy thời gian hiện tại
                Calendar calendar = Calendar.getInstance();
                long currentTimeMillis = calendar.getTimeInMillis();
                int currentYear = calendar.get(Calendar.YEAR);
                int currentMonth = calendar.get(Calendar.MONTH); // 0-based (0 = January)
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());

                if (!dataSnapshot.exists()) {
                    Log.d("Firebase", "Không tìm thấy node 'orders'");
                    updateUI(monthlyOrdersCount, monthlyRevenueValue, dailyOrdersCount, dailyRevenueValue, peakHourCounts, ordersList);
                    return;
                }

                for (DataSnapshot orderSnap : dataSnapshot.getChildren()) {
                    Double orderPrice = orderSnap.child("total").getValue(Double.class);
                    String createdAt = orderSnap.child("createdAt").getValue(String.class);
                    String status = orderSnap.child("status").getValue(String.class);
                    String orderId = orderSnap.getKey();

                    if (orderPrice == null) {
                        orderPrice = 0.0;
                    }
                    if (createdAt == null) {
                        Log.w("Firebase", "Order " + orderId + " missing createdAt");
                        continue;
                    }
                    if (status == null) {
                        status = "Không xác định";
                    }

                    try {
                        long timestamp = Long.parseLong(createdAt);
                        Date orderDate = new Date(timestamp);
                        Calendar orderCalendar = Calendar.getInstance();
                        orderCalendar.setTime(orderDate);
                        int orderYear = orderCalendar.get(Calendar.YEAR);
                        int orderMonth = orderCalendar.get(Calendar.MONTH);
                        int orderHour = orderCalendar.get(Calendar.HOUR_OF_DAY);
                        String orderDateStr = dateFormat.format(orderDate);

                        Order order = new Order(orderId, timestamp, orderPrice, status);
                        ordersList.add(order);

                        // Doanh thu tháng
                        if (orderYear == currentYear && orderMonth == currentMonth && orderPrice > 0) {
                            monthlyOrdersCount++;
                            monthlyRevenueValue += orderPrice;
                        }

                        // Doanh thu ngày
                        if (orderDateStr.equals(currentDate) && orderPrice > 0) {
                            dailyOrdersCount++;
                            dailyRevenueValue += orderPrice;
                        }

                        // Giờ cao điểm (±1 giờ)
                        if (orderHour >= 9 && orderHour < 11) {
                            peakHourCounts[0]++; // 10h
                        } else if (orderHour >= 14 && orderHour < 16) {
                            peakHourCounts[1]++; // 15h
                        } else if (orderHour >= 15 && orderHour < 17) {
                            peakHourCounts[2]++; // 16h
                        } else if (orderHour >= 17 && orderHour < 19) {
                            peakHourCounts[3]++; // 18h
                        }
                    } catch (NumberFormatException e) {
                        Log.w("Firebase", "Invalid timestamp format for order: " + orderId);
                    }
                }

                updateUI(monthlyOrdersCount, monthlyRevenueValue, dailyOrdersCount, dailyRevenueValue, peakHourCounts, ordersList);
                updateBarChart(monthlyRevenueValue);
                updatePeakHours(peakHourCounts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("FirebaseError", "Failed to read 'orders': " + error.getMessage());
            }
        });
    }

    private static class Order {
        String id;
        long timestamp;
        double total;
        String status;

        Order(String id, long timestamp, double total, String status) {
            this.id = id;
            this.timestamp = timestamp;
            this.total = total;
            this.status = status;
        }
    }

    private void updateUI(int monthlyOrdersCount, double monthlyRevenueValue, int dailyOrdersCount, double dailyRevenueValue, int[] peakHourCounts, ArrayList<Order> ordersList) {
        String formattedMonthlyRevenue = formatNumber(monthlyRevenueValue);
        String formattedDailyRevenue = formatNumber(dailyRevenueValue);

        monthlyOrders.setText(String.valueOf(monthlyOrdersCount));
        monthlyRevenue.setText(formattedMonthlyRevenue + " đ");
        dailyOrders.setText(String.valueOf(dailyOrdersCount));
        dailyRevenue.setText(formattedDailyRevenue + " đ");

        updateNotifications(ordersList);
    }

    private void updateBarChart(double totalRevenueValue) {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(0f, (float) totalRevenueValue));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Doanh thu tháng");
        barDataSet.setColor(Color.parseColor("#FF5722"));
        barDataSet.setValueTextColor(Color.WHITE);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    private void updatePeakHours(int[] peakHourCounts) {
        int maxCount = 0;
        for (int count : peakHourCounts) {
            if (count > maxCount) {
                maxCount = count;
            }
        }
        if (maxCount > 0) {
            float ratio10h = (float) peakHourCounts[0] / maxCount;
            float ratio15h = (float) peakHourCounts[1] / maxCount;
            float ratio16h = (float) peakHourCounts[2] / maxCount;
            float ratio18h = (float) peakHourCounts[3] / maxCount;

            setBarWidth(bar10h, ratio10h);
            setBarWidth(bar15h, ratio15h);
            setBarWidth(bar16h, ratio16h);
            setBarWidth(bar18h, ratio18h);
        }
    }

    private void setBarWidth(View bar, float ratio) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
        params.weight = ratio;
        bar.setLayoutParams(params);
    }

    private void updateNotifications(ArrayList<Order> ordersList) {
        notificationsContainer.removeAllViews();

        if (ordersList.isEmpty()) {
            TextView noDataView = new TextView(this);
            noDataView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            noDataView.setText("Chưa có đơn hàng");
            noDataView.setTextSize(14);
            noDataView.setTextColor(Color.parseColor("#666666"));
            noDataView.setPadding(8, 8, 8, 8);
            notificationsContainer.addView(noDataView);
            return;
        }

        ordersList.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));

        int maxOrders = Math.min(ordersList.size(), 10);

        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        for (int i = 0; i < maxOrders; i++) {
            Order order = ordersList.get(i);

            LinearLayout orderLayout = new LinearLayout(this);
            orderLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            orderLayout.setOrientation(LinearLayout.HORIZONTAL);
            orderLayout.setPadding(8, 8, 8, 8);

//            // ID đơn hàng
//            TextView idView = new TextView(this);
//            idView.setLayoutParams(new LinearLayout.LayoutParams(
//                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
//            idView.setText(order.id);
//            idView.setTextSize(14);
//            idView.setTextColor(Color.parseColor("#333333"));

            // Thời gian
            TextView timeView = new TextView(this);
            timeView.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            timeView.setText(timeFormat.format(new Date(order.timestamp)));
            timeView.setTextSize(14);
            timeView.setTextColor(Color.parseColor("#333333"));

            // Tổng tiền
            TextView totalView = new TextView(this);
            totalView.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            totalView.setText(formatNumber(order.total) + " đ");
            totalView.setTextSize(14);
            totalView.setTextColor(Color.parseColor("#333333"));
            totalView.setPadding(8, 0, 8, 0);

            // Trạng thái
            LinearLayout statusLayout = new LinearLayout(this);
            statusLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            statusLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView statusView = new TextView(this);
            statusView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            statusView.setText(order.status);
            statusView.setTextSize(14);
            statusView.setTextColor(Color.parseColor("#333333"));
            statusView.setPadding(8, 0, 0, 0);

            // Kiểm tra "Mới" (trong vòng 1 giờ)
            long timeDiff = (currentTimeMillis - order.timestamp) / 1000; // Chênh lệch giây
            if (timeDiff <= 3600) { // 1 giờ = 3600 giây
                TextView newLabel = new TextView(this);
                newLabel.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                newLabel.setText("Mới");
                newLabel.setTextSize(14);
                newLabel.setTextColor(Color.parseColor("#FF5722"));
                newLabel.setPadding(8, 0, 8, 0);
                statusLayout.addView(newLabel);
            }

            statusLayout.addView(statusView);

//            orderLayout.addView(idView);
            orderLayout.addView(timeView);
            orderLayout.addView(totalView);
            orderLayout.addView(statusLayout);

            notificationsContainer.addView(orderLayout);
        }
    }

    private String formatNumber(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(value);
    }
}