package com.it4medicine.mobilenurse.core.actionreminder;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.activeandroid.query.Select;
import com.it4medicine.mobilenurse.core.model.vkActionCondition;
import com.it4medicine.mobilenurse.core.model.vkReminderEvent;

import java.util.HashMap;


public class vkNotificationHandlerService extends IntentService {

    private IntentFilter matcher;

    public vkNotificationHandlerService(){
        super("mobile-nurse-handler-service");
        matcher = new IntentFilter();
        matcher.addAction(vkReminderEvent.ACTION.TAKE.name());
        matcher.addAction(vkReminderEvent.ACTION.DISMISS.name());
        matcher.addAction(vkReminderEvent.ACTION.SNOOZE.name());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        int eventId = intent.getIntExtra("notification_id", 0);
        Log.d(getClass().getSimpleName(), "action = " + action + " id: " + eventId);

        vkReminderEvent event = new Select().from(vkReminderEvent.class)
                .where("Id=?", eventId)
                .executeSingle();

        HashMap<vkActionCondition.K, String> actionConditions = event.getParent().getAssociatedConditions();

    // do we have any event rules associated with this fired event
        if (matcher.matchAction(action)) {
            event.handleUserAction(getApplicationContext(), action);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(eventId);
        }
    }
}