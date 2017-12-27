package com.dncdemo.dncweather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class Home extends AppCompatActivity {

    private Button signin;
    private EditText username,password;
    private TextView signup;
    private DatabaseReference mDatabase;
    private ImageView imageView;
    ValueEventListener vel;
    private static final int PERMISSION_REQUEST_CODE = 200;
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
                                    //Toast.makeText(Home.this,"Login Successful",Toast.LENGTH_LONG).show();
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
                //finish();
            }
        });

        if (checkPermission()==false) {
            // TODO: Consider calling
            requestPermission();
            //Manifest.permission.ACCESS_FINE_LOCATION= String.valueOf(PackageManager.PERMISSION_GRANTED);
        }
    }
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted)
                        Toast.makeText(Home.this, "Permission Granted, Now you can access location data.", Toast.LENGTH_LONG).show();
                    else {

                        Toast.makeText(Home.this, "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                                showMessageOKCancel("You need to allow access to the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }


                break;
        }
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(Home.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
