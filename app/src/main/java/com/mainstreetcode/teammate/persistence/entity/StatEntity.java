package com.mainstreetcode.teammate.persistence.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.StatAttribute;
import com.mainstreetcode.teammate.model.enums.StatAttributes;
import com.mainstreetcode.teammate.model.enums.StatType;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;
import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;
import static com.mainstreetcode.teammate.util.ModelUtils.parseFloat;


@Entity(tableName = "stats",
        foreignKeys = {
                @ForeignKey(entity = GameEntity.class, parentColumns = "game_id", childColumns = "stat_game", onDelete = CASCADE),
                @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "stat_team", onDelete = CASCADE),
                @ForeignKey(entity = UserEntity.class, parentColumns = "user_id", childColumns = "stat_user", onDelete = CASCADE),
        }
)
public class StatEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "stat_id") protected String id;

    @ColumnInfo(name = "stat_created") protected Date created;
    @ColumnInfo(name = "stat_type") protected StatType statType;
    @ColumnInfo(name = "stat_sport") protected Sport sport;
    @ColumnInfo(name = "stat_user") protected User user;
    @ColumnInfo(name = "stat_team") protected Team team;
    @ColumnInfo(name = "stat_game") protected Game game;
    @ColumnInfo(name = "stat_attributes") protected StatAttributes attributes;

    @ColumnInfo(name = "stat_value") protected int value;
    @ColumnInfo(name = "stat_time") protected float time;

    public StatEntity(@NonNull String id, Date created,
                      StatType statType, Sport sport, User user, Team team, Game game,
                      StatAttributes attributes, int value, float time) {
        this.id = id;
        this.created = created;
        this.statType = statType;
        this.sport = sport;
        this.user = user;
        this.team = team;
        this.game = game;
        this.value = value;
        this.time = time;
        this.attributes = attributes == null ? new StatAttributes() : attributes;
    }

    protected StatEntity(Parcel in) {
        id = in.readString();
        created = new Date(in.readLong());
        user = (User) in.readValue(User.class.getClassLoader());
        team = (Team) in.readValue(Team.class.getClassLoader());
        game = (Game) in.readValue(Game.class.getClassLoader());
        sport = Config.sportFromCode(in.readString());
        statType = sport.statTypeFromCode(in.readString());
        value = in.readInt();
        time = in.readFloat();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return EMPTY_STRING;
    }

    public Date getCreated() {
        return created;
    }

    public Team getTeam() {
        return team;
    }

    public Game getGame() {
        return game;
    }

    public User getUser() {
        return user;
    }

    public StatType getStatType() { return statType; }

    public Sport getSport() { return sport; }

    public StatAttributes getAttributes() { return attributes; }

    public int getValue() { return value; }

    public float getTime() { return time; }

    public boolean contains(StatAttribute attribute) {return attributes.contains(attribute);}

    protected void setStatType(String statType) {
        this.statType = sport.statTypeFromCode(statType);
        attributes.clear();
    }

    protected void setTime(String time) {
        this.time = parseFloat(time);
    }

    public void compoundAttribute(StatAttribute attribute) {
        if (attributes.contains(attribute)) attributes.remove(attribute);
        else attributes.add(attribute);
    }

    public boolean isHome() {
        Competitive home = game.home.entity;
        return user.equals(home) || team.equals(home);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StatEntity)) return false;

        StatEntity event = (StatEntity) o;

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
        dest.writeLong(created.getTime());
        dest.writeValue(user);
        dest.writeValue(team);
        dest.writeValue(game);
        dest.writeString(sport.getCode());
        dest.writeString(statType.getCode());
        dest.writeInt(value);
        dest.writeFloat(time);
    }

    public static final Creator<StatEntity> CREATOR = new Creator<StatEntity>() {
        @Override
        public StatEntity createFromParcel(Parcel in) {
            return new StatEntity(in);
        }

        @Override
        public StatEntity[] newArray(int size) {
            return new StatEntity[size];
        }
    };
}
