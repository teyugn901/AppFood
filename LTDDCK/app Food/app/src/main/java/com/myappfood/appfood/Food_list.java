package com.myappfood.appfood;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.myappfood.appfood.Common.Common;
import com.myappfood.appfood.Database.Database;
import com.myappfood.appfood.Interface.ItemClickListener;
import com.myappfood.appfood.Model.Category;
import com.myappfood.appfood.Model.Food;
import com.myappfood.appfood.Model.Order;
import com.myappfood.appfood.ViewHolder.FoodViewHolder;
import com.myappfood.appfood.ViewHolder.MenuViewHolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

public class Food_list extends AppCompatActivity {
    RecyclerView recycler_food;
    RecyclerView.LayoutManager layoutManager;

    int mCartItemCount = 10;

    FirebaseDatabase database;
    DatabaseReference foodList;

    String categoryId="";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;


   Database localDB;

    SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);


        database = FirebaseDatabase.getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        foodList = database.getReference("Foods");

        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.teal_700,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //get Intent
                if (getIntent() !=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId !=null){
                    if (Common.isConnectedToInterner(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(Food_list.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                //get Intent
                if (getIntent() !=null)
                    categoryId = getIntent().getStringExtra("CategoryId");
                if (!categoryId.isEmpty() && categoryId !=null){
                    if (Common.isConnectedToInterner(getBaseContext()))
                        loadListFood(categoryId);
                    else {
                        Toast.makeText(Food_list.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
        localDB= new Database(this);
        recycler_food=(RecyclerView) findViewById(R.id.recycler_food);
        recycler_food.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recycler_food.setLayoutManager(layoutManager);


    }




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        //fix click back button category
        if (adapter!= null)
            adapter.startListening();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!= null)
            adapter.startListening();
    }

    private void loadListFood(String categoryId) {

        Query query = FirebaseDatabase .getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app/") .getReference("Foods") .orderByChild("menuId").equalTo(categoryId);//select from food where Menuid is categoru
        //query =category.orderByKey();
        FirebaseRecyclerOptions<Food> options;
        options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query, Food.class).build();
        adapter= new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {
                viewHolder.food_name.setText(model.getName());
                viewHolder.food_price.setText(String.format("$ %s",model.getPrice().toString()));

                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.food_image);
                //quick cart
                viewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getBaseContext()).addToCart(new Order(
                                adapter.getRef(position).getKey(),
                                model .getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount(),
                                model.getImage()

                        ));
                        Toast.makeText(Food_list.this, "Add to Cart", Toast.LENGTH_SHORT).show();

                    }
                });
                //add favorite
                if (localDB.isFavorite(adapter.getRef(position).getKey()))
                    viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);

                //click share
                viewHolder.share_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });
                //click favorite
                viewHolder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDB.isFavorite(adapter.getRef(position).getKey()))
                        {
                            localDB.addToFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_24);
                            Toast.makeText(Food_list.this, ""+model.getName()+" was add to Favorites", Toast.LENGTH_SHORT).show();
                        }else {
                            localDB.removeFromFavorites(adapter.getRef(position).getKey());
                            viewHolder.fav_image.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                            Toast.makeText(Food_list.this, ""+model.getName()+" was removed to Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                final Food local = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int postion, boolean isLongClick) {
                        Intent foodDetail = new Intent(Food_list.this,FoodDetail.class);
                        foodDetail.putExtra("FoodId",adapter.getRef(postion).getKey());
                        startActivity(foodDetail);
                    }
                });
            }
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(view);
            }
        };
       //set Adepter
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),1);
        adapter.startListening();
        recycler_food.setLayoutManager(gridLayoutManager);
        recycler_food.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

      }


    @Override
    protected void onStop() {
        super.onStop();
//        adapter.stopListening();
    }
}
