package com.midterm22.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
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
            // Lấy vị trí của nút "Add to Cart"
            int[] startLocation = new int[2];
            holder.btnAddToCart.getLocationInWindow(startLocation);

            // Lấy vị trí và kích thước của giỏ hàng (cart_button) từ BaseActivity
            int[] endLocation = new int[2];
            View cartView = ((BaseActivity) context).findViewById(R.id.cart_button);  // Truy cập `cart_button` trong BaseActivity
            cartView.getLocationInWindow(endLocation);

            // Lấy chiều rộng và chiều cao của giỏ hàng
            int cartWidth = cartView.getWidth();
            int cartHeight = cartView.getHeight();

            // Tính toán vị trí căn giữa của giỏ hàng
            int centerX = endLocation[0] + cartWidth / 2;
            int centerY = endLocation[1] + cartHeight / 2;


            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.tin);
            mediaPlayer.setOnCompletionListener(mp -> {
                mp.release();  // Giải phóng bộ nhớ
            });
            mediaPlayer.start();

            // Tạo image di chuyển từ nút "Add to Cart" đến giỏ hàng
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.ic_pickup);
            imageView.setColorFilter(0xFFFFA000, android.graphics.PorterDuff.Mode.SRC_IN);            imageView.setLayoutParams(new ViewGroup.LayoutParams(80, 80)); // Kích thước icon
            ((ViewGroup) holder.itemView.getRootView()).addView(imageView); // Thêm vào layout cha

            // Tạo chuyển động từ nút đến giỏ hàng
            ObjectAnimator translateX = ObjectAnimator.ofFloat(imageView, "translationX", startLocation[0], centerX - 40);  // 40 là bán kính của icon để căn giữa
            ObjectAnimator translateY = ObjectAnimator.ofFloat(imageView, "translationY", startLocation[1], centerY - 40);  // 40 là bán kính của icon để căn giữa
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);  // Giảm độ mờ để làm icon biến mất
            fadeOut.setStartDelay(500);  // Đợi cho animation di chuyển xong rồi mới bắt đầu fadeOut

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(translateX, translateY, fadeOut);  // Kết hợp các animation
            animatorSet.setDuration(1000); // Thời gian di chuyển (1000ms)
            animatorSet.start();



            // Sau khi di chuyển hoàn tất, thêm sản phẩm vào giỏ hàng
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);

                    // Sau khi di chuyển và icon biến mất, thêm sản phẩm vào giỏ hàng
                    CartItem item = new CartItem();
                    item.setProductId(product.getId());
                    item.setProductName(product.getName());
                    item.setProductImageUrl(product.getImageUrl());
                    item.setPrice(product.getPrice());
                    item.setQuantity(1);
                    item.setCreatedAt(String.valueOf(System.currentTimeMillis()));

                    addToCart(item); // Thêm vào giỏ hàng

                    // Ẩn icon sau khi animation kết thúc (nếu chưa thực hiện fadeOut)
                    imageView.setVisibility(View.GONE);
                }
            });
        });

    }



    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDescription, tvPrice;
        ImageView imgProduct, btnAddToCart;

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
