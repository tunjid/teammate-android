package com.mainstreetcode.teammate.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Sport;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;


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
    @ColumnInfo(name = "stat_score") protected CharSequence name;

    @ColumnInfo(name = "stat_created") protected Date created;
    @ColumnInfo(name = "stat_sport") protected Sport sport;
    @ColumnInfo(name = "stat_user") protected User user;
    @ColumnInfo(name = "stat_team") protected Team team;
    @ColumnInfo(name = "stat_game") protected Game game;

    @ColumnInfo(name = "stat_value") protected int value;
    @ColumnInfo(name = "stat_time") protected float time;

    public StatEntity(@NonNull String id, CharSequence name,
                      Date created, Sport sport, User user, Team team, Game game,
                      int value, float time) {
        this.id = id;
        this.name = name;
        this.created = created;
        this.sport = sport;
        this.user = user;
        this.team = team;
        this.game = game;
        this.value = value;
        this.time = time;
    }

    protected StatEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        created = new Date(in.readLong());
        sport = Config.sportFromCode(in.readString());
        user = (User) in.readValue(User.class.getClassLoader());
        team = (Team) in.readValue(Team.class.getClassLoader());
        game = (Game) in.readValue(Game.class.getClassLoader());
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

    public CharSequence getName() { return name; }

    public Sport getSport() { return sport; }

    public int getValue() { return value; }

    public float getTime() { return time; }

    public boolean isHome() {
        String refPath = game.refPath;
        Competitive home = game.home.entity;
        return User.COMPETITOR_TYPE.equals(refPath)
                ? user.equals(home)
                : team.equals(home);
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
        dest.writeString(name.toString());
        dest.writeLong(created.getTime());
        dest.writeString(sport.getCode());
        dest.writeValue(user);
        dest.writeValue(team);
        dest.writeValue(game);
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
