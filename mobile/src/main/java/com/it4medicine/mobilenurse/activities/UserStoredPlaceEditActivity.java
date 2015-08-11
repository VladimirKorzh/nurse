package com.it4medicine.mobilenurse.activities;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.it4medicine.mobilenurse.R;
import com.it4medicine.mobilenurse.core.model.vkUserStoredLocation;
import com.it4medicine.mobilenurse.utils.vkGoogleGeocodeAPI;

public class UserStoredPlaceEditActivity extends ActionBarActivity {


    private EditText edtName, edtAddr, edtLat, edtLon;
    vkUserStoredLocation location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stored_place_edit);

        Intent i = getIntent();
        long loc_id = i.getExtras().getLong("location_id");

        edtName = (EditText) findViewById(R.id.edtPlaceName);
        edtAddr = (EditText) findViewById(R.id.edtPlaceAddr);
        edtLat = (EditText)  findViewById(R.id.edtPlaceLat);
        edtLon = (EditText)  findViewById(R.id.edtPlaceLon);

        location = vkUserStoredLocation.load(vkUserStoredLocation.class,loc_id);

        edtName.setText(location.getName());
        edtAddr.setText(location.getAddress());
        edtLat.setText(String.valueOf(location.getLatitude()) );
        edtLon.setText(String.valueOf(location.getLongitude()) );

        Button btnFind = (Button) findViewById(R.id.btnPlaceFindAddrOnMap);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findAddrLatLon(edtAddr.getText().toString());
            }
        });

    }

    public void findAddrLatLon(String addr){
        vkGoogleGeocodeAPI googleGeocodeAPI = new vkGoogleGeocodeAPI();
        googleGeocodeAPI.setLocationResponseListener(new vkGoogleGeocodeAPI.onLocationResponseListener() {
            @Override
            public void onResponse(LatLng latLng) {
                edtLat.setText(String.valueOf(latLng.latitude));
                edtLon.setText(String.valueOf(latLng.longitude));
            }

            @Override
            public void onResponse(vkGoogleGeocodeAPI.GeocodeApiReponse response) {

            }
        });

        googleGeocodeAPI.getLocationFromString(addr);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_stored_place_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop(){
        super.onStop();
        location.setLatitude(Double.valueOf(edtLat.getText().toString()));
        location.setLongitude(Double.valueOf(edtLon.getText().toString()));
        location.save();
    }
}
