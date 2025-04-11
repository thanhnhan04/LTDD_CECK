package com.midterm22.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.midterm22.app.R;
import com.midterm22.app.model.OrderItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder> {

    private Context context;
    private List<OrderItem> orderItems;

    public OrderItemAdapter(Context context, List<OrderItem> orderItems) {
        this.context = context;
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItems.size();
    }

    static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProduct;
        TextView tvName, tvPrice, tvQuantity, tvTotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProduct = itemView.findViewById(R.id.ivProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotal = itemView.findViewById(R.id.tvTotal);
        }

        public void bind(OrderItem item) {
            // Xử lý ảnh sản phẩm
            if (item.getProductImageUrl() != null && !item.getProductImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getProductImageUrl())
                        .placeholder(R.drawable.load_img) // ảnh tạm thời
                        .error(R.drawable.error_image)       // ảnh nếu lỗi// Ảnh hiển thị khi lỗi
                        .into(ivProduct);
            } else {
                ivProduct.setImageResource(R.drawable.error_image); // Ảnh mặc định
            }

            tvName.setText(item.getProductName());

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvPrice.setText(format.format(item.getUnitPrice()));
            tvQuantity.setText("x" + item.getQuantity());
            tvTotal.setText(format.format(item.getTotalPrice()));
        }
    }
}