package com.midterm22.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.midterm22.app.model.Cart;
import com.midterm22.app.model.CartItem;
import com.midterm22.app.model.Product;
import com.midterm22.app.R;

import java.lang.reflect.Type;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;
    private OnProductClickListener listener;

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public void setProductList(List<Product> list) {
        this.productList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvDescription.setText(product.getDescription());
        holder.tvPrice.setText(String.format("%.0fđ", product.getPrice()));

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.load_img)
                .error(R.drawable.error_image)
                .into(holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            CartItem item = new CartItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductImageUrl(product.getImageUrl());
            item.setPrice(product.getPrice());
            item.setQuantity(1);
            item.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            Log.d("AddToCart", "Add to cart clicked for product: " + product.getName());

            addToCart(item);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice;
        ImageView imgProduct;
        ImageView btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvDescription = itemView.findViewById(R.id.tv_product_description);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            imgProduct = itemView.findViewById(R.id.img_product);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }
    }

    private void addToCart(CartItem cartItem) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("AddToCart", "User is not logged in.");
            return;
        }

        String userId = currentUser.getUid();  // Get the current user's unique ID
        Cart cart = CartStorageHelper.getCart(context, userId);  // Pass the user ID to load the cart

        if (cart.getItems().containsKey(cartItem.getProductId())) {
            CartItem existing = cart.getItems().get(cartItem.getProductId());
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            cart.getItems().put(cartItem.getProductId(), cartItem);
        }

        CartStorageHelper.saveCart(context, cart, userId);  // Pass the user ID to save the cart
        Log.d("AddToCart", "Item saved. Total items: " + cart.getItems().size());
    }


    private Cart getCartFromStorage() {
        SharedPreferences prefs = context.getSharedPreferences("cart", Context.MODE_PRIVATE);
        String cartJson = prefs.getString("cart_data", null);
        if (cartJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<Cart>() {}.getType();
            return gson.fromJson(cartJson, type);
        }
        return new Cart();
    }

    private void updateCartInStorage(Cart cart) {
        SharedPreferences prefs = context.getSharedPreferences("cart", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String cartJson = gson.toJson(cart);
        editor.putString("cart_data", cartJson);
        editor.apply();
    }

}
