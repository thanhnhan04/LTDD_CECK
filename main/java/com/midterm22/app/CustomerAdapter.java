package com.midterm22.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm22.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private List<User> activeCustomerList;
    private List<User> inactiveCustomerList;
    private OnItemClickListener listener;

    // Màu sắc cho trạng thái
    private static final int ACTIVE_COLOR = 0xFF4CAF50; // Màu xanh cho trạng thái "Đang hoạt động"
    private static final int INACTIVE_COLOR = 0xFFF44336; // Màu đỏ cho trạng thái "Đã khóa"

    public interface OnItemClickListener {
        void onDetailsClick(User user);  // Single method for handling the details click
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    // Constructor for the adapter with initial active and inactive customer lists
    public CustomerAdapter(Context context) {
        this.context = context;
        this.activeCustomerList = new ArrayList<>();
        this.inactiveCustomerList = new ArrayList<>();
    }

    // Method to update the active customer list
    public void setActiveCustomers(List<User> customers) {
        this.activeCustomerList = customers;
        notifyDataSetChanged();
    }

    // Method to update the inactive customer list
    public void setInactiveCustomers(List<User> customers) {
        this.inactiveCustomerList = customers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        // Determine which list (active or inactive) is being displayed
        User user = activeCustomerList.size() > 0 ? activeCustomerList.get(position) : inactiveCustomerList.get(position);

        // Set user details
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText("Email: " + user.getEmail());

        // Thay đổi điều kiện kiểm tra trạng thái
        if ("locked".equals(user.getRole())) {
            holder.tvStatus.setText("Trạng thái: Đã khóa");
            holder.tvStatus.setTextColor(INACTIVE_COLOR); // Màu đỏ cho trạng thái bị khóa
        } else {
            holder.tvStatus.setText("Trạng thái: Đang hoạt động");
            holder.tvStatus.setTextColor(ACTIVE_COLOR); // Màu xanh cho trạng thái đang hoạt động
        }

        // Set default avatar image
        holder.imgAvatar.setImageResource(R.drawable.user); // Default image

        // Handle click on details button
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(user);  // Use the method from listener
            }
        });
    }


    @Override
    public int getItemCount() {
        // Return the total number of items in the active or inactive list based on the displayed category
        if (!activeCustomerList.isEmpty()) {
            return activeCustomerList.size();
        } else {
            return inactiveCustomerList.size();
        }
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvStatus;
        ImageView imgAvatar;
        Button btnDetails;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
