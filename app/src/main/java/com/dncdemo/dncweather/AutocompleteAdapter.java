package com.dncdemo.dncweather;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by neera on 12/26/2017.
 */

public class AutocompleteAdapter extends ArrayAdapter implements Filterable {
    private ArrayList<LocationDetails> mLocation;
    private String LocationDetail_URL ="http://dataservice.accuweather.com/locations/v1/cities/autocomplete?apikey=OXLJwdqPr0hoSdR9WLGFVOKdc9DEqiLG&q=";
    public AutocompleteAdapter(Context context, int resource) {
        super(context, resource);
        mLocation = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mLocation.size();
    }

    @Override
    public LocationDetails getItem(int position) {
        return mLocation.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    try{
                        //get data from the web
                        String term = constraint.toString();
                        mLocation = new DownloadLocationDetail().execute(term).get();
                    }catch (Exception e){
                        Log.d("HUS","EXCEPTION "+e);
                    }
                    filterResults.values = mLocation;
                    filterResults.count = mLocation.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.auto_complete_layout,parent,false);

        //get LocationDetail
        LocationDetails location = mLocation.get(position);

        TextView LocationDetailName = (TextView) view.findViewById(R.id.countryName);
        LocationDetailName.setText(location.getCity()+", "+location.getState()+", "+location.getCountry());
        return view;
    }

    //download mLocation list
    private class DownloadLocationDetail extends AsyncTask<String, Void, ArrayList> {

        @Override
        protected ArrayList doInBackground(String... params) {
            try {
                //Create a new LocationDetail SEARCH url Ex "search.php?term=india"
                String NEW_URL = LocationDetail_URL + URLEncoder.encode(params[0],"UTF-8");
                Log.d("HUS", "JSON RESPONSE URL " + NEW_URL);

                URL url = new URL(NEW_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }

                //parse JSON and store it in the list
                String jsonString =  sb.toString();
                ArrayList locationDetailList = new ArrayList<>();
                JSONArray jarray=new JSONArray(jsonString);
                for(int i=0;i<jarray.length();i++)
                {
                    JSONObject job=jarray.getJSONObject(i);
                    String city=job.getString("LocalizedName");
                    String state=job.getJSONObject("AdministrativeArea").getString("LocalizedName");
                    String country=job.getJSONObject("Country").getString("LocalizedName");
                    String id=job.getString("Key");
                    System.out.println(id+" : "+city+", "+state+", "+country);
                    LocationDetails location=new LocationDetails();
                    location.setCity(city);
                    location.setState(state);
                    location.setCountry(country);
                    locationDetailList.add(location);

                }

                //return the LocationDetailList
                return locationDetailList;

            } catch (Exception e) {
                Log.d("HUS", "EXCEPTION " + e);
                return null;
            }
        }
    }
}