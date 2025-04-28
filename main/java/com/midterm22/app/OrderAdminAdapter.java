package com.midterm22.app;

import android.content.Context;
import android.util.Log;
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
import com.midterm22.app.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdminAdapter extends RecyclerView.Adapter<OrderAdminAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdminAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvCustomerName, tvTotal, tvStatus;
        Button btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvTotal = itemView.findViewById(R.id.tvTotalAmount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetail);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            // Hiển thị mã đơn hàng
            if (tvOrderId != null) {
                tvOrderId.setText("Đơn hàng #" + (order.getId() != null ? order.getId().substring(0, Math.min(6, order.getId().length())) : "N/A"));
            }

            // Định dạng ngày tháng
            String createdAt = order.getCreatedAt();
            String formattedDate = createdAt != null ? createdAt : "N/A";
            try {
                long timestamp = Long.parseLong(createdAt);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                formattedDate = sdf.format(new Date(timestamp));
            } catch (NumberFormatException e) {
                try {
                    String[] parts = createdAt.split(" ");
                    String[] dateParts = parts[0].split("-");
                    formattedDate = String.format("%s/%s/%s %s", dateParts[2], dateParts[1], dateParts[0], parts[1].substring(0, 5));
                } catch (Exception ex) {
                    Log.w("OrderAdapter", "Error formatting date: " + createdAt);
                }
            }
            if (tvDate != null) {
                tvDate.setText(formattedDate);
            }

            // Truy vấn trực tiếp tên khách hàng từ Firebase
            String customerId = order.getCustomerId();
            if (customerId != null && !customerId.isEmpty()) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("User").child(customerId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String customerName = snapshot.child("name").getValue(String.class);
                            if (customerName != null) {
                                if (tvCustomerName != null) {
                                    tvCustomerName.setText(customerName);
                                }
                                Log.d("OrderAdapter", "Order ID=" + order.getId() + ", Customer Name=" + customerName);
                            } else {
                                if (tvCustomerName != null) {
                                    tvCustomerName.setText("Khách hàng không tồn tại (ID: " + customerId + ")");
                                }
                                Log.w("OrderAdapter", "Customer name not found for ID: " + customerId + ", snapshot exists but 'name' field is missing");
                            }
                        } else {
                            if (tvCustomerName != null) {
                                tvCustomerName.setText("Khách hàng không tồn tại (ID: " + customerId + ")");
                            }
                            Log.w("OrderAdapter", "Customer not found for ID: " + customerId + ", snapshot does not exist");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (tvCustomerName != null) {
                            tvCustomerName.setText("Lỗi: " + error.getMessage());
                        }
                        Log.e("OrderAdapter", "Error loading customer for ID: " + customerId + ", Error: " + error.getMessage());
                    }
                });
            } else {
                if (tvCustomerName != null) {
                    tvCustomerName.setText("Khách hàng không xác định");
                }
                Log.w("OrderAdapter", "Customer ID is null or empty for order: " + order.getId());
            }

            // Hiển thị tổng tiền
            if (tvTotal != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvTotal.setText(format.format(order.getTotal()));
            }

            // Hiển thị trạng thái
            if (tvStatus != null) {
                tvStatus.setText(getStatusText(order.getStatus()));
                tvStatus.setTextColor(getStatusColor(order.getStatus()));
            }

            // Sự kiện click cho nút Xem chi tiết
            if (btnViewDetails != null) {
                btnViewDetails.setOnClickListener(v -> listener.onOrderClick(order));
            }
        }

        private String getStatusText(String status) {
            if (status == null) return "N/A";
            switch (status) {
                case "pending":
                    return "Pending";
                case "Confirm":
                    return "Confirm";
                case "Shipping":
                    return "Shipping";
                case "completed":
                    return "Complete";
                case "cancelled":
                    return "Cancelled";
                default:
                    return status;
            }
        }

        private int getStatusColor(String status) {
            if (status == null) return 0xFF000000; // Black
            switch (status) {
                case "pending":
                    return 0xFFFB8C00; // Orange
                case "confirm":
                    return 0xFFFFC107; // Yellow (Amber)
                case "processing":
                    return 0xFF1976D2; // Blue
                case "completed":
                    return 0xFF388E3C; // Green
                case "cancelled":
                    return 0xFFD32F2F; // Red
                default:
                    return 0xFF000000; // Black
            }
        }
    }
}