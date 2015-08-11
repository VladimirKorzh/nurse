package com.it4medicine.mobilenurse.core.actionreminder;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.activeandroid.query.Select;
import com.it4medicine.mobilenurse.core.model.vkActionCondition;
import com.it4medicine.mobilenurse.core.model.vkReminderEvent;

import java.util.HashMap;

/**
 * Created by root on 08.08.15.
 */
public class vkAlarmService extends IntentService {

    public static final String TAG = "mobile-nurse-as";
    public static final String CREATE = "CREATE";
    public static final String CANCEL = "CANCEL";

    private IntentFilter matcher;

    public vkAlarmService() {
        super(TAG);
        matcher = new IntentFilter();
        matcher.addAction(CREATE);
        matcher.addAction(CANCEL);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        String eventId = intent.getStringExtra("notificationId");

        if (matcher.matchAction(action)) {
            execute(action, eventId);
        }
    }

    private void execute(String action, String eventId) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        vkReminderEvent event = new Select().from(vkReminderEvent.class)
                                            .where("Id=?", eventId)
                                            .executeSingle();


        Log.d(TAG, "execute() action: "+action+" id: "+eventId+" "+event.getParent().getName());

        HashMap<vkActionCondition.K, String> actionConditions = event.getParent().getAssociatedConditions();

        Intent i = new Intent(this, vkAlarmReceiver.class);
        i.putExtra("id", eventId);
        i.putExtra("title", actionConditions.get(vkActionCondition.K.NOTIFICATION_TITLE));
        i.putExtra("text", actionConditions.get(vkActionCondition.K.NOTIFICATION_TEXT));

        long time = Long.valueOf(actionConditions.get(vkActionCondition.K.TRIGGER_TIME_EXACT));
        Log.d(TAG, "Current time: "+ System.currentTimeMillis());
        Log.d(TAG, "execute() at: " + time);

        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        if (CREATE.equals(action)) {
            am.set(AlarmManager.RTC_WAKEUP, time, pi);
        }
        if (CANCEL.equals(action))
            am.cancel(pi);
    }
}

