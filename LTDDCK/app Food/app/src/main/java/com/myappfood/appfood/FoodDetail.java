package com.myappfood.appfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.myappfood.appfood.Common.Common;
import com.myappfood.appfood.Database.Database;
import com.myappfood.appfood.Model.Food;
import com.myappfood.appfood.Model.Order;
import com.myappfood.appfood.Model.Rating;
import com.myappfood.appfood.ViewHolder.FoodViewHolder;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.lang.reflect.Array;
import java.util.Arrays;

import info.hoang8f.widget.FButton;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnrating;
    ElegantNumberButton numberButton;
    CounterFab btnCart;

    RatingBar ratingBar;
    String foodId="";

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;
    Food currentfood;
    FButton btnShowComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        //firebase
        database = FirebaseDatabase.getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        foods = database.getReference("Foods");

        ratingTbl = database.getReference("Rating");

        btnShowComment=(FButton)findViewById(R.id.btnShowComment);
        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                startActivity(intent);
            }
        });
        numberButton=(ElegantNumberButton) findViewById(R.id.number_button);
        btnCart=(CounterFab) findViewById(R.id.btnCart);
        btnrating=(FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar=(RatingBar)findViewById(R.id.ratingBar);
        btnrating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }
        });
        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Database(getBaseContext()).addToCart(new Order(
                        foodId,
                        currentfood .getName(),
                        numberButton.getNumber(),
                        currentfood.getPrice(),
                        currentfood.getDiscount(),
                        currentfood.getImage()

                ));
                Toast.makeText(FoodDetail.this, "Add to Cart", Toast.LENGTH_SHORT).show();
            }
        });
        btnCart.setCount(new Database(this).getCountCart());

        food_description=(TextView) findViewById(R.id.food_description);
        food_name=(TextView) findViewById(R.id.food_name);
        food_price=(TextView) findViewById(R.id.food_price);
        food_image=(ImageView) findViewById(R.id.img_food);
        collapsingToolbarLayout=(CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        //get food id
        if (getIntent() !=null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()){
            if (Common.isConnectedToInterner(getBaseContext()))
            {
                getDetailFood(foodId);
                getRating(foodId);
            }
            else {
                Toast.makeText(FoodDetail.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void getRating(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               for (DataSnapshot postSnapshort:dataSnapshot.getChildren()){
                   Rating item=postSnapshort.getValue(Rating.class);
                   sum+= Integer.parseInt(item.getRateValue());
                   count++;
               }
               if (count!=0){
                   float average = sum/count;
                   ratingBar.setRating(average);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedBack")
                .setTitleTextColor(R.color.purple_700)
                .setDescriptionTextColor(R.color.purple_700)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.teal_200)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.black)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentfood=dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(currentfood.getImage())
                        .into(food_image);
                collapsingToolbarLayout.setTitle(currentfood.getName());
                food_price.setText(currentfood.getPrice());
                food_description.setText(currentfood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NonNull String comments) {
        Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,String.valueOf(value),
                comments);
        //fix rate multiple time
        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this, "Thank Your For Submit Rating!!!", Toast.LENGTH_SHORT).show();

                    }
                });
//        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child(Common.currentUser.getPhone()).exists()){
//                    //remove old value
//                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
//                    //update new value
//                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
//                }
//                else {
//                    //update new value
//                    ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
//                }
//                Toast.makeText(FoodDetail.this, "Thank Your For Submit Rating!!!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
    }
}