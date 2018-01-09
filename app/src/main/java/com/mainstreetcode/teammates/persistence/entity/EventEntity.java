package com.mainstreetcode.teammates.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.arch.persistence.room.ForeignKey.CASCADE;


@Entity(tableName = "events",
        foreignKeys = @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "event_team", onDelete = CASCADE)
)
public class EventEntity implements Parcelable {

    public static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US);
    private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);

    @NonNull @PrimaryKey
    @ColumnInfo(name = "event_id") protected String id;
    @ColumnInfo(name = "event_name") protected String name;
    @ColumnInfo(name = "event_notes") protected String notes;
    @ColumnInfo(name = "event_image_url") protected String imageUrl;
    @ColumnInfo(name = "event_location_name") protected String locationName;

    @ColumnInfo(name = "event_team") protected Team team;
    @ColumnInfo(name = "event_start_date") protected Date startDate;
    @ColumnInfo(name = "event_end_date") protected Date endDate;
    @ColumnInfo(name = "event_location") protected LatLng location;

    public EventEntity(@NonNull String id, String name, String notes, String imageUrl, String locationName,
                       Date startDate, Date endDate, Team team, LatLng location) {
        this.id = id;
        this.name = name;
        this.notes = notes;
        this.imageUrl = imageUrl;
        this.locationName = locationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.team = team;
        this.location = location;
    }

    protected EventEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        notes = in.readString();
        imageUrl = in.readString();
        locationName = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        team = (Team) in.readValue(Team.class.getClassLoader());
        location = (LatLng) in.readValue(LatLng.class.getClassLoader());
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNotes() {
        return notes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLocationName() {
        return locationName;
    }

    public Team getTeam() {
        return team;
    }

    public LatLng getLocation() {
        return location;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getTime() {
        String time = prettyPrinter.format(startDate) + " - ";
        time += endsSameDay() ? timePrinter.format(endDate) : prettyPrinter.format(endDate);
        return time;
    }

    private boolean endsSameDay() {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setTime(startDate);
        end.setTime(endDate);
        return start.get(Calendar.YEAR) == end.get(Calendar.YEAR)
                && start.get(Calendar.MONTH) == end.get(Calendar.MONTH)
                && start.get(Calendar.DATE) == end.get(Calendar.DATE);
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void setNotes(String notes) {
        this.notes = notes;
    }

    protected void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    protected void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

//    public void setTeamId(String teamId) {
//        this.teamId = teamId;
//    }

    protected void setStartDate(String startDate) {
        this.startDate = ModelUtils.parseDate(startDate, prettyPrinter);
    }

    protected void setEndDate(String endDate) {
        this.endDate = ModelUtils.parseDate(endDate, prettyPrinter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventEntity)) return false;

        EventEntity event = (EventEntity) o;

        return id.equals(event.id);
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
        dest.writeString(notes);
        dest.writeString(imageUrl);
        dest.writeString(locationName);
        dest.writeLong(startDate.getTime());
        dest.writeLong(endDate.getTime());
        dest.writeValue(team);
        dest.writeValue(location);
    }

    public static final Parcelable.Creator<EventEntity> CREATOR = new Parcelable.Creator<EventEntity>() {
        @Override
        public EventEntity createFromParcel(Parcel in) {
            return new EventEntity(in);
        }

        @Override
        public EventEntity[] newArray(int size) {
            return new EventEntity[size];
        }
    };
}
