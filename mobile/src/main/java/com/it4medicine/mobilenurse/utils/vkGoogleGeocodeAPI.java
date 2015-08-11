package com.it4medicine.mobilenurse.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by vladimir on 7/4/2015.
 */
public class vkGoogleGeocodeAPI {


    public vkGoogleGeocodeAPI() {
    }

    public void setLocationResponseListener(onLocationResponseListener locationResponseListener) {
        this.locationResponseListener = locationResponseListener;
    }

    private onLocationResponseListener locationResponseListener = null;
    public interface onLocationResponseListener {
        public void onResponse(LatLng latLng);
        public void onResponse(GeocodeApiReponse response);
    }

    public class GeocodeApiReponse{
        public String Address1 = "";
        public String Address2 = "";
        public String City = "";
        public String State = "";
        public String Country = "";
        public String County = "";
        public String PIN = "";
        public String Place = "";
    }

    public void getLocationFromString(String place){
        new getLatLngFromNameTask().execute(place);
    }

    public void getLocationFromLanLng(LatLng latLng){ new getStringFromLatLngTask().execute(latLng);}

    private class getStringFromLatLngTask extends AsyncTask<LatLng, Void, GeocodeApiReponse> {

        @Override
        protected GeocodeApiReponse doInBackground(LatLng... strings) {
            //        https://maps.googleapis.com/maps/api/geocode/json?latlng=40.714224,-73.961452
            LatLng request = strings[0];

            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?latlng="+request.latitude+","+request.longitude+"&language=ru");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;

            String whatWeGot = null;
            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();


                whatWeGot = EntityUtils.toString(entity, "UTF-8");

            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(whatWeGot);
            } catch (JSONException e) {

                e.printStackTrace();
            }

            String place = null;
            GeocodeApiReponse geocodeApiReponse = new GeocodeApiReponse();
            try {
                place = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getString("formatted_address");

                JSONObject zero = ((JSONArray)jsonObject.get("results")).getJSONObject(0);
                JSONArray address_components = zero.getJSONArray("address_components");

                for (int i = 0; i < address_components.length(); i++) {
                    JSONObject zero2 = address_components.getJSONObject(i);
                    String long_name = zero2.getString("long_name");
                    JSONArray mtypes = zero2.getJSONArray("types");
                    String Type = mtypes.getString(0);

                    if (!TextUtils.isEmpty(long_name) || !long_name.equals(null) || long_name.length() > 0 || long_name != "") {
                        if (Type.equalsIgnoreCase("street_number")) {
                            geocodeApiReponse.Address1 = long_name + " ";
                        } else if (Type.equalsIgnoreCase("route")) {
                            geocodeApiReponse.Address1 = geocodeApiReponse.Address1 + long_name;
                        } else if (Type.equalsIgnoreCase("sublocality")) {
                            geocodeApiReponse.Address2 = long_name;
                        } else if (Type.equalsIgnoreCase("locality")) {
                            // Address2 = Address2 + long_name + ", ";
                            geocodeApiReponse.City = long_name;
                        } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                            geocodeApiReponse.County = long_name;
                        } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                            geocodeApiReponse.State = long_name;
                        } else if (Type.equalsIgnoreCase("country")) {
                            geocodeApiReponse.Country = long_name;
                        } else if (Type.equalsIgnoreCase("postal_code")) {
                            geocodeApiReponse.PIN = long_name;
                        }
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            geocodeApiReponse.Place = place;
            return geocodeApiReponse;
        }


        @Override
        protected void onPostExecute(GeocodeApiReponse geocodeApiReponse) {
            super.onPostExecute(geocodeApiReponse);
            if(locationResponseListener != null) {
                locationResponseListener.onResponse(geocodeApiReponse);
            }
        }
    }

    private class getLatLngFromNameTask extends AsyncTask<String, Void, LatLng> {

        @Override
        protected LatLng doInBackground(String... strings) {
            String request = strings[0];
            try {
                request = URLEncoder.encode(request, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpGet httpGet = new HttpGet("http://maps.google.com/maps/api/geocode/json?address="+request+"&ka&sensor=false");
            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            StringBuilder stringBuilder = new StringBuilder();

            try {
                response = client.execute(httpGet);
                HttpEntity entity = response.getEntity();
                InputStream stream = entity.getContent();
                int b;
                while ((b = stream.read()) != -1) {
                    stringBuilder.append((char) b);
                }
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(stringBuilder.toString());
            } catch (JSONException e) {

                e.printStackTrace();
            }

            Double lon = 0d;
            Double lat = 0d;

            try {
                lon = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lng");

                lat = ((JSONArray)jsonObject.get("results")).getJSONObject(0)
                        .getJSONObject("geometry").getJSONObject("location")
                        .getDouble("lat");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return new LatLng(lat,lon);
        }

        @Override
        protected void onPostExecute(LatLng latLng) {
            super.onPostExecute(latLng);
            if(locationResponseListener != null) locationResponseListener.onResponse(latLng);
        }
    }
}