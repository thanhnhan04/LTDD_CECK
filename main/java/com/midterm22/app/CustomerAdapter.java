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

import com.midterm22.app.model.User;

import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {

    private Context context;
    private List<User> customerList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onDetailsClick(User user);  // Single method for handling the details click
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CustomerAdapter(Context context, List<User> customerList) {
        this.context = context;
        this.customerList = customerList;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        User user = customerList.get(position);

        // Set user details
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText("Email: " + user.getEmail());
        holder.tvStatus.setText(user.isActive() ? "Trạng thái: Đang hoạt động" : "Trạng thái: Đã khóa");

        // Set color for status
        int statusColor = user.isActive() ? 0xFF4CAF50 : 0xFFF44336; // green or red
        holder.tvStatus.setTextColor(statusColor);

        // Set the avatar (if any)
        holder.imgAvatar.setImageResource(R.drawable.user);

        // Handle click on details button
        holder.btnDetails.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetailsClick(user);  // Use the method from listener
            }
        });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    public static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvCustomerId, tvName, tvEmail, tvStatus;
        ImageView imgAvatar;
        Button btnDetails;

        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
