package com.midterm22.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerManagementActivity extends AppCompatActivity {
    private RecyclerView rvCustomers;
    private CustomerAdapter adapter;
    private List<User> customerList;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_management);

        // Khởi tạo RecyclerView
        rvCustomers = findViewById(R.id.rvCustomers);
        rvCustomers.setLayoutManager(new LinearLayoutManager(this));
        customerList = new ArrayList<>();
        adapter = new CustomerAdapter(this, customerList);
        rvCustomers.setAdapter(adapter);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản lý khách hàng");

        // Firebase
        usersRef = FirebaseDatabase.getInstance().getReference("User");
        loadCustomers();

        // Xử lý click item (chuyển sang chi tiết)
        adapter.setOnItemClickListener(user -> {
            Intent intent = new Intent(this, CustomerDetailActivity.class);
            intent.putExtra("userId", user.getId());
            startActivity(intent);
        });

    }

    // Tải danh sách khách hàng từ Firebase
    private void loadCustomers() {
        usersRef.orderByChild("role").equalTo("customer").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user != null) {
                        user.setId(data.getKey());
                        customerList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerManagementActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}