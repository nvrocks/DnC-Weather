package com.dncdemo.dncweather;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class DisplayWeather extends AppCompatActivity implements OnMapReadyCallback {

    String locationId = "";
    Button submit;
    TextView temperature, isDay, weatherType;
    double lati = 0.0, longi = 0.0;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int PERMISSION_REQUEST_CODE = 200;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_weather);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkPermission()==false) {
            // TODO: Consider calling
            requestPermission();
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            System.out.println("loc "+location.getLatitude() + " : " + location.getLongitude());
                            lati=location.getLatitude();
                            longi=location.getLongitude();
                            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.map);
                            mapFragment.getMapAsync(DisplayWeather.this);


                        }
                    }});
        weatherType=(TextView)findViewById(R.id.weatherType);
        temperature=(TextView)findViewById(R.id.temperature);
        isDay=(TextView)findViewById(R.id.isDay);
        submit=(Button)findViewById(R.id.submit);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        final AutoCompleteTextView countrySearch = (AutoCompleteTextView) findViewById(R.id.input);
        final AutocompleteAdapter adapter = new AutocompleteAdapter(this,android.R.layout.simple_dropdown_item_1line);
        countrySearch.setThreshold(3);
        countrySearch.setAdapter(adapter);
        countrySearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocationDetails location = adapter.getItem(position);
                countrySearch.setText(location.getCity()+", "+location.getState()+", "+location.getCountry());
                locationId=location.getId();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DisplayWeather.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if(locationId!="")
                {
                    progressDialog=new ProgressDialog(DisplayWeather.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Loading");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                    new GetLocationDetailsAsynTask().execute(locationId);
                    //new GetLocationGeoAsynTask().execute(locationId);
                }
                else
                {
                    Toast.makeText(DisplayWeather.this,"Select a loaction",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    @Override
    public void onMapReady(GoogleMap map) {
        map.clear();
        map.addMarker(new MarkerOptions()
                .position(new LatLng(lati,longi))
                .title("Marker"));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati,longi),12));
       map.getUiSettings().setZoomGesturesEnabled(true);
        //map.setMinZoomPreference(50.0f);
    }

    public class GetLocationDetailsAsynTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://dataservice.accuweather.com/currentconditions/v1/"+params[0]+"?apikey=OXLJwdqPr0hoSdR9WLGFVOKdc9DEqiLG");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                if(connection.getResponseCode()==200) {
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    String jsonString = sb.toString();
                    return jsonString;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s!=null)
            {
                try {
                    String jsonString = s;
                    JSONArray jarray= new JSONArray(jsonString);
                    JSONObject job=jarray.getJSONObject(0);
                    String weather=job.getString("WeatherText");
                    String tempera=job.getJSONObject("Temperature").getJSONObject("Metric").getString("Value");
                    String isday=job.getString("IsDayTime");
                    System.out.println(weather+" : "+tempera+"*C, "+isday);
                    weatherType.setText(weather);
                    temperature.setText(tempera+ (char) 0x00B0);
                    isDay.setText(isday);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new GetLocationGeoAsynTask().execute(locationId);

            }
        }
    }
    public class GetLocationGeoAsynTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://dataservice.accuweather.com/locations/v1/"+params[0]+"?apikey=OXLJwdqPr0hoSdR9WLGFVOKdc9DEqiLG");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                if(connection.getResponseCode()==200) {
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    String jsonString = sb.toString();
                    return jsonString;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if(s!=null)
            {
                try {
                    String jsonString = s;
                    JSONObject job=new JSONObject(s);
                    lati=Float.parseFloat(job.getJSONObject("GeoPosition").getString("Latitude"));
                    longi=Float.parseFloat(job.getJSONObject("GeoPosition").getString("Longitude"));
                    System.out.println(lati+", "+longi);
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(DisplayWeather.this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
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
                        Toast.makeText(DisplayWeather.this, "Permission Granted, Now you can access location data.", Toast.LENGTH_LONG).show();
                    else {

                        Toast.makeText(DisplayWeather.this, "Permission Denied, You cannot access location data.", Toast.LENGTH_LONG).show();

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


    /*@Override
    public void onBackPressed() {
        super.onBackPressed();
        new AlertDialog.Builder(DisplayWeather.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Close Application")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }*/

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(DisplayWeather.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

}
