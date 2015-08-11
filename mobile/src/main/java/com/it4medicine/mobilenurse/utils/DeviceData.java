package com.it4medicine.mobilenurse.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * Created by root on 10.08.15.
 */
public class DeviceData {

    @SerializedName("device_id")
    private String device_id;

    @SerializedName("device_name")
    private String device_name;

    @SerializedName("timestamp")
    private long timeStamp;

    public DeviceData(Context context){
        this.device_id=getDeviceUid(context);
        this.device_name= getDeviceName();
        this.timeStamp=System.currentTimeMillis();
    }

    public String toJson(){
        Gson gson = new Gson();
        return gson.toJson(DeviceData.this);
    }

    public String getDeviceUid(Context c) {
        return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }
}