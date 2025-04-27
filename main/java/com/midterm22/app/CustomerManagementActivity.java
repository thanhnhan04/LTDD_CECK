package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerManagementActivity extends AppCompatActivity {

    private RecyclerView rvActiveCustomers, rvLockedCustomers;
    private CustomerAdapter activeAdapter, lockedAdapter;
    private List<User> activeCustomerList, inactiveCustomerList;
    private DatabaseReference usersRef;
    private DrawerLayout drawerLayout;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_management);

        // Khởi tạo DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        // Khởi tạo RecyclerViews
        rvActiveCustomers = findViewById(R.id.rvActiveCustomers);
        rvLockedCustomers = findViewById(R.id.rvLockedCustomers);

        rvActiveCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvLockedCustomers.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Adapter
        activeAdapter = new CustomerAdapter(this);
        lockedAdapter = new CustomerAdapter(this);

        rvActiveCustomers.setAdapter(activeAdapter);
        rvLockedCustomers.setAdapter(lockedAdapter);

        // Khởi tạo danh sách
        activeCustomerList = new ArrayList<>();
        inactiveCustomerList = new ArrayList<>();

        // Khởi tạo TabLayout
        tabLayout = findViewById(R.id.tabLayout);

        // Thêm 2 tab
        tabLayout.addTab(tabLayout.newTab().setText("Tài khoản hiện tại"));
        tabLayout.addTab(tabLayout.newTab().setText("Tài khoản bị khóa"));

        // Xử lý sự kiện chọn tab
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) { // Tab "Tài khoản hiện tại"
                    rvActiveCustomers.setVisibility(View.VISIBLE);
                    rvLockedCustomers.setVisibility(View.GONE);
                } else { // Tab "Tài khoản bị khóa"
                    rvActiveCustomers.setVisibility(View.GONE);
                    rvLockedCustomers.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(""); // Ẩn tiêu đề mặc định

        // Xử lý hamburger menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Xử lý chọn item menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // Đã ở trang chủ
            } else if (id == R.id.nav_qldh) {
                startActivity(new Intent(this, ManagerOrderActivity.class));
            } else if (id == R.id.nav_qlsp) {
                startActivity(new Intent(this, FoodManagementActivity.class));
            } else if (id == R.id.nav_qlkh) {
                // Đã ở CustomerManagementActivity
            } else if (id == R.id.nav_qldt) {
                startActivity(new Intent(this, DoanhthuActivity.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        // Firebase Database
        usersRef = FirebaseDatabase.getInstance().getReference("User");
        loadCustomers();

        // Sự kiện click vào item RecyclerView
        activeAdapter.setOnItemClickListener(user -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("userId", user.getId());
            startActivity(intent);
        });
    }

    private void loadCustomers() {
        usersRef.orderByChild("role").equalTo("customer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeCustomerList.clear();
                inactiveCustomerList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null) {
                        user.setId(data.getKey());
                        Log.d("CustomerRole", "User role: " + user.getRole());  // Kiểm tra role
                        if (user.getRole().equals("locked")) {
                            inactiveCustomerList.add(user);  // Người bị khóa
                        } else {
                            activeCustomerList.add(user);  // Người đang hoạt động
                        }
                    }
                }
                Log.d("CustomerData", "Active users: " + activeCustomerList.size());
                Log.d("CustomerData", "Locked users: " + inactiveCustomerList.size());

                // Cập nhật adapter cho các RecyclerView
                activeAdapter.setActiveCustomers(activeCustomerList);
                activeAdapter.notifyDataSetChanged();  // Cập nhật dữ liệu cho active customers

                lockedAdapter.setInactiveCustomers(inactiveCustomerList);
                lockedAdapter.notifyDataSetChanged();  // Cập nhật dữ liệu cho locked customers
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerManagementActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
