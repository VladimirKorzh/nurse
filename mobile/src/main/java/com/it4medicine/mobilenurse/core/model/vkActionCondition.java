package com.it4medicine.mobilenurse.core.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.google.gson.annotations.Expose;


/**
 *
 * Created by Vladimir Korshak on 09.08.15.
 */
@Table(name = "vkActionConditions")
public class vkActionCondition extends Model{

    public enum K {
        TRIGGER_TYPE_GEO,
        TRIGGER_TYPE_TIME,

        TRIGGER_GEO_LOCATION,
        TRIGGER_GEO_TIME_DELTA,

        TRIGGER_TIME_EXACT,
        TRIGGER_TIME_ACTIONDELTA,
        TRIGGER_TIME_DELTAVALUE,

        EVENT_RULE_ON_TAKE,
        EVENT_RULE_ON_CANCEL,
        EVENT_RULE_ON_SNOOZE,

        WATCHER_NAME,
        WATCHER_PHONE,
        NOTIFICATION_TITLE,
        NOTIFICATION_TEXT
    }

    public enum V {
        RULE_ACTION_SEND_WATCHER_SMS
    }


    @Expose
    @Column(name = "key")
    private K key;


    @Expose
    @Column(name = "value")
    private String value;

    @Column(name = "parent", onDelete = Column.ForeignKeyAction.CASCADE)
    private vkReminderAction parent;

    public vkActionCondition(){
        super();
    }

    public static void removeCondition(vkReminderAction parent, K condition){
        vkActionCondition actionCondition = new Select().from(vkActionCondition.class)
                                                        .where("parent=?",parent.getId())
                                                        .and("key=?",condition).executeSingle();
        if (actionCondition != null)
            actionCondition.delete();
    }

    public vkActionCondition set(vkReminderAction parent, K condition, Object value){
        setParent(parent);
        setKey(condition);

        switch (condition){
            case TRIGGER_TYPE_GEO:
            case TRIGGER_TYPE_TIME:
                this.setValue(Boolean.valueOf(String.valueOf(value)).toString());
                break;

            case TRIGGER_GEO_LOCATION:
                this.setValue( ((vkUserStoredLocation) value).getId().toString() );
                break;
            case TRIGGER_TIME_ACTIONDELTA:
                this.setValue( ((vkReminderAction)value).getId().toString() );
                break;

            case EVENT_RULE_ON_TAKE:
            case EVENT_RULE_ON_CANCEL:
            case EVENT_RULE_ON_SNOOZE:
                this.setValue( ((V)value).name() );
                break;

            case WATCHER_NAME:
            case WATCHER_PHONE:
            case NOTIFICATION_TITLE:
            case NOTIFICATION_TEXT:
            case TRIGGER_TIME_EXACT:
            case TRIGGER_GEO_TIME_DELTA:
            case TRIGGER_TIME_DELTAVALUE:
            default:
                this.setValue( String.valueOf(value) );
                break;
        }
        return this;
    }



    public K getKey() {
        return key;
    }

    private vkActionCondition setKey(K key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    private vkActionCondition setValue(String value) {
        this.value = value;
        return this;
    }

    public vkReminderAction getParent() {
        return parent;
    }

    public vkActionCondition setParent(vkReminderAction parent) {
        this.parent = parent;
        return this;
    }
}
