package com.myappfood.appfood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
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

public class Signup extends AppCompatActivity {
    MaterialEditText editPhone,editPass,editName;
    Button btnSignup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editPass=(MaterialEditText) findViewById(R.id.editPass);
        editPhone=(MaterialEditText) findViewById(R.id.editPhone);
        editName=(MaterialEditText) findViewById(R.id.editName);
        btnSignup=(Button) findViewById(R.id.btnSignup);

        //init firecase
        FirebaseDatabase database= FirebaseDatabase.getInstance("https://appfood-9abc2-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference table_user= database.getReference("User");

        btnSignup.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInterner(getBaseContext())) {


                    final ProgressDialog mDialog = new ProgressDialog(Signup.this);
                    mDialog.setMessage("Please waiting...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                                mDialog.dismiss();
                                Toast.makeText(Signup.this, "Phone Number alreary register", Toast.LENGTH_SHORT).show();

                            } else {
                                mDialog.dismiss();
                                User user = new User(editName.getText().toString(), editPass.getText().toString());
                                table_user.child(editPhone.getText().toString()).setValue(user);
                                Toast.makeText(Signup.this, "Sign up successfull!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }else {
                    Toast.makeText(Signup.this, "Please check your connect!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}