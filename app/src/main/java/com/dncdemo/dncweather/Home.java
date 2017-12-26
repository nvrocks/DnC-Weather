package com.dncdemo.dncweather;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Home extends AppCompatActivity {

    private Button signin;
    private EditText username,password;
    private TextView signup;
    private DatabaseReference mDatabase;
    private ImageView imageView;
    ValueEventListener vel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        username = (EditText) findViewById(R.id.username);
        password = (EditText)findViewById(R.id.loginpassword);
        signin = (Button)findViewById(R.id.signin);
        signup = (TextView)findViewById(R.id.signup);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                vel = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int f = 0;
                        for(DataSnapshot userDetails : dataSnapshot.getChildren()) {
                            if(username.getText().toString().trim().length() == 0)
                            {
                                Toast.makeText(Home.this,"Enter Username",Toast.LENGTH_LONG).show();
                                f = 1;
                                break;
                            }
                            if(username.getText().toString().trim().equals(userDetails.child("username").getValue().toString()))
                            {
                                f = 1;
                                String passw = userDetails.child("password").getValue().toString();
                                Log.e("Pass",passw);
                                if(password.getText().toString().trim().length() == 0)
                                {
                                    Toast.makeText(Home.this,"Enter Password",Toast.LENGTH_LONG).show();
                                    break;
                                }
                                if(passw.equals(password.getText().toString().trim()))
                                {

                                    /*SQLiteDatabase data = openOrCreateDatabase("login", MODE_PRIVATE, null); //nobody other can access
                                    data.execSQL("create table if not exists student (regno varchar, password varchar);");
                                    data.execSQL("insert into student values ('" + regnum.getText().toString().trim() + "','" + password.getText().toString().trim() + "');");
                                    finish();
                                    Intent i = new Intent(.this,StudentProfile.class);
                                    i.putExtra("reg",regnum.getText().toString().trim());
                                    startActivity(i);*/
                                    Toast.makeText(Home.this,"Login Successful",Toast.LENGTH_LONG).show();
                                    Intent intent=new Intent(Home.this,DisplayWeather.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(Home.this,"Invalid Password",Toast.LENGTH_LONG).show();
                                    break;
                                }
                            }
                        }
                        if(f==0)
                        {
                            Toast.makeText(Home.this,"User Not Registered",Toast.LENGTH_LONG).show();
                        }
                        //mDatabase.removeEventListener(vel);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mDatabase.addListenerForSingleValueEvent(vel);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(Home.this,Signup.class);
                startActivity(i);
                finish();
                //finish();
            }
        });
    }
}
