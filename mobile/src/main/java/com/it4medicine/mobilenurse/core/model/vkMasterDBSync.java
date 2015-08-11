package com.it4medicine.mobilenurse.core.model;

import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;
import com.it4medicine.mobilenurse.MobileNurseApplication;
import com.it4medicine.mobilenurse.core.network.packets.DefaultPayload;
import com.it4medicine.mobilenurse.utils.DeviceData;
import com.it4medicine.mobilenurse.utils.vkHelperFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 10.08.15.
 */
public class vkMasterDBSync {
    @Expose
    private List<vkReminderProcess> reminderProcesses;

    @Expose
    private List<vkReportEvent> reportEvents;

    @Expose
    private List<vkUserStoredLocation> storedLocations;

    public String locaDbToJson(){
        List<vkReminderProcess> rp_list = new Select().from(vkReminderProcess.class).execute();
        for (vkReminderProcess rp : rp_list)
            for (vkReminderAction ra : rp.items()){
                ra.getAssociatedConditions();
            }

        reminderProcesses = rp_list;
        reportEvents = new Select().from(vkReportEvent.class).execute();
        storedLocations = new Select().from(vkUserStoredLocation.class).execute();

        return MobileNurseApplication.gson.toJson(this);
    }

    public void clearLocalDb(){
        new Delete().from(vkReminderProcess.class).execute();
        new Delete().from(vkReportEvent.class).execute();
        new Delete().from(vkUserStoredLocation.class).execute();
    }

    public void localDbFromJson(String json){
        vkMasterDBSync n = MobileNurseApplication.gson.fromJson(json, vkMasterDBSync.class);

        clearLocalDb();

        reminderProcesses = n.reminderProcesses;
        reportEvents = n.reportEvents;
        storedLocations = n.storedLocations;

        for (vkReminderProcess rp : reminderProcesses){
            rp.save();
            for (vkReminderAction ra : rp.vkReminderActions){
                ra.setParent(rp);
                ra.save();
                for (vkActionCondition ac : ra.conditions){
                    ac.setParent(ra);
                    ac.save();
                }
            }
        }

        for(vkReportEvent re : reportEvents){
            re.save();
        }

        for (vkUserStoredLocation l : storedLocations) {
            l.save();
        }
    }

    private class masterServerSyncPayload{
        @Expose
        public DeviceData devicedata;

        @Expose
        public String request;
    }

    public class perfrormSyncWithMasterTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... params) {
            String json = MobileNurseApplication.masterDbSync.locaDbToJson();

            masterServerSyncPayload m = new masterServerSyncPayload();
            m.devicedata = MobileNurseApplication.deviceData;
            m.request = json;

            DefaultPayload p = new DefaultPayload();
            p.payload.put("type", "sync");
            p.payload.put("msg", MobileNurseApplication.gson.toJson(m));

            Log.d("test", p.toJson());

            vkMQ

            return null;
        }
    }

}
