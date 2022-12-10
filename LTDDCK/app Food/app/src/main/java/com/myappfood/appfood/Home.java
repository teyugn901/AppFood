package com.myappfood.appfood;

import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.myappfood.appfood.Common.Common;
import com.myappfood.appfood.Database.Database;
import com.myappfood.appfood.Interface.ItemClickListener;
import com.myappfood.appfood.Model.Banner;
import com.myappfood.appfood.Model.Category;
import com.myappfood.appfood.Model.Token;
import com.myappfood.appfood.ViewHolder.MenuViewHolder;
import com.myappfood.appfood.databinding.ActivityHomeBinding;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    TextView txtFullname;



    int mCartItemCount = 10;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference category;

    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;

    SwipeRefreshLayout swipeRefreshLayout;
    CounterFab fab;
    //slider
    HashMap<String,String> image_list;
    SliderLayout mSlider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        
        //view
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.teal_700,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (Common.isConnectedToInterner(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(getBaseContext(), "Please check your connect!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        //load time
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                if (Common.isConnectedToInterner(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Home.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
        database = FirebaseDatabase.getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app/");
        category = database.getReference("Category");

        fab =(CounterFab) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cartIntent= new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
            }
        });
        fab.setCount(new Database(this).getCountCart());

        DrawerLayout drawer= (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,  R.string.navigation_drawer_open,
                R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txtFullname=(TextView)headerView.findViewById(R.id.txtFullname);
        txtFullname.setText(Common.currentUser.getName());



        //loadmenu

        recycler_menu=(RecyclerView) findViewById(R.id.recycler_menu);
        layoutManager= new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);

        if (Common.isConnectedToInterner(getBaseContext()))
          loadMenu();
        else {
            Toast.makeText(Home.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
            return;
        }
        
//        updateToken(FirebaseMessaging.getInstance().getToken());
        //set slider
        setupSlider();

    }

    private void setupSlider() {
        mSlider = (SliderLayout) findViewById(R.id.slider);
        image_list= new HashMap<>();

        DatabaseReference banner = database.getReference("Banner");

        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshort:dataSnapshot.getChildren())
                {
                    //piza_01
                    Banner banner = postSnapshort.getValue(Banner.class);
                    image_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for (String key:image_list.keySet())
                {
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String isOfFood = keySplit[1];

                    //creat slider
                   final TextSliderView textSliderView= new TextSliderView(getBaseContext());
                    textSliderView
                            .description(nameOfFood)
                            .image(image_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                                      Intent intent = new Intent(Home.this,FoodDetail.class);
                                      //we will send food id to fooddetail
                                      intent.putExtras(textSliderView.getBundle());
                                      startActivity(intent);
                                }
                            });
                    //add extras bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",isOfFood);
                    mSlider.addSlider(textSliderView);

                     //remove event after finish
                    banner.removeEventListener(this);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }


    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart());
        //fix click back button category
        if (adapter!= null)
            adapter.startListening();
    }

    private void loadMenu() {
        FirebaseRecyclerOptions<Category> options;
//        final FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter ;
        options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category,Category.class).build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int i, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);

                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodlList = new Intent(Home.this,Food_list.class);
                        foodlList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodlList);

                    }
                });

            }
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);
                return new MenuViewHolder(view);
            }


        };
        adapter.startListening();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(),2);

        recycler_menu.setLayoutManager(gridLayoutManager);
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);


    }

    @Override
    protected void onStop() {
        super.onStop();
//        adapter.stopListening();
        mSlider.stopAutoCycle();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer=(DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.searchmenu,menu);
        getMenuInflater().inflate(R.menu.home,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.refresh)
            loadMenu();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id== R.id.nav_home) {
        }else if (id== R.id.nav_cart){
           startActivity(new Intent(Home.this,Cart.class));
        }
        else if (id== R.id.nav_orthes)
        {
            startActivity(new Intent(Home.this,OrderStatus.class));
        }
        else if ( id==R.id.nav_profile)
        {
            startActivity(new Intent());
        }
        else if (id==R.id.nav_log_out){
            Intent signIn = new Intent(Home.this,Signin.class);
            signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(signIn);

        }else if (id==R.id.nav_chage_pwd){
            showChangPassworDiaglog();

        }

        
        DrawerLayout drawer =(DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showChangPassworDiaglog() {
        AlertDialog.Builder alertdiglog = new AlertDialog.Builder(Home.this);
        alertdiglog.setTitle("Change Password");
        alertdiglog.setMessage("Please fill all information");

        LayoutInflater inflater= LayoutInflater.from(this);
        View layout_pwd= inflater.inflate(R.layout.change_passwoed_layout,null);

        MaterialEditText editPassword=(MaterialEditText) layout_pwd.findViewById(R.id.editPassword);
        MaterialEditText editNewPassword=(MaterialEditText) layout_pwd.findViewById(R.id.editNewPassword);
        MaterialEditText editRepeatPassword=(MaterialEditText) layout_pwd.findViewById(R.id.editRepeatPassword);

        alertdiglog.setView(layout_pwd);
        //button
        alertdiglog.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {

                //suportDiaglog githup
                android.app.AlertDialog waitingDiaglog= new SpotsDialog(Home.this);
                waitingDiaglog.show();

                //check pass old
                if (editPassword.getText().toString().equals(Common.currentUser.getPassword())){
                    if (editNewPassword.getText().toString().equals(editRepeatPassword.getText().toString()))
                    {
                        Map<String,Object> passwordUpdate= new HashMap<>();
                        passwordUpdate.put("Password",editNewPassword.getText().toString());

                        //make update
                        DatabaseReference user =FirebaseDatabase.getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(passwordUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        waitingDiaglog.dismiss();
                                        Toast.makeText(Home.this, "Password was update", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Home.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else {
                        waitingDiaglog.dismiss();
                        Toast.makeText(Home.this, "New password doesn't match", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    waitingDiaglog.dismiss();
                    Toast.makeText(Home.this, "Wrong old password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertdiglog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
               dialog.dismiss();
            }
        });
      alertdiglog.show();
    }
}