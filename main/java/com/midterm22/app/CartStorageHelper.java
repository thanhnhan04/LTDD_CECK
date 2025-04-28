package com.midterm22.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.midterm22.app.model.Cart;

import java.lang.reflect.Type;

public class CartStorageHelper {
    private static final String PREF_NAME = "cart_pref";
    private static final String KEY_CART = "cart_data";

    public static void saveCart(Context context, Cart cart, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(cart);
        editor.putString(KEY_CART + "_" + userId, json);  // Store cart with user ID as part of the key
        editor.apply();
    }
    public static void clearCart(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // dùng PREF_NAME đúng
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_CART + "_" + userId); // remove theo đúng key đã lưu
        editor.apply();
    }

    public static Cart getCart(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_CART + "_" + userId, null);  // Retrieve cart based on user ID
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<Cart>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new Cart();
    }
}
