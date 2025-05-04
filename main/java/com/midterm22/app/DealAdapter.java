package com.midterm22.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private List<String> dealList;

    public DealAdapter(List<String> dealList) {
        this.dealList = dealList;
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deal, parent, false);
        return new DealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        String deal = dealList.get(position);
        holder.tvDealTitle.setText("Ưu đãi #" + (position + 1));
        holder.tvDealDesc.setText(deal);
    }

    @Override
    public int getItemCount() {
        return dealList.size();
    }

    public static class DealViewHolder extends RecyclerView.ViewHolder {
        TextView tvDealTitle, tvDealDesc;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDealTitle = itemView.findViewById(R.id.tvDealTitle);
            tvDealDesc = itemView.findViewById(R.id.tvDealDesc);
        }
    }
}
