package com.myappfood.appfood.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myappfood.appfood.Interface.ItemClickListener;
import com.myappfood.appfood.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtOrderId,txtOrderStatus,txtOrderPhone,txtOrderAddress;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);
        txtOrderId=(TextView) itemView.findViewById(R.id.order_id);
        txtOrderAddress=(TextView) itemView.findViewById(R.id.order_address);
        txtOrderPhone=(TextView) itemView.findViewById(R.id.order_phone);
        txtOrderStatus=(TextView) itemView.findViewById(R.id.order_status);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public OrderViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {

        itemClickListener.onClick(view,getAdapterPosition(),false);
    }
}
