package com.dncdemo.dncweather;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

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

public class DisplayWeather extends AppCompatActivity {

    String locationId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_weather);

        final AutoCompleteTextView countrySearch = (AutoCompleteTextView) findViewById(R.id.input);
        final AutocompleteAdapter adapter = new AutocompleteAdapter(this,android.R.layout.simple_dropdown_item_1line);
        countrySearch.setAdapter(adapter);
        countrySearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LocationDetails location = adapter.getItem(position);
                countrySearch.setText(location.getCity()+", "+location.getState()+", "+location.getCountry());
                locationId=location.getId();
            }
        });
     //   String ur = "https://api.infermedica.com/v2/diagnosis";
        //new GetLocationAsynTask().execute(ur);
    }
    public class GetLocationAsynTask extends AsyncTask<String, Void, Boolean>{
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                URL url = new URL("http://dataservice.accuweather.com/locations/v1/cities/autocomplete?apikey=OXLJwdqPr0hoSdR9WLGFVOKdc9DEqiLG&q=varanas");
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
                    JSONArray jarray=new JSONArray(jsonString);
                    for(int i=0;i<jarray.length();i++)
                    {
                        JSONObject job=jarray.getJSONObject(i);
                        String city=job.getString("LocalizedName");
                        String state=job.getJSONObject("AdministrativeArea").getString("LocalizedName");
                        String country=job.getJSONObject("Country").getString("LocalizedName");
                        String id=job.getString("Key");
                        System.out.println(id+" : "+city+", "+state+", "+country);
                    }

                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }
    }
}
