package com.mainstreetcode.teammate.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.enums.Sport;

import java.util.Date;

import static com.mainstreetcode.teammate.util.ModelUtils.parse;
import static com.mainstreetcode.teammate.util.ModelUtils.processString;


@Entity(tableName = "teams")
public class TeamEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "team_id") protected String id;
    @ColumnInfo(name = "team_image_url") protected String imageUrl;
    @ColumnInfo(name = "team_screen_name") protected String screenName;
    @ColumnInfo(name = "team_city") protected String city;
    @ColumnInfo(name = "team_state") protected String state;
    @ColumnInfo(name = "team_zip") protected String zip;
    @ColumnInfo(name = "team_name") protected CharSequence name;
    @ColumnInfo(name = "team_description") protected CharSequence description;

    @ColumnInfo(name = "team_sport") protected Sport sport;
    @ColumnInfo(name = "team_created") protected Date created;
    @ColumnInfo(name = "team_location") protected LatLng location;

    @ColumnInfo(name = "team_storage_used") protected long storageUsed;
    @ColumnInfo(name = "team_max_storage") protected long maxStorage;
    @ColumnInfo(name = "team_min_age") protected int minAge;
    @ColumnInfo(name = "team_max_age") protected int maxAge;

    public TeamEntity(@NonNull String id, String imageUrl, String screenName,
                      String city, String state, String zip,
                      CharSequence name, CharSequence description,
                      Date created, LatLng location, Sport sport,
                      long storageUsed, long maxStorage,
                      int minAge, int maxAge) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.screenName = screenName;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.name = name;
        this.description = description;
        this.sport = sport;
        this.created = created;
        this.location = location;
        this.storageUsed = storageUsed;
        this.maxStorage = maxStorage;
        this.minAge = minAge;
        this.maxAge = maxAge;
    }

    protected TeamEntity(Parcel in) {
        id = in.readString();
        imageUrl = in.readString();
        screenName = in.readString();
        city = in.readString();
        state = in.readString();
        zip = in.readString();
        name = in.readString();
        description = in.readString();
        created = new Date(in.readLong());
        location = (LatLng) in.readValue(LatLng.class.getClassLoader());
        sport = Config.sportFromCode(in.readString());
        storageUsed = in.readLong();
        maxStorage = in.readLong();
        minAge = in.readInt();
        maxAge = in.readInt();
    }

    void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getId() {return this.id;}

    public CharSequence getName() {return processString(this.name);}

    public CharSequence getSportAndName() {return sport.appendEmoji(name);}

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

    public CharSequence getDescription() {
        return processString(description);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LatLng getLocation() {
        return location;
    }

    protected void setName(String name) {this.name = name; }

    protected void setScreenName(String screenName) { this.screenName = screenName; }

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
    public void setMinAge(String minAge) { this.minAge = parse(minAge); }

    @SuppressWarnings("WeakerAccess")
    public void setMaxAge(String maxAge) { this.maxAge = parse(maxAge); }

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
        dest.writeString(imageUrl);
        dest.writeString(screenName);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(zip);
        dest.writeString(name.toString());
        dest.writeString(description.toString());
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
