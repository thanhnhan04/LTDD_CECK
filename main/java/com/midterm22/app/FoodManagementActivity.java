package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.AddEditFoodActivity;
import com.midterm22.app.CustomerManagementActivity;
import com.midterm22.app.DoanhthuActivity;
import com.midterm22.app.LoginActivity;
import com.midterm22.app.ManagerOrderActivity;
import com.midterm22.app.ProductAdapter;
import com.midterm22.app.R;
import com.midterm22.app.model.Product;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

public class FoodManagementActivity extends AppCompatActivity {

    private RecyclerView rvCurrentFoods, rvHiddenFoods;
    private FloatingActionButton btnAddFood;
    private ProductAdapter currentAdapter, hiddenAdapter;
    private List<Product> currentFoodList, hiddenFoodList;
    private DatabaseReference productRef;
    private TabLayout tabLayout;
    private SearchView searchView;
    private TextView toolbarTitle;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_management);

        // Khởi tạo adapter và RecyclerView
        List<Product> foodList = new ArrayList<>();
        currentAdapter = new ProductAdapter(this, foodList);
        hiddenAdapter = new ProductAdapter(this, foodList);

        // Khởi tạo view
        rvCurrentFoods = findViewById(R.id.rvCurrentFoods);
        rvHiddenFoods = findViewById(R.id.rvHiddenFoods);
        btnAddFood = findViewById(R.id.btnAddFood);
        tabLayout = findViewById(R.id.tabLayout);
        searchView = findViewById(R.id.searchView);
        toolbarTitle = findViewById(R.id.toolbar_title);
        drawerLayout = findViewById(R.id.drawerLayout);

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // DrawerLayout setup
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // NavigationView setup
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Handling menu item clicks
            if (id == R.id.nav_home) {
                // Already on the homepage, do nothing
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
                finish();  // Close current activity to prevent going back
            }

            drawerLayout.closeDrawers(); // Close the drawer after an item is selected
            return true;
        });

        // Tab setup
        tabLayout.addTab(tabLayout.newTab().setText("Món ăn hiện tại"));
        tabLayout.addTab(tabLayout.newTab().setText("Món ăn đã ẩn"));

        // Set mặc định: hiển thị rvCurrentFoods
        rvCurrentFoods.setVisibility(View.VISIBLE);
        rvHiddenFoods.setVisibility(View.GONE);

        // Chọn tab đầu tiên
        TabLayout.Tab defaultTab = tabLayout.getTabAt(0);
        if (defaultTab != null) {
            defaultTab.select();
        }

        // Initialize food lists
        currentFoodList = new ArrayList<>();
        hiddenFoodList = new ArrayList<>();

        rvCurrentFoods.setLayoutManager(new LinearLayoutManager(this));
        rvHiddenFoods.setLayoutManager(new LinearLayoutManager(this));

        rvCurrentFoods.setAdapter(currentAdapter);
        rvHiddenFoods.setAdapter(hiddenAdapter);

        // Search functionality
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFoods(newText);
                toolbarTitle.setVisibility(View.VISIBLE);
                return true;
            }
        });

        // Firebase setup
        productRef = FirebaseDatabase.getInstance().getReference("products");
        loadFoods();

        // Add food button click
        btnAddFood.setOnClickListener(v -> {
            startActivity(new Intent(this, AddEditFoodActivity.class));
        });

        // Tab selection listener
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    rvCurrentFoods.setVisibility(View.VISIBLE);
                    rvHiddenFoods.setVisibility(View.GONE);
                } else {
                    rvCurrentFoods.setVisibility(View.GONE);
                    rvHiddenFoods.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Adapter actions setup
        setupAdapterActions();
    }

    private void loadFoods() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentFoodList.clear();
                hiddenFoodList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        product.setId(data.getKey());
                        if (product.isAvailable()) {
                            currentFoodList.add(product);
                        } else {
                            hiddenFoodList.add(product);
                        }
                    }
                }

                // Update the adapter's list
                currentAdapter.setProductList(currentFoodList);
                hiddenAdapter.setProductList(hiddenFoodList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FoodManagementActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterFoods(String query) {
        currentAdapter.filterList(query);
        hiddenAdapter.filterList(query);
    }

    private void setupAdapterActions() {
        ProductAdapter.OnFoodActionListener actionListener = new ProductAdapter.OnFoodActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(FoodManagementActivity.this, AddEditFoodActivity.class);
                intent.putExtra("productId", product.getId());
                startActivity(intent);
            }

            @Override
            public void onHide(Product product) {
                boolean newAvailable = !product.isAvailable();
                productRef.child(product.getId())
                        .child("available")
                        .setValue(newAvailable)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(FoodManagementActivity.this,
                                    newAvailable ? "Đã hiện lại món ăn" : "Đã ẩn món ăn",
                                    Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(FoodManagementActivity.this, "Lỗi, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                        });
            }
        };
        currentAdapter.setOnFoodActionListener(actionListener);
        hiddenAdapter.setOnFoodActionListener(actionListener);
    }
}
