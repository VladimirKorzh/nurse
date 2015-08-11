package com.it4medicine.mobilenurse;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.activeandroid.ActiveAndroid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.it4medicine.mobilenurse.core.actionreminder.geo.GeofenceAPI;
import com.it4medicine.mobilenurse.core.model.vkMasterDBSync;
import com.it4medicine.mobilenurse.utils.DeviceData;

/**
 * Created by root on 08.08.15.
 */
public class MobileNurseApplication extends com.activeandroid.app.Application{

    private static final String TAG = "MobileNurseApplication";

    /**
     * Used to persist application state
     */
    public static SharedPreferences sp;
    public static GeofenceAPI geofenceAPI;
    public static Gson gson;
    public static vkMasterDBSync masterDbSync;

    public static DeviceData deviceData;

    @Override
    public void onCreate() {
        super.onCreate();

        ActiveAndroid.initialize(this);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        geofenceAPI = new GeofenceAPI(this);
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        masterDbSync = new vkMasterDBSync();

        deviceData = new DeviceData(this);
    }

}
