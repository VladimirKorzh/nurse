package com.it4medicine.mobilenurse.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.activeandroid.query.Select;
import com.it4medicine.mobilenurse.MobileNurseApplication;
import com.it4medicine.mobilenurse.R;
import com.it4medicine.mobilenurse.core.model.vkActionCondition;
import com.it4medicine.mobilenurse.core.model.vkReminderAction;
import com.it4medicine.mobilenurse.core.model.vkReminderProcess;
import com.it4medicine.mobilenurse.core.model.vkMasterDBSync;
import com.it4medicine.mobilenurse.core.model.vkUserStoredLocation;
import com.it4medicine.mobilenurse.utils.vkHelperFunctions;

public class TestActivity extends FragmentActivity {

    public Activity activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        activity = this;

        Button btnCreateNotification  = (Button) findViewById(R.id.btnCreateNotification);
        Button btnStartPlacesActivity = (Button) findViewById(R.id.btnStartPlacesActivity);
        Button btnEditActionActivity  = (Button) findViewById(R.id.btnEditActionActivity);

        btnEditActionActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(activity, EditGeoActionActivity.class);
//                startActivity(intent);
                test_gson_serialization();
            }
        });


        btnStartPlacesActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, UserStoredPlacesActivity.class);
                startActivity(intent);
            }
        });

//        vkPlacePicker placePicker = new vkPlacePicker();
//        placePicker.setmListener(new vkPlacePicker.onDialogResultListener() {
//            @Override
//            public void onPlaceSelected(long id) {
//                Log.d("selected", String.valueOf(id));
//                vkUserStoredLocation location = vkUserStoredLocation.load(vkUserStoredLocation.class, id);
//                Log.d("selected", location.getName());
//            }
//        });
//        placePicker.show(getSupportFragmentManager(), "placePicker");


        btnCreateNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test_trigger_exact_time_sms_on_take();
                test_trigger_geo_delta_time();
            }
        });

        new vkHelperFunctions().CheckLocationSettings(this, MobileNurseApplication.geofenceAPI);
    }


    private void test_trigger_exact_time_sms_on_take(){
        vkReminderProcess rp;
        rp = new Select().from(vkReminderProcess.class).where("Name=?","testing").executeSingle();
        if (rp == null) {
            rp = new vkReminderProcess().setName("testing");
            rp.save();
        }

        vkReminderAction ra = new vkReminderAction().setName("Testing TRIGGER EXACT TIME with SMS ON_TAKE").setParent(rp);
        ra.save();

        new vkActionCondition().set(ra, vkActionCondition.K.TRIGGER_TYPE_TIME, true).save();
        new vkActionCondition().set(ra, vkActionCondition.K.TRIGGER_TIME_EXACT,
                                        String.valueOf(System.currentTimeMillis() + 5000)).save();
        new vkActionCondition().set(ra, vkActionCondition.K.WATCHER_NAME,"Andrey Zayac").save();
        new vkActionCondition().set(ra, vkActionCondition.K.WATCHER_PHONE,"+380676932088").save();
        new vkActionCondition().set(ra, vkActionCondition.K.EVENT_RULE_ON_CANCEL,
                                                    vkActionCondition.V.RULE_ACTION_SEND_WATCHER_SMS).save();
        new vkActionCondition().set(ra, vkActionCondition.K.NOTIFICATION_TITLE,"Testing notifications").save();
        new vkActionCondition().set(ra, vkActionCondition.K.NOTIFICATION_TEXT,"Sending SMS on CANCEL TAKE_PILL event").save();

        ra.generateEvents(activity);
    }

    private void test_trigger_geo_delta_time(){

        vkReminderProcess rp;
        rp = new Select().from(vkReminderProcess.class).where("Name=?","testing").executeSingle();
        if (rp == null) {
            rp = new vkReminderProcess().setName("testing");
            rp.save();
        }

        vkUserStoredLocation location = vkUserStoredLocation.load(vkUserStoredLocation.class, 1);
        Log.d("TRIGGER GEO DELTA", location.getName());
        vkReminderAction ra = new vkReminderAction().setName("Testing TRIGGER GEO DELTA").setParent(rp);
        ra.save();

        new vkActionCondition().set(ra, vkActionCondition.K.TRIGGER_TYPE_GEO, true).save();
        new vkActionCondition().set(ra, vkActionCondition.K.TRIGGER_GEO_LOCATION,location).save();
        new vkActionCondition().set(ra, vkActionCondition.K.TRIGGER_GEO_TIME_DELTA, 30000).save();
        new vkActionCondition().set(ra, vkActionCondition.K.NOTIFICATION_TITLE,"Testing notifications").save();
        new vkActionCondition().set(ra, vkActionCondition.K.NOTIFICATION_TEXT,"Trigger geo delta").save();

        ra.generateEvents(activity);
    }

    private void test_gson_serialization(){
        vkReminderProcess rp;
        rp = new Select().from(vkReminderProcess.class).where("Name=?","testing").executeSingle();
        if (rp == null) {
            rp = new vkReminderProcess().setName("testing");
            rp.save();
        }

        String json = MobileNurseApplication.masterDbSync.locaDbToJson();
        Log.d("GSON", json);
        MobileNurseApplication.masterDbSync.localDbFromJson(json);
        json = MobileNurseApplication.masterDbSync.locaDbToJson();
        Log.d("GSON", json);
    }

}
