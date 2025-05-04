package com.midterm22.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm22.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private List<User> customerList;
    private List<User> customerListFull;  // List full để thực hiện tìm kiếm
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDetailsClick(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CustomerAdapter(Context context) {
        this.context = context;
        this.customerList = new ArrayList<>();
        this.customerListFull = new ArrayList<>();  // Khởi tạo danh sách đầy đủ
    }

    public void setCustomers(List<User> customers, boolean isActive) {
        if (customers != null) {
            this.customerList = customers;
            this.customerListFull = new ArrayList<>(customers);  // Copy dữ liệu ban đầu vào danh sách đầy đủ
            notifyDataSetChanged();
        }
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

        boolean isLocked = "locked".equals(user.getRole());
        holder.tvStatus.setText(isLocked ? "Trạng thái: Đã khóa" : "Trạng thái: Đang hoạt động");
        holder.tvStatus.setTextColor(ContextCompat.getColor(
                context,
                isLocked ? R.color.red : R.color.green // Tạo 2 màu trong colors.xml
        ));

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

    public void filter(String text) {
        customerList.clear();
        if (text.isEmpty()) {
            customerList.addAll(customerListFull);  // Nếu không tìm kiếm, hiển thị toàn bộ danh sách
        } else {
            text = text.toLowerCase();
            for (User user : customerListFull) {
                if (user.getName().toLowerCase().contains(text)) {  // Tìm kiếm theo tên khách hàng
                    customerList.add(user);
                }
            }
        }
        notifyDataSetChanged();  // Cập nhật dữ liệu trong RecyclerView
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
