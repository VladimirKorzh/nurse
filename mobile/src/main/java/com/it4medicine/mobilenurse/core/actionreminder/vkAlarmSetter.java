package com.it4medicine.mobilenurse.core.actionreminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by root on 08.08.15.
 */
public class vkAlarmSetter extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, vkAlarmService.class);
        service.setAction(vkAlarmService.CREATE);
        context.startService(service);
    }

}
