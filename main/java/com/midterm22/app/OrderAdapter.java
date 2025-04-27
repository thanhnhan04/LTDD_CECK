package com.midterm22.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.midterm22.app.R;
import com.midterm22.app.model.Order;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private final List<Order> orders;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public OrderAdapter(List<Order> orders, OnItemClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvOrderDate, tvCustomerName, tvTotalAmount, tvStatus;
        Button btnViewDetail;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetail = itemView.findViewById(R.id.btnViewDetail);
        }
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);

        // Hiển thị thông tin đơn hàng
        holder.tvOrderId.setText(order.getId());

        // Định dạng ngày từ "2025-04-10 17:51:23" thành "10/04/2025 17:51"
        String createdAt = order.getCreatedAt();
        String formattedDate = createdAt;
        try {
            String[] parts = createdAt.split(" ");
            String[] dateParts = parts[0].split("-");
            formattedDate = String.format("%s/%s/%s %s", dateParts[2], dateParts[1], dateParts[0], parts[1].substring(0, 5));
        } catch (Exception e) {
            // Nếu định dạng không đúng, giữ nguyên giá trị
        }
        holder.tvOrderDate.setText(formattedDate);

        holder.tvTotalAmount.setText(String.format(Locale.getDefault(), "%.0f VNĐ", order.getTotal()));
        holder.tvStatus.setText(order.getStatus().substring(0, 1).toUpperCase() + order.getStatus().substring(1));

        // Lấy tên khách hàng từ Firebase dựa trên customerId
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(order.getCustomerId());
        userRef.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                holder.tvCustomerName.setText(name != null ? name : "Unknown");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.tvCustomerName.setText("Unknown");
                // Hiển thị thông báo lỗi nếu cần
                // Toast.makeText(holder.itemView.getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Gán background cho trạng thái
        switch (order.getStatus().toLowerCase()) {
            case "pending":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
            case "processing":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_processing);
                break;
            case "shipping":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_shipping);
                break;
            case "completed":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                break;
            case "cancelled":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled);
                break;
            default:
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
                break;
        }

        holder.btnViewDetail.setOnClickListener(v -> listener.onItemClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }
}