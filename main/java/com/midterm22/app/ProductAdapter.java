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

import com.bumptech.glide.Glide;
import com.midterm22.app.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnFoodActionListener listener;

    public interface OnFoodActionListener {
        void onEdit(Product product);
        void onHide(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }
    public void setOnFoodActionListener(OnFoodActionListener listener) {
        this.listener = listener;
    }
    public void setProductList(List<Product> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_admin, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvDescription.setText(product.getDescription());
        holder.tvPrice.setText(String.format("%.0fđ", product.getPrice()));
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(product);
            }
        });

        // Ẩn / Xoá (tuỳ bạn xử lý bên ngoài)
        holder.btnHide.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHide(product);
            }
        });

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.load_img) // ảnh tạm thời
                .error(R.drawable.error_image)       // ảnh nếu lỗi
                .into(holder.imgProduct);

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice;
        ImageView imgProduct;
         Button btnEdit, btnHide;


        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvDescription = itemView.findViewById(R.id.tv_product_description);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            imgProduct = itemView.findViewById(R.id.img_product);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnHide = itemView.findViewById(R.id.btn_hide);
        }
    }
}
