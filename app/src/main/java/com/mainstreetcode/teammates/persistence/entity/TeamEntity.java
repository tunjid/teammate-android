package com.mainstreetcode.teammates.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;


@Entity(tableName = "teams")
public class TeamEntity {

    @PrimaryKey @ColumnInfo(name = "team_id") protected String id;
    @ColumnInfo(name = "team_name") protected String name;
    @ColumnInfo(name = "team_city") protected String city;
    @ColumnInfo(name = "team_state") protected String state;
    @ColumnInfo(name = "team_zip") protected String zip;
    @ColumnInfo(name = "team_image_url") protected String imageUrl;

    public TeamEntity(String id, String name, String city, String state, String zip, String imageUrl) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.imageUrl = imageUrl;
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
    public String getImageUrl() {
        return imageUrl;
    }

    protected void setName(String name) {this.name = name; }

    protected void setCity(String city) {this.city = city; }

    protected void setState(String state) {this.state = state; }

    protected void setZip(String zip) {this.zip = zip; }

    @SuppressWarnings("WeakerAccess")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
