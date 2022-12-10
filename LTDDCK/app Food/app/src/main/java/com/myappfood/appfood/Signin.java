package com.myappfood.appfood;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.myappfood.appfood.Common.Common;
import com.myappfood.appfood.Model.User;
import com.rengwuxian.materialedittext.MaterialEditText;

public class Signin extends AppCompatActivity {
    EditText editPhone,editPass;
    Button btnsignIn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lognin);

        editPass=(MaterialEditText) findViewById(R.id.editPass);
        editPhone=(MaterialEditText) findViewById(R.id.editPhone);

        btnsignIn=(Button) findViewById(R.id.btnSignin);

        //init firecase
         FirebaseDatabase database= FirebaseDatabase.getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app");
         DatabaseReference table_user= database.getReference("User");

        btnsignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInterner(getBaseContext())) {

                    final ProgressDialog mDialog = new ProgressDialog(Signin.this);
                    mDialog.setMessage("Please waiting...");
                    mDialog.show();
                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //user not exist
                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                                //get user
                                mDialog.dismiss();
                                User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                                user.setPhone(editPhone.getText().toString());
                                if (user.getPassword().equals(editPass.getText().toString())) {
                                    Intent intent = new Intent(Signin.this, Home.class);
                                    Common.currentUser = user;
                                    startActivity(intent);
                                    finish();

                                    table_user.removeEventListener(this);
                                } else {
                                    Toast.makeText(Signin.this, "Check Password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(Signin.this, "User not Exist", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                }else {
                    Toast.makeText(Signin.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });



            }
    }
