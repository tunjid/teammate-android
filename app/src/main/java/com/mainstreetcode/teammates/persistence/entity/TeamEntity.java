package com.mainstreetcode.teammates.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;


@Entity(tableName = "teams")
public class TeamEntity implements Parcelable{

    @PrimaryKey @ColumnInfo(name = "team_id") protected String id;
    @ColumnInfo(name = "team_name") protected String name;
    @ColumnInfo(name = "team_city") protected String city;
    @ColumnInfo(name = "team_state") protected String state;
    @ColumnInfo(name = "team_zip") protected String zip;
    @ColumnInfo(name = "team_image_url") protected String imageUrl;

    @ColumnInfo(name = "team_created") protected Date created;
    @ColumnInfo(name = "team_last_seen") protected Date lastSeen;

    public TeamEntity(String id, String name, String city, String state, String zip, String imageUrl,
                      Date created, Date lastSeen) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.imageUrl = imageUrl;
        this.created = created;
        this.lastSeen = lastSeen;
    }

    protected TeamEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        city = in.readString();
        state = in.readString();
        zip = in.readString();
        imageUrl = in.readString();
        created = new Date(in.readLong());
        lastSeen = new Date(in.readLong());
    }

    public String getId() {return this.id;}

    public String getName() {return this.name;}

    public String getCity() {return this.city;}

    public Date getCreated() {
        return created;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamEntity)) return false;

        TeamEntity team = (TeamEntity) o;

        //return uid.equals(team.uid);
        return id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(zip);
        dest.writeString(imageUrl);
        dest.writeLong(created.getTime());
        dest.writeLong(lastSeen.getTime());
    }

    public static final Parcelable.Creator<TeamEntity> CREATOR = new Parcelable.Creator<TeamEntity>() {
        @Override
        public TeamEntity createFromParcel(Parcel in) {
            return new TeamEntity(in);
        }

        @Override
        public TeamEntity[] newArray(int size) {
            return new TeamEntity[size];
        }
    };
}
