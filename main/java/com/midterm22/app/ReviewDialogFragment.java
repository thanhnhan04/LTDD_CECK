package com.midterm22.app;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.midterm22.app.model.Review;

public class ReviewDialogFragment extends DialogFragment {

    private static final String ARG_ORDER_ID = "order_id";
    private String orderId;

    public static ReviewDialogFragment newInstance(String orderId) {
        ReviewDialogFragment fragment = new ReviewDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.activity_review_dialog_fragment, null);

        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        EditText editReview = view.findViewById(R.id.editReview);

        builder.setView(view)
                .setTitle("Đánh giá đơn hàng")
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String reviewText = editReview.getText().toString();

                    // Lưu vào Firebase
                    DatabaseReference reviewRef = FirebaseDatabase.getInstance()
                            .getReference("reviews")
                            .child(orderId);

                    reviewRef.setValue(new Review(rating, reviewText))
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Đã gửi đánh giá", Toast.LENGTH_SHORT).show();
                                dismiss(); // Đóng dialog
                                // Quay về ListOrderActivity nếu cần:
                                Intent intent = new Intent(getActivity(), ListOrderActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                })
                .setNegativeButton("Hủy", null);

        return builder.create();
    }
}
