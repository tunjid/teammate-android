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
import static com.mainstreetcode.teammate.util.ModelUtils.parse;


@Entity(tableName = "games",
        foreignKeys = @ForeignKey(entity = TournamentEntity.class, parentColumns = "tournament_id", childColumns = "game_tournament", onDelete = CASCADE)
)
public class GameEntity implements Parcelable {

    private static final SimpleDateFormat prettyPrinter = new SimpleDateFormat("dd MMM", Locale.US);

    @NonNull @PrimaryKey
    @ColumnInfo(name = "game_id") protected String id;
    @ColumnInfo(name = "game_name") protected String name;
    @ColumnInfo(name = "game_ref_path") protected String refPath;
    @ColumnInfo(name = "game_score") protected String score;
    @ColumnInfo(name = "game_match_up") protected String matchUp;
    @ColumnInfo(name = "game_home_entity") protected String homeEntityId;
    @ColumnInfo(name = "game_away_entity") protected String awayEntityId;
    @ColumnInfo(name = "game_winner_entity") protected String winnerEntityId;

    @ColumnInfo(name = "game_created") protected Date created;
    @ColumnInfo(name = "game_sport") protected Sport sport;
    @ColumnInfo(name = "game_referee") protected User referee;
    @ColumnInfo(name = "game_host") protected Team host;
    @ColumnInfo(name = "game_event") protected Event event;
    @ColumnInfo(name = "game_tournament") protected Tournament tournament;
    @ColumnInfo(name = "game_home") protected Competitor home;
    @ColumnInfo(name = "game_away") protected Competitor away;
    @ColumnInfo(name = "game_winner") protected Competitor winner;

    @ColumnInfo(name = "game_leg") protected int leg;
    @ColumnInfo(name = "game_seed") protected int seed;
    @ColumnInfo(name = "game_round") protected int round;
    @ColumnInfo(name = "game_home_score") protected int homeScore;
    @ColumnInfo(name = "game_away_score") protected int awayScore;

    @ColumnInfo(name = "game_ended") protected boolean ended;
    @ColumnInfo(name = "game_can_draw") protected boolean canDraw;

    public GameEntity(@NonNull String id, String name, String refPath, String score, String matchUp,
                      String homeEntityId, String awayEntityId, String winnerEntityId,
                      Date created, Sport sport, User referee, Team host, Event event, Tournament tournament,
                      Competitor home, Competitor away, Competitor winner,
                      int seed, int leg, int round, int homeScore, int awayScore,
                      boolean ended, boolean canDraw) {
        this.id = id;
        this.name = name;
        this.refPath = refPath;
        this.score = score;
        this.matchUp = matchUp;
        this.homeEntityId = homeEntityId;
        this.awayEntityId = awayEntityId;
        this.winnerEntityId = winnerEntityId;
        this.created = created;
        this.sport = sport;
        this.referee = referee;
        this.host = host;
        this.event = event;
        this.tournament = tournament;
        this.home = home;
        this.away = away;
        this.winner = winner;
        this.seed = seed;
        this.leg = leg;
        this.round = round;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.ended = ended;
        this.canDraw = canDraw;
    }

    protected GameEntity(Parcel in) {
        id = in.readString();
        name = in.readString();
        refPath = in.readString();
        score = in.readString();
        matchUp = in.readString();
        homeEntityId = in.readString();
        awayEntityId = in.readString();
        winnerEntityId = in.readString();
        created = new Date(in.readLong());
        sport = Config.sportFromCode(in.readString());
        referee = (User) in.readValue(User.class.getClassLoader());
        host = (Team) in.readValue(Team.class.getClassLoader());
        event = (Event) in.readValue(Event.class.getClassLoader());
        tournament = (Tournament) in.readValue(Tournament.class.getClassLoader());
        home = (Competitor) in.readValue(Competitor.class.getClassLoader());
        away = (Competitor) in.readValue(Competitor.class.getClassLoader());
        winner = (Competitor) in.readValue(Competitor.class.getClassLoader());
        seed = in.readInt();
        leg = in.readInt();
        round = in.readInt();
        homeScore = in.readInt();
        awayScore = in.readInt();
        ended = in.readByte() != 0x00;
        canDraw = in.readByte() != 0x00;
    }

    @NonNull
    public String getId() { return id; }

    public String getName() { return name; }

    public String getImageUrl() { return EMPTY_STRING; }

    public String getRefPath() { return refPath; }

    public String getDate() { return event.isEmpty() ? "" : prettyPrinter.format(event.startDate); }

    public String getMatchUp() { return matchUp; }

    public String getHomeEntityId() { return homeEntityId; }

    public String getAwayEntityId() { return awayEntityId; }

    public String getWinnerEntityId() { return winnerEntityId; }

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

    public String getScore() { return score; }

    public Sport getSport() {
        return sport;
    }

    public User getReferee() {
        return referee;
    }

    public Team getHost() { return host; }

    public Event getEvent() { return event; }

    public Tournament getTournament() {
        return tournament;
    }

    public int getLeg() {
        return leg;
    }

    public int getSeed() {
        return seed;
    }

    public int getRound() {
        return round;
    }

    public int getHomeScore() { return homeScore; }

    public int getAwayScore() { return awayScore; }

    public boolean isEnded() {
        return ended;
    }

    public boolean isCanDraw() {
        return canDraw;
    }

    public boolean betweenUsers() {return User.COMPETITOR_TYPE.equals(refPath);}

    public boolean isCompeting(Competitive competitive) {return home.getEntity().equals(competitive) || away.getEntity().equals(competitive);}

    public boolean competitorsNotAccepted() { return !home.accepted || !away.accepted; }

    public boolean competitorsDeclined() { return home.declined || away.declined; }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    @SuppressWarnings("WeakerAccess")
    public void setHomeScore(String homeScore) { this.homeScore = parse(homeScore); }

    @SuppressWarnings("WeakerAccess")
    public void setAwayScore(String awayScore) { this.awayScore = parse(awayScore); }

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
        dest.writeString(name);
        dest.writeString(refPath);
        dest.writeString(score);
        dest.writeString(matchUp);
        dest.writeString(homeEntityId);
        dest.writeString(awayEntityId);
        dest.writeString(winnerEntityId);
        dest.writeLong(created.getTime());
        dest.writeString(sport.getCode());
        dest.writeValue(referee);
        dest.writeValue(host);
        dest.writeValue(event);
        dest.writeValue(tournament);
        dest.writeValue(home);
        dest.writeValue(away);
        dest.writeValue(winner);
        dest.writeInt(seed);
        dest.writeInt(leg);
        dest.writeInt(round);
        dest.writeInt(homeScore);
        dest.writeInt(awayScore);
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
