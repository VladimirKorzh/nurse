package com.it4medicine.mobilenurse.core.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;


/**
 * Created by root on 08.08.15.
 */

@Table(name = "vkUserStoredLocations")
public class vkUserStoredLocation extends Model{

    @Expose
    @Column(name = "name")
    private String name;

    @Expose
    @Column(name = "addr")
    private String address;

    @Expose
    @Column(name = "lat")
    private double latitude;

    @Expose
    @Column(name = "lon")
    private double longitude;

    public vkUserStoredLocation() {
        super();
    }

    public String getName() {
        return name;
    }

    public vkUserStoredLocation setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public vkUserStoredLocation setAddress(String address) {
        this.address = address;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public vkUserStoredLocation setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public vkUserStoredLocation setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
}
