package com.midterm22.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm22.app.R;
import com.midterm22.app.model.OrderItem;

import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private final List<OrderItem> orderItems;

    public OrderItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvUnitPrice, tvQuantity;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvUnitPrice = itemView.findViewById(R.id.tvUnitPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_item, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvUnitPrice.setText(String.format(Locale.getDefault(), "%.0f đ", item.getUnitPrice()));
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "Số lượng: %d", item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }
}