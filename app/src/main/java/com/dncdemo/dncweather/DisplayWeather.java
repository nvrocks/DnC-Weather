package com.dncdemo.dncweather;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

public class DisplayWeather extends AppCompatActivity implements OnMapReadyCallback {

    String locationId="";
    Button submit;
    TextView temperature,isDay,weatherType;
    float lati=0.0f,longi=0.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_weather);

        weatherType=(TextView)findViewById(R.id.weatherType);
        temperature=(TextView)findViewById(R.id.temperature);
        isDay=(TextView)findViewById(R.id.isDay);
        submit=(Button)findViewById(R.id.submit);
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
                if(locationId!="")
                {
                    new GetLocationDetailsAsynTask().execute(locationId);
                    new GetLocationGeoAsynTask().execute(locationId);
                }
                else
                {
                    Toast.makeText(DisplayWeather.this,"Select a loaction",Toast.LENGTH_SHORT).show();
                }
            }
        });
        lati=28.643f;
        longi=77.118f;
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(DisplayWeather.this);

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
                    temperature.setText(tempera);
                    isDay.setText(isday);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


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
}
