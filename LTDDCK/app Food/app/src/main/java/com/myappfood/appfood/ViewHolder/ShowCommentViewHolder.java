package com.myappfood.appfood.ViewHolder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myappfood.appfood.R;

public class ShowCommentViewHolder extends RecyclerView.ViewHolder {
    public TextView txtUserPhone,txtComment;
    public RatingBar ratingBar;
    public ShowCommentViewHolder(@NonNull View itemView) {
        super(itemView);
        txtComment=(TextView) itemView.findViewById(R.id.txtComment);
        txtUserPhone=(TextView) itemView.findViewById(R.id.txtUserPhone);
        ratingBar=(RatingBar) itemView.findViewById(R.id.ratingBar);

    }
}
