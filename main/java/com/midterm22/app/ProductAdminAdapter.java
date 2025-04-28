package com.midterm22.app;

import android.content.Context;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class ProductAdminAdapter extends RecyclerView.Adapter<ProductAdminAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private List<Product> productListFull;
    private OnFoodActionListener listener;

    public ProductAdminAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList);
    }
    public void filter(String query) {
        productList.clear();
        if (query.isEmpty()) {
            productList.addAll(productListFull);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Product product : productListFull) {
                if (product.getName().toLowerCase().contains(lowerQuery) ||
                        product.getDescription().toLowerCase().contains(lowerQuery)) {
                    productList.add(product);
                }
            }
        }
        notifyDataSetChanged();
    }
    public void setProductList(List<Product> list) {
        this.productList = list;
        this.productListFull.clear();
        this.productListFull.addAll(list);
        notifyDataSetChanged();
    }
    public interface OnFoodActionListener {
        void onEdit(Product product);
        void onHide(Product product);
    }

    public void setOnFoodActionListener(OnFoodActionListener listener) {
        this.listener = listener;
    }



    // Phương thức lọc món ăn
    public void filterList(String query) {
        List<Product> filteredList = new ArrayList<>();
        // Kiểm tra từng sản phẩm trong danh sách gốc
        Log.d("FILTER_LIST", "All products in the list:");
        for (Product product : productListFull) {
            Log.d("FILTER_LIST", "Product name: " + product.getName() + ", Description: " + product.getDescription());  // In tên và mô tả sản phẩm

            // Lọc theo cả tên và mô tả
            if (product.getName().toLowerCase().contains(query.toLowerCase()) ||
                    product.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        Log.d("FILTER_LIST", "Filtered products: " + filteredList.size() + " items matching query: " + query);

        // Cập nhật danh sách sau khi lọc và thông báo thay đổi
        updateList(filteredList);
    }

    // Phương thức cập nhật danh sách mới và notifyDataSetChanged
    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();  // Cập nhật giao diện
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

        // Đổi tên nút dựa trên trạng thái
        if (product.isAvailable()) {
            holder.btnHide.setText("Ẩn");
        } else {
            holder.btnHide.setText("Hiện lại");
        }

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(product);
            }
        });

        holder.btnHide.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHide(product);
            }
        });

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.load_img)
                .error(R.drawable.error_image)
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