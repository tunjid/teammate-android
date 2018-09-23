package com.mainstreetcode.teammate.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Visibility;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static com.mainstreetcode.teammate.util.ModelUtils.parse;
import static com.mainstreetcode.teammate.util.ModelUtils.processString;


@Entity(tableName = "events",
        foreignKeys = @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "event_team", onDelete = CASCADE)
)
public class EventEntity implements Parcelable {

    //private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);

    @NonNull @PrimaryKey
    @ColumnInfo(name = "event_id") protected String id;
    @ColumnInfo(name = "event_game_id") protected String gameId;
    @ColumnInfo(name = "event_image_url") protected String imageUrl;
    @ColumnInfo(name = "event_name") protected CharSequence name;
    @ColumnInfo(name = "event_notes") protected CharSequence notes;
    @ColumnInfo(name = "event_location_name") protected CharSequence locationName;

    @ColumnInfo(name = "event_team") protected Team team;
    @ColumnInfo(name = "event_start_date") protected Date startDate;
    @ColumnInfo(name = "event_end_date") protected Date endDate;
    @ColumnInfo(name = "event_location") protected LatLng location;
    @ColumnInfo(name = "event_visibility") protected Visibility visibility;

    @ColumnInfo(name = "event_spots") protected int spots;

    public EventEntity(@NonNull String id, String gameId, String imageUrl,
                       CharSequence name, CharSequence notes, CharSequence locationName,
                       Date startDate, Date endDate, Team team, LatLng location, Visibility visibility,
                       int spots) {
        this.id = id;
        this.gameId = gameId;
        this.imageUrl = imageUrl;
        this.name = name;
        this.notes = notes;
        this.visibility = visibility;
        this.locationName = locationName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.team = team;
        this.location = location;
        this.spots = spots;
    }

    protected EventEntity(Parcel in) {
        id = in.readString();
        gameId = in.readString();
        imageUrl = in.readString();
        name = in.readString();
        notes = in.readString();
        locationName = in.readString();
        startDate = new Date(in.readLong());
        endDate = new Date(in.readLong());
        team = (Team) in.readValue(Team.class.getClassLoader());
        location = (LatLng) in.readValue(LatLng.class.getClassLoader());
        visibility = Config.visibilityFromCode(in.readString());
        spots = in.readInt();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getGameId() { return gameId; }

    public CharSequence getName() {
        return processString(name);
    }

    public CharSequence getNotes() {
        return processString(notes);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public CharSequence getLocationName() {
        return processString(locationName);
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
        return ModelUtils.prettyPrinter.format(startDate);
    }

    public int getSpots() {
        return spots;
    }

    public boolean isPublic(){
        return visibility.isPublic();
    }

    public void setName(String name) {
        this.name = name;
    }

    protected void setNotes(String notes) {
        this.notes = notes;
    }

    public void setVisibility(String visibility) {
        this.visibility = Config.visibilityFromCode(visibility);
    }

    protected void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    protected void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    protected void setStartDate(String startDate) {
        this.startDate = ModelUtils.parseDate(startDate, ModelUtils.prettyPrinter);
    }

    protected void setEndDate(String endDate) {
        this.endDate = ModelUtils.parseDate(endDate, ModelUtils.prettyPrinter);
    }

    protected void setSpots(String spots) {
        this.spots = parse(spots);
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
        dest.writeString(gameId);
        dest.writeString(imageUrl);
        dest.writeString(name.toString());
        dest.writeString(notes.toString());
        dest.writeString(locationName.toString());
        dest.writeLong(startDate.getTime());
        dest.writeLong(endDate.getTime());
        dest.writeValue(team);
        dest.writeValue(location);
        dest.writeString(visibility.getCode());
        dest.writeInt(spots);
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
