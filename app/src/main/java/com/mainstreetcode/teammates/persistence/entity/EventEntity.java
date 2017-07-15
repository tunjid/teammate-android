package com.mainstreetcode.teammates.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import com.mainstreetcode.teammates.model.Team;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


@Entity(tableName = "events",
        foreignKeys = @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "team_id")
)
public class EventEntity implements Parcelable {

    public static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.US);
    protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);

    @PrimaryKey
    @ColumnInfo(name = "event_id") protected String id;
    @ColumnInfo(name = "event_name") protected String name;
    @ColumnInfo(name = "event_notes") protected String notes;
    @ColumnInfo(name = "event_image_url") protected String imageUrl;
    @ColumnInfo(name = "event_start_date") protected Date startDate;
    @ColumnInfo(name = "event_end_date") protected Date endDate;

    @Embedded
    protected Team team;

    public EventEntity(String id, String name, String notes, String imageUrl, Date startDate, Date endDate, Team team) {
        this.id = id;
        this.name = name;
        this.notes = notes;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.team = team;
    }

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

    public Team getTeam() {
        return team;
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

    protected static Date parseDate(String date) {
        return parseDate(date, dateFormatter);
    }

    public static Date parseDate(String date, SimpleDateFormat formatter) {
        try {
            return formatter.parse(date);
        }
        catch (ParseException e) {
            return new Date();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void setNotes(String notes) {
        this.notes = notes;
    }

    protected void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    protected void setStartDate(String startDate) {
        this.startDate = parseDate(startDate, prettyPrinter);
    }

    protected void setEndDate(String endDate) {
        this.endDate = parseDate(endDate, prettyPrinter);
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    protected EventEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        notes = in.readString();
        imageUrl = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        team = (Team) in.readValue(Team.class.getClassLoader());
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
        dest.writeLong(startDate != null ? startDate.getTime() : -1L);
        dest.writeLong(endDate != null ? endDate.getTime() : -1L);
        dest.writeValue(team);
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
