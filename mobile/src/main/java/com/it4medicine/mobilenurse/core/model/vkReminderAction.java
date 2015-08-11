package com.it4medicine.mobilenurse.core.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.android.gms.location.Geofence;
import com.google.gson.annotations.Expose;
import com.it4medicine.mobilenurse.MobileNurseApplication;
import com.it4medicine.mobilenurse.core.actionreminder.geo.Constants;
import com.it4medicine.mobilenurse.core.actionreminder.vkAlarmService;

import java.util.HashMap;
import java.util.List;

/**
 * Created by root on 08.08.15.
 */
@Table(name = "vkReminderActions")
public class vkReminderAction extends Model {
    @Expose
    @Column(name = "Name")
    private String name;

    @Column(name = "parent", onDelete = Column.ForeignKeyAction.CASCADE)
    private vkReminderProcess parent;

    @Expose
    public List<vkReminderEvent> events;

    @Expose
    public List<vkActionCondition> conditions;


    public List<vkReminderEvent> getAssociatedEvents() {
        events = getMany(vkReminderEvent.class, "parent");
        return events;
    }

    public HashMap<vkActionCondition.K, String> getAssociatedConditions(){
        HashMap<vkActionCondition.K, String> kv = new HashMap<>();
        conditions = getMany(vkActionCondition.class, "parent");
        for (vkActionCondition condition : conditions){
            kv.put(condition.getKey(), condition.getValue());
        }

        return kv;

    }

    public vkReminderProcess getParent() {
        return parent;
    }

    public vkReminderAction setParent(vkReminderProcess parent) {
        this.parent = parent;
        return this;
    }

    public String getName() {
        return name;
    }

    public vkReminderAction setName(String name) {
        this.name = name;
        return this;
    }

    public void generateEvents(Context context){

        final String TAG = "Action->generateNextEvent()";

        // remove any pending events for this action
        for (vkReminderEvent e : getAssociatedEvents()){

            // remove scheduled alarms
            Intent remove = new Intent(context, vkAlarmService.class);
            remove.putExtra("notificationId", String.valueOf(e.getId().intValue()));
            remove.setAction(vkAlarmService.CANCEL);
            context.startService(remove);

            // remove from database
            e.delete();
        }

        // get all conditions
        HashMap<vkActionCondition.K, String> actionConditions = getAssociatedConditions();

        vkReminderEvent event = new vkReminderEvent().setParent(this);
        event.save();

        // our event is based on reaching a specific geo location
        if (actionConditions.containsKey(vkActionCondition.K.TRIGGER_TYPE_GEO)){
            Log.d(TAG, "generated geo event");
            vkUserStoredLocation location = vkUserStoredLocation.load(vkUserStoredLocation.class,
                                        Long.valueOf(actionConditions.get(vkActionCondition.K.TRIGGER_GEO_LOCATION)));

            MobileNurseApplication.geofenceAPI.mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this geofence.
                    .setRequestId(event.getId().toString())
                // Set the circular region of this geofence.
                    .setCircularRegion(location.getLatitude(), location.getLongitude(), Constants.GEOFENCE_RADIUS_IN_METERS)
                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                // Set the transition types of interest. Alerts are only generated for these
                // transition.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                // Create the geofence.
                    .build());
            MobileNurseApplication.geofenceAPI.addGeofences();
        }

        // our event is based on reaching a specific point in time
        if (actionConditions.containsKey(vkActionCondition.K.TRIGGER_TYPE_TIME)){
            Log.d(TAG, "generated time event");
            Intent create = new Intent(context, vkAlarmService.class);
            create.putExtra("notificationId", String.valueOf(event.getId().intValue()));
            create.setAction(vkAlarmService.CREATE);
            context.startService(create);
        }

        Log.d(TAG, "new event action name: " + event.getParent().getName());
        Log.d(TAG, "new event id: " + event.getId().toString());
    }

    public vkReminderAction() {
        super();
    }
}
