package com.it4medicine.mobilenurse.core.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

/**
 * 
 * Created by Vladimir Korshak on 08.08.15.
 */
@Table(name = "vkReportEvents")
public class vkReportEvent extends Model{

    @Expose
    @Column(name = "processName")
    private String processName;

    @Expose
    @Column(name = "actionName")
    private String actionName;

    @Expose
    @Column(name = "eventAction")
    private String eventAction;

    @Expose
    @Column(name = "eventTimestamp")
    private long eventTimestamp;

    public vkReportEvent(){
        super();
    }

    public String getProcessName() {
        return processName;
    }

    public vkReportEvent setProcessName(String processName) {
        this.processName = processName;
        return this;
    }

    public String getActionName() {
        return actionName;
    }

    public vkReportEvent setActionName(String actionName) {
        this.actionName = actionName;
        return this;
    }

    public String getEventAction() {
        return eventAction;
    }

    public vkReportEvent setEventAction(String eventAction) {
        this.eventAction = eventAction;
        return this;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public vkReportEvent setEventTimestamp(long eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
        return this;
    }
}
