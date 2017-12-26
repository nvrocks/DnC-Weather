package com.dncdemo.dncweather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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


public class Signup extends AppCompatActivity {

    private static final String TAG = Signup.class.getSimpleName();
    private DatabaseReference mDatabase;
    private String userId;
    private Button register;
    private EditText password,email,name,confirmpassword,mobile,username;
    int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        userId = mDatabase.push().getKey();
        register   = (Button)findViewById(R.id.register);
        email     = (EditText)findViewById(R.id.email);
        name   = (EditText)findViewById(R.id.naam);
        username   = (EditText)findViewById(R.id.username);
        password   = (EditText)findViewById(R.id.password);
        confirmpassword = (EditText)findViewById(R.id.confirmpassword);
        mobile     = (EditText)findViewById(R.id.mobile);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String semail = email.getText().toString().trim();
                final String susername = username.getText().toString().trim();
                final String spass = password.getText().toString().trim();
                final String sname = name.getText().toString().trim();
                final String sconfirmpass = confirmpassword.getText().toString().trim();
                final String smobile  = mobile.getText().toString().trim();
                if(semail.length()==0 || susername.length()==0 || spass.length()==0 || sname.length()==0 || sconfirmpass.length()==0 || smobile.length()==0)
                {
                    Toast.makeText(Signup.this,"Fill all entries",Toast.LENGTH_LONG).show();
                }
                else if(!sconfirmpass.equals(spass))
                {
                    Toast.makeText(Signup.this,"Password not matched",Toast.LENGTH_LONG).show();
                }
                else
                {
                    if (TextUtils.isEmpty(userId)) {
                    }
                    else {
                        flag=0;
                        final DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference("users");
                        ValueEventListener vel1=new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for(DataSnapshot userDetails:dataSnapshot.getChildren())
                                {
                                    if(userDetails.child("username").getValue().toString().equals(username.getText().toString()))
                                    {
                                        flag=1;
                                        //Toast.makeText(Signup.this,"User already registered",Toast.LENGTH_LONG).show();
                                        break;
                                    }
                                }
                                if(flag==1)
                                {
                                    Toast.makeText(Signup.this,"Username already registered",Toast.LENGTH_LONG).show();
                                }
                                else
                                {
                                    final User user=new User();
                                    user.name=sname;
                                    user.username=susername;
                                    user.email=semail;
                                    user.mobile=smobile;
                                    user.password=spass;
                                    mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            User user1 = user;

                                            // Check for null
                                            if (user1 == null) {
                                                Log.e(TAG, "User data is null!");
                                                return;
                                            }
                                            final String userId = mDatabase.push().getKey();
                                            mDatabase.child(userId).setValue(user1);
                                            Log.e(TAG, "User data is changed!" + user1.name + ", " + user1.username);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError error) {
                                            // Failed to read value
                                            Log.e(TAG, "Failed to read user", error.toException());
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        mDatabase1.addListenerForSingleValueEvent(vel1);
                        //createUser(nam, years, s1, s2, mob, userId);
                    }
                }
            }
        });
    }

}
