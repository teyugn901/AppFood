package com.myappfood.appfood.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myappfood.appfood.Interface.ItemClickListener;
import com.myappfood.appfood.Model.Food;
import com.myappfood.appfood.R;

import java.util.ArrayList;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    ArrayList<Food> list;
    public TextView food_name,food_price;
    public ImageView food_image,fav_image,share_image,quick_cart;
    private ItemClickListener itemClickListener;

    public FoodViewHolder(@NonNull View itemView, ArrayList<Food> list) {
        super(itemView);
        this.list = list;
    }


    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);

        food_name=(TextView) itemView.findViewById(R.id.food_name);
        food_image=(ImageView) itemView.findViewById(R.id.food_image);
        fav_image=(ImageView) itemView.findViewById(R.id.fav);
        share_image=(ImageView) itemView.findViewById(R.id.btnShare);
        food_price=(TextView)itemView.findViewById(R.id.food_price);
        quick_cart=(ImageView)itemView.findViewById(R.id.btn_quick_cart);
        itemView.setOnClickListener(this);
    }
    public FoodViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        this.itemClickListener = itemClickListener;
    }
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view,getAdapterPosition(),false);


    }
}
