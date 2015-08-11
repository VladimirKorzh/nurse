package com.it4medicine.mobilenurse.core.actionreminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.it4medicine.mobilenurse.MobileNurseApplication;
import com.it4medicine.mobilenurse.R;
import com.it4medicine.mobilenurse.core.model.vkReminderEvent;

/**
 *
 * Created by root on 08.08.15.
 */
public class vkAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("id");
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        Log.d("vkAlarmReceiver", "Firing notification: " +title+ " "+text+" id: "+id);
        createNotification(context, Integer.valueOf(id), title, text);
    }

    public void createNotification(Context context,int id, String title, String text) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);

        Log.d(getClass().getSimpleName(), "passed id: " + id);
        Intent iOK = new Intent(context, vkNotificationHandlerService.class);
        iOK.putExtra("notification_id", id);
        iOK.setAction(vkReminderEvent.ACTION.TAKE.name());

        Intent iCancel = new Intent(context, vkNotificationHandlerService.class);
        iCancel.putExtra("notification_id", id);
        iCancel.setAction(vkReminderEvent.ACTION.DISMISS.name());

        Intent iSnooze = new Intent(context, vkNotificationHandlerService.class);
        iSnooze.putExtra("notification_id", id);
        iSnooze.setAction(vkReminderEvent.ACTION.SNOOZE.name());

        PendingIntent piOK     = PendingIntent.getService(context, 0, iOK, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piCancel = PendingIntent.getService(context, 0, iCancel, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent piSnooze = PendingIntent.getService(context, 0, iSnooze, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
//                .setContentTitle(title)
//                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
//                .addAction(R.drawable.ic_event_taken,   "Take Pill", pendingTargetIntent)
//                .addAction(R.drawable.ic_event_dismiss, "Dismiss", pendingTargetIntent)
//                .addAction(R.drawable.ic_event_snooze,  "Snooze",   pendingTargetIntent)
                .setPriority(Notification.PRIORITY_MAX)
//                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .setContent(remoteViews);


//        // The stack builder object will contain an artificial back stack for
//        // the started Activity.
//        // This ensures that navigating backward from the Activity leads out of
//        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(TestActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(targetIntent);

//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.btnNotificationAccept, piOK);
        remoteViews.setOnClickPendingIntent(R.id.btnNotificationDismiss, piCancel);

        remoteViews.setTextViewText(R.id.txtNotificationTitle, title);
        remoteViews.setTextViewText(R.id.txtNotificationText, text);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notificationCompat = notification.build();

        notificationCompat.defaults = Notification.DEFAULT_SOUND;
        notificationCompat.defaults |= Notification.DEFAULT_LIGHTS;

        if (MobileNurseApplication.sp.getBoolean("vibrate_on_notification", true))
            notificationCompat.defaults |= Notification.DEFAULT_VIBRATE;

        notificationCompat.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(id, notificationCompat);

        Notification wear_not = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        notificationManager.notify(id+1000, wear_not);
    }

}

