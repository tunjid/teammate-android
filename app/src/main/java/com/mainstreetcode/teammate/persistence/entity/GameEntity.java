package com.mainstreetcode.teammate.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Sport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static com.mainstreetcode.teammate.util.ModelUtils.EMPTY_STRING;


@Entity(tableName = "games",
        foreignKeys = @ForeignKey(entity = TournamentEntity.class, parentColumns = "tournament_id", childColumns = "game_tournament", onDelete = CASCADE)
)
public class GameEntity implements Parcelable {

    private static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("dd MMM", Locale.US);

    @NonNull @PrimaryKey
    @ColumnInfo(name = "game_id") protected String id;
    @ColumnInfo(name = "game_ref_path") protected String refPath;
    @ColumnInfo(name = "game_score") protected String score;

    @ColumnInfo(name = "game_created") protected Date created;
    @ColumnInfo(name = "game_sport") protected Sport sport;
    @ColumnInfo(name = "game_event") protected Event event;
    @ColumnInfo(name = "game_tournament") protected Tournament tournament;
    @ColumnInfo(name = "game_home") protected Competitor home;
    @ColumnInfo(name = "game_away") protected Competitor away;
    @ColumnInfo(name = "game_winner") protected Competitor winner;

    @ColumnInfo(name = "game_leg") protected int leg;
    @ColumnInfo(name = "game_seed") protected int seed;
    @ColumnInfo(name = "game_round") protected int round;

    @ColumnInfo(name = "game_ended") protected boolean ended;
    @ColumnInfo(name = "game_can_draw") protected boolean canDraw;

    public GameEntity(@NonNull String id, String refPath, String score,
                      Date created, Sport sport, Event event, Tournament tournament,
                      Competitor home, Competitor away, Competitor winner,
                      int seed, int leg, int round,
                      boolean ended, boolean canDraw) {
        this.id = id;
        this.refPath = refPath;
        this.score = score;
        this.created = created;
        this.sport = sport;
        this.event = event;
        this.tournament = tournament;
        this.home = home;
        this.away = away;
        this.winner = winner;
        this.seed = seed;
        this.leg = leg;
        this.round = round;
        this.ended = ended;
        this.canDraw = canDraw;
    }

    protected GameEntity(Parcel in) {
        id = in.readString();
        refPath = in.readString();
        score = in.readString();
        created = new Date(in.readLong());
        sport = Config.sportFromCode(in.readString());
        event = (Event) in.readValue(Event.class.getClassLoader());
        tournament = (Tournament) in.readValue(Tournament.class.getClassLoader());
        home = (Competitor) in.readValue(Competitor.class.getClassLoader());
        away = (Competitor) in.readValue(Competitor.class.getClassLoader());
        winner = (Competitor) in.readValue(Competitor.class.getClassLoader());
        seed = in.readInt();
        leg = in.readInt();
        round = in.readInt();
        ended = in.readByte() != 0x00;
        canDraw = in.readByte() != 0x00;
    }

    public boolean hasValidRefType() {
        return User.COMPETITOR_TYPE.equals(refPath) || Team.COMPETITOR_TYPE.equals(refPath);
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return EMPTY_STRING;
    }

    public String getRefPath() {
        return refPath;
    }

    public String getDate() {
        return event.isEmpty() ? "" : prettyPrinter.format(event.startDate);
    }

    public Date getCreated() {
        return created;
    }

    public Competitor getWinner() {
        return winner;
    }

    public Competitor getHome() {
        return home;
    }

    public Competitor getAway() {
        return away;
    }

    public int getSeed() {
        return seed;
    }

    public String getScore() { return score; }

    public Sport getSport() {
        return sport;
    }

    public Event getEvent() {
        return event;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public int getLeg() {
        return leg;
    }

    public int getRound() {
        return round;
    }

    public boolean isEnded() {
        return ended;
    }

    public boolean isCanDraw() {
        return canDraw;
    }

    public boolean betweenUsers() {return User.COMPETITOR_TYPE.equals(refPath);}

    public boolean isCompeting(Competitive competitive) {return home.getEntity().equals(competitive) || away.getEntity().equals(competitive);}

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameEntity)) return false;

        GameEntity event = (GameEntity) o;

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
        dest.writeString(refPath);
        dest.writeString(score);
        dest.writeLong(created.getTime());
        dest.writeString(sport.getCode());
        dest.writeValue(event);
        dest.writeValue(tournament);
        dest.writeValue(home);
        dest.writeValue(away);
        dest.writeValue(winner);
        dest.writeInt(seed);
        dest.writeInt(leg);
        dest.writeInt(round);
        dest.writeByte((byte) (ended ? 0x01 : 0x00));
        dest.writeByte((byte) (canDraw ? 0x01 : 0x00));
    }

    public static final Creator<GameEntity> CREATOR = new Creator<GameEntity>() {
        @Override
        public GameEntity createFromParcel(Parcel in) {
            return new GameEntity(in);
        }

        @Override
        public GameEntity[] newArray(int size) {
            return new GameEntity[size];
        }
    };
}
