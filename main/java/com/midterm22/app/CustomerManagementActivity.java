package com.midterm22.app;
import androidx.appcompat.widget.SearchView;
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
        SearchView searchView = findViewById(R.id.searchView);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);

        rvActiveCustomers = findViewById(R.id.rvActiveCustomers);
        rvLockedCustomers = findViewById(R.id.rvLockedCustomers);

        rvActiveCustomers.setLayoutManager(new LinearLayoutManager(this));
        rvLockedCustomers.setLayoutManager(new LinearLayoutManager(this));

        activeAdapter = new CustomerAdapter(this);
        lockedAdapter = new CustomerAdapter(this);

        rvActiveCustomers.setAdapter(activeAdapter);
        rvLockedCustomers.setAdapter(lockedAdapter);

        activeCustomerList = new ArrayList<>();
        inactiveCustomerList = new ArrayList<>();

        tabLayout = findViewById(R.id.tabLayout);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // không xử lý khi nhấn tìm
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                int selectedTab = tabLayout.getSelectedTabPosition();
                if (selectedTab == 0) {
                    activeAdapter.filter(newText);
                } else {
                    lockedAdapter.filter(newText);
                }
                return true;
            }
        });

        tabLayout.addTab(tabLayout.newTab().setText("Tài khoản hiện tại"));
        tabLayout.addTab(tabLayout.newTab().setText("Tài khoản bị khóa"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvActiveCustomers.setVisibility(View.VISIBLE);
                    rvLockedCustomers.setVisibility(View.GONE);
                    activeAdapter.setCustomers(activeCustomerList, true);
                } else {
                    rvActiveCustomers.setVisibility(View.GONE);
                    rvLockedCustomers.setVisibility(View.VISIBLE);
                    lockedAdapter.setCustomers(inactiveCustomerList, false);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

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
            drawerLayout.closeDrawer(navigationView);
            return true;
        });

        usersRef = FirebaseDatabase.getInstance().getReference("User");
        loadCustomers();

        activeAdapter.setOnItemClickListener(user -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("userId", user.getId());
            startActivity(intent);
        });
        lockedAdapter.setOnItemClickListener(user -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("userId", user.getId());
            startActivity(intent);
        });
    }

    private void loadCustomers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                activeCustomerList.clear();
                inactiveCustomerList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null) {
                        user.setId(data.getKey());
                        Log.d("CustomerRole", "User role: " + user.getRole());

                        if ("customer".equals(user.getRole())) {
                            activeCustomerList.add(user);
                        } else if ("locked".equals(user.getRole())) {
                            inactiveCustomerList.add(user);
                        }
                    }
                }
                Log.d("CustomerData", "Active users: " + activeCustomerList.size());
                Log.d("CustomerData", "Locked users: " + inactiveCustomerList.size());

                activeAdapter.setCustomers(activeCustomerList, true);
                lockedAdapter.setCustomers(inactiveCustomerList, false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerManagementActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
