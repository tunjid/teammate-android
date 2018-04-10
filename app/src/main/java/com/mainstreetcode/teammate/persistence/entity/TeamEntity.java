package com.mainstreetcode.teammate.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Sport;
import com.mainstreetcode.teammate.util.Logger;

import java.util.Date;


@Entity(tableName = "teams")
public class TeamEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "team_id") protected String id;
    @ColumnInfo(name = "team_name") protected String name;
    @ColumnInfo(name = "team_city") protected String city;
    @ColumnInfo(name = "team_state") protected String state;
    @ColumnInfo(name = "team_zip") protected String zip;
    @ColumnInfo(name = "team_description") protected String description;
    @ColumnInfo(name = "team_image_url") protected String imageUrl;

    @ColumnInfo(name = "team_sport") protected Sport sport;
    @ColumnInfo(name = "team_created") protected Date created;
    @ColumnInfo(name = "team_location") protected LatLng location;

    @ColumnInfo(name = "team_storage_used") protected long storageUsed;
    @ColumnInfo(name = "team_max_storage") protected long maxStorage;
    @ColumnInfo(name = "team_min_age") protected int minAge;
    @ColumnInfo(name = "team_max_age") protected int maxAge;

    public TeamEntity(@NonNull String id, String name, String city, String state,
                      String zip, String description, String imageUrl,
                      Date created, LatLng location, Sport sport,
                      long storageUsed, long maxStorage,
                      int minAge, int maxAge) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.sport = sport;
        this.description = description;
        this.imageUrl = imageUrl;
        this.created = created;
        this.location = location;
        this.storageUsed = storageUsed;
        this.maxStorage = maxStorage;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    protected TeamEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        city = in.readString();
        state = in.readString();
        zip = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        created = new Date(in.readLong());
        location = (LatLng) in.readValue(LatLng.class.getClassLoader());
        sport = Config.sportFromCode(in.readString());
        storageUsed = in.readLong();
        maxStorage = in.readLong();
        minAge = in.readInt();
        maxAge = in.readInt();
    }

    @NonNull
    public String getId() {return this.id;}

    public String getName() {return this.name;}

    public String getCity() {return this.city;}

    public Date getCreated() {
        return created;
    }

    public long getStorageUsed() {
        return storageUsed;
    }

    public long getMaxStorage() {
        return maxStorage;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getState() {
        return state;
    }

    public String getZip() {
        return zip;
    }

    public Sport getSport() {
        return sport;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    protected void setName(String name) {this.name = name; }

    protected void setCity(String city) {this.city = city; }

    protected void setState(String state) {this.state = state; }

    protected void setZip(String zip) {this.zip = zip; }

    @SuppressWarnings("WeakerAccess")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @SuppressWarnings("WeakerAccess")
    public void setSport(String code) {
        this.sport = Config.sportFromCode(code);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @SuppressWarnings("WeakerAccess")
    public void setMinAge(String minAge) {
        try { this.minAge = Integer.valueOf(minAge); }
        catch (Exception e) { Logger.log("Team", "Number Format Exception", e);}
    }

    @SuppressWarnings("WeakerAccess")
    public void setMaxAge(String maxAge) {
        try { this.minAge = Integer.valueOf(maxAge); }
        catch (Exception e) { Logger.log("Team", "Number Format Exception", e);}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamEntity)) return false;

        TeamEntity team = (TeamEntity) o;

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
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeLong(created.getTime());
        dest.writeValue(location);
        dest.writeString(sport.getCode());
        dest.writeLong(storageUsed);
        dest.writeLong(maxStorage);
        dest.writeInt(minAge);
        dest.writeInt(maxAge);
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
