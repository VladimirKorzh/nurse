package com.it4medicine.mobilenurse.core.model;

import android.content.Context;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;
import com.it4medicine.mobilenurse.MobileNurseApplication;
import com.it4medicine.mobilenurse.utils.vkHelperFunctions;

import java.util.HashMap;

/**
 * Created by root on 08.08.15.
 */

@Table(name = "vkReminderEvents")
public class vkReminderEvent extends Model {

    public enum ACTION {
        TAKE, DISMISS, SNOOZE
    }

    @Column(name = "parent", onDelete = Column.ForeignKeyAction.CASCADE)
    private vkReminderAction parent;

    public vkReminderEvent() {
        super();

    }

    public vkReminderAction getParent() {
        return parent;
    }

    public vkReminderEvent setParent(vkReminderAction parent) {
        this.parent = parent;
        return this;
    }

    public void handleUserAction(Context context, String action) {
        HashMap<vkActionCondition.K, String> kv = this.getParent().getAssociatedConditions();

        new vkReportEvent().setProcessName(this.getParent().getParent().getName())
                            .setActionName(this.getParent().getName())
                            .setEventAction(action)
                            .setEventTimestamp(System.currentTimeMillis()).save();

        if (vkReminderEvent.ACTION.TAKE.name().equals(action) &&
                kv.containsKey(vkActionCondition.K.EVENT_RULE_ON_TAKE)) {
            if (kv.get(vkActionCondition.K.EVENT_RULE_ON_TAKE)
                    .equals(vkActionCondition.V.RULE_ACTION_SEND_WATCHER_SMS.name())) {
                vkHelperFunctions.sendSMS(context,
                        kv.get(vkActionCondition.K.WATCHER_PHONE),
                        kv.get(vkActionCondition.K.NOTIFICATION_TEXT));

            }
        }
        if (vkReminderEvent.ACTION.DISMISS.name().equals(action) &&
                kv.containsKey(vkActionCondition.K.EVENT_RULE_ON_CANCEL)) {
            if (kv.get(vkActionCondition.K.EVENT_RULE_ON_CANCEL)
                    .equals(vkActionCondition.V.RULE_ACTION_SEND_WATCHER_SMS.name())) {
                vkHelperFunctions.sendSMS(context,
                        kv.get(vkActionCondition.K.WATCHER_PHONE),
                        kv.get(vkActionCondition.K.NOTIFICATION_TEXT));
            }
        }
        if (vkReminderEvent.ACTION.SNOOZE.name().equals(action) &&
                kv.containsKey(vkActionCondition.K.EVENT_RULE_ON_SNOOZE)) {

        }
    }
}
