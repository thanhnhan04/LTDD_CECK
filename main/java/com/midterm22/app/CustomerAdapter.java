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
    private List<User> customerList;
    private OnItemClickListener listener;

    // Màu sắc cho trạng thái
    private static final int ACTIVE_COLOR = 0xFF4CAF50;  // Xanh
    private static final int INACTIVE_COLOR = 0xFFF44336;  // Đỏ

    public interface OnItemClickListener {
        void onDetailsClick(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CustomerAdapter(Context context) {
        this.context = context;
        this.customerList = new ArrayList<>();
    }

    public void setCustomers(List<User> customers, boolean isActiveList) {
        this.customerList = customers;
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
        User user = customerList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.imgAvatar.setImageResource(R.drawable.user);

        if ("locked".equals(user.getRole())) {
            holder.tvStatus.setText("Trạng thái: Đã khóa");
            holder.tvStatus.setTextColor(INACTIVE_COLOR);
        } else {
            holder.tvStatus.setText("Trạng thái: Đang hoạt động");
            holder.tvStatus.setTextColor(ACTIVE_COLOR);
        }

        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList != null ? customerList.size() : 0;
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
