package com.midterm22.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm22.app.R;
import com.midterm22.app.model.Order;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
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
        TextView tvOrderId, tvDate, tvTotal, tvStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            tvOrderId.setText("Đơn hàng #" + order.getId().substring(0, 6));
            tvDate.setText(order.getCreatedAt());

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvTotal.setText(format.format(order.getTotal()));

            tvStatus.setText(getStatusText(order.getStatus()));
            tvStatus.setTextColor(getStatusColor(order.getStatus()));

            itemView.setOnClickListener(v -> listener.onOrderClick(order));
        }

        private String getStatusText(String status) {
            switch (status) {
                case "Pending": return "Chờ xử lý";
                case "Confirm": return "Xác nhận";
                case "Shipping": return "Đang giao";
                case "Complete": return "Hoàn thành";
                case "Cancelled": return "Đã hủy";
                default: return status;
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "Pending": return 0xFFFB8C00; // Orange
                case "Confirm": return 0xFF4CAF50; // Green
                case "Shipping": return 0xFF1976D2; // Blue
                case "Complete": return 0xFF388E3C; // Green
                case "Cancelled": return 0xFFD32F2F; // Red
                default: return 0xFF000000; // Black
            }
        }
    }
}