package com.mainstreetcode.teammates.persistence.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;



@Entity(tableName = "teams")
public class TeamEntity {

    @PrimaryKey
    protected String id;
    protected String name;
    protected String city;
    protected String state;
    protected String zip;
    protected String logoUrl;

    public TeamEntity(String id, String name, String city, String state, String zip, String logoUrl) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.logoUrl = logoUrl;
    }

    public String getId() {return this.id;}

    public String getName() {return this.name;}

    public String getCity() {return this.city;}

    @SuppressWarnings("unused")
    public String getState() {
        return state;
    }

    @SuppressWarnings("unused")
    public String getZip() {
        return zip;
    }

    @SuppressWarnings("unused")
    public String getLogoUrl() {
        return logoUrl;
    }

    protected void setName(String name) {this.name = name; }

    protected void setCity(String city) {this.city = city; }

    protected void setState(String state) {this.state = state; }

    protected void setZip(String zip) {this.zip = zip; }

    @SuppressWarnings("WeakerAccess")
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

}
