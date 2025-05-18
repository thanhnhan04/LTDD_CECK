package com.midterm22.app;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.midterm22.app.model.Cart;
import com.midterm22.app.model.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItemList;
    private Map<String, CartItem> cartItemMap;
    private Context context;
    private OnCartTotalChangedListener cartTotalChangedListener;

    public CartAdapter(Map<String, CartItem> cartItems) {
        this.cartItemMap = cartItems;
        this.cartItemList = new ArrayList<>(cartItems.values());
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        holder.tvName.setText(item.getProductName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(String.format("%.0fđ", item.getPrice() * item.getQuantity()));

        Glide.with(context)
                .load(item.getProductImageUrl())
                .placeholder(R.drawable.placeholder)
                .into(holder.img_food);

        holder.btnIncrease.setOnClickListener(v -> {
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.tin);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();  // Giải phóng bộ nhớ
            });
            mediaPlayer.start();
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            notifyCartChanged();
        });

        holder.btnDecrease.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.tin);
                mediaPlayer.setOnCompletionListener(mp -> {
                    mp.release();
                });
                mediaPlayer.start();
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                notifyCartChanged();
            }
        });
        holder.btnDelete.setOnClickListener(v -> {
            cartItemMap.remove(item.getProductId());
            cartItemList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItemList.size());

            // Lưu lại cart mới
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                // Tạo cart mới từ map
                Cart updatedCart = new Cart();
                updatedCart.setItems(cartItemMap);

                // Lưu cart
                CartStorageHelper.saveCart(holder.itemView.getContext(), updatedCart, currentUser.getUid());
            }

            // Gọi callback cập nhật tổng tiền
            if (cartTotalChangedListener != null) {
                cartTotalChangedListener.onCartTotalChanged(calculateTotal());
            }
        });

    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public void setOnCartTotalChangedListener(OnCartTotalChangedListener listener) {
        this.cartTotalChangedListener = listener;
    }

    private void notifyCartChanged() {
        if (cartTotalChangedListener != null) {
            cartTotalChangedListener.onCartTotalChanged(calculateTotal());
        }
    }

    private double calculateTotal() {
        double total = 0;
        for (CartItem item : cartItemList) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public interface OnCartTotalChangedListener {
        void onCartTotalChanged(double total);
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;
        ImageView img_food;
        ImageView btnIncrease, btnDecrease, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_food_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            img_food = itemView.findViewById(R.id.img_food);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
