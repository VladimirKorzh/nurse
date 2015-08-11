package com.it4medicine.mobilenurse.core.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by root on 08.08.15.
 */

@Table(name = "vkReminderProcesses")
public class vkReminderProcess extends Model{

    @Expose
    @Column(name = "Name")
    public String name;

    @Expose
    public List<vkReminderAction> vkReminderActions;

    // This method is optional, does not affect the foreign key creation.
    public List<vkReminderAction> items() {
        vkReminderActions = getMany(vkReminderAction.class, "parent");
        return vkReminderActions;
    }

    public vkReminderProcess() {
        super();
    }

    public String getName() {
        return name;
    }

    public vkReminderProcess setName(String name) {
        this.name = name;
        return this;
    }
}
