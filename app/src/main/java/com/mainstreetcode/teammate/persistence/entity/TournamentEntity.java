package com.mainstreetcode.teammate.persistence.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.model.enums.TournamentStyle;
import com.mainstreetcode.teammate.model.enums.TournamentType;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static com.mainstreetcode.teammate.util.ModelUtils.parse;
import static com.mainstreetcode.teammate.util.ModelUtils.processString;


@Entity(tableName = "tournaments",
        foreignKeys = @ForeignKey(entity = TeamEntity.class, parentColumns = "team_id", childColumns = "tournament_host", onDelete = CASCADE)
)
public class TournamentEntity implements Parcelable {

    //private static final SimpleDateFormat timePrinter = new SimpleDateFormat("HH:mm", Locale.US);
//    refPath:
//    winner:

    @NonNull @PrimaryKey
    @ColumnInfo(name = "tournament_id") protected String id;
    @ColumnInfo(name = "tournament_image_url") protected String imageUrl;
    @ColumnInfo(name = "tournament_name") protected CharSequence name;
    @ColumnInfo(name = "tournament_description") protected CharSequence description;

    @ColumnInfo(name = "tournament_host") protected Team host;
    @ColumnInfo(name = "tournament_created") protected Date created;
    @ColumnInfo(name = "tournament_sport") protected Sport sport;
    @ColumnInfo(name = "tournament_type") protected TournamentType type;
    @ColumnInfo(name = "tournament_style") protected TournamentStyle style;
    @ColumnInfo(name = "tournament_winner") protected Competitor winner;

    @ColumnInfo(name = "tournament_num_legs") protected int numLegs;
    @ColumnInfo(name = "tournament_num_rounds") protected int numRounds;
    @ColumnInfo(name = "tournament_current_round") protected int currentRound;
    @ColumnInfo(name = "tournament_num_competitors") protected int numCompetitors;

    @ColumnInfo(name = "tournament_single_final") protected boolean singleFinal;

    public TournamentEntity(@NonNull String id, String imageUrl,
                            CharSequence name, CharSequence description,
                            Date created, Team host, Sport sport, TournamentType type, TournamentStyle style,
                            Competitor winner,
                            int numLegs, int numRounds, int currentRound, int numCompetitors,
                            boolean singleFinal) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.description = description;
        this.created = created;
        this.host = host;
        this.sport = sport;
        this.type = type;
        this.style = style;
        this.winner = winner;
        this.numLegs = numLegs;
        this.numRounds = numRounds;
        this.currentRound = currentRound;
        this.numCompetitors = numCompetitors;
        this.singleFinal = singleFinal;
    }

    protected TournamentEntity(Parcel in) {
        id = in.readString();
        imageUrl = in.readString();
        name = in.readString();
        description = in.readString();
        created = new Date(in.readLong());
        host = (Team) in.readValue(Team.class.getClassLoader());
        sport = Config.sportFromCode(in.readString());
        type = Config.tournamentTypeFromCode(in.readString());
        style = Config.tournamentStyleFromCode(in.readString());
        winner = Competitor.Util.fromParcel(in);
        numLegs = in.readInt();
        numRounds = in.readInt();
        currentRound = in.readInt();
        numCompetitors = in.readInt();
        singleFinal = in.readByte() != 0x00;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public CharSequence getName() {
        return processString(name);
    }

    public CharSequence getDescription() {
        return processString(description);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Team getHost() {
        return host;
    }

    public Date getCreated() {
        return created;
    }

    public Sport getSport() { return sport; }

    public TournamentType getType() { return type; }

    public TournamentStyle getStyle() { return style; }

    public boolean isSingleFinal() { return singleFinal; }

    public Competitor getWinner() { return winner; }

    public int getCurrentRound() { return currentRound; }

    public int getNumCompetitors() { return numCompetitors; }

    public boolean isRoundRobin() {
        return type.getCode().contains("obin");
    }

    public int getNumLegs() { return numLegs; }

    public int getNumRounds() { return numRounds; }

    public void setName(String name) {
        this.name = name;
    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setType(String type) {
        this.type = Config.tournamentTypeFromCode(type);
    }

    public void setStyle(String style) {
        this.style = Config.tournamentStyleFromCode(style);
    }

    protected void setNumLegs(String numLegs) {
        this.numLegs = parse(numLegs);
    }

    protected void setNumRounds(String numRounds) {
        this.numRounds = parse(numRounds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TournamentEntity)) return false;

        TournamentEntity event = (TournamentEntity) o;

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
        dest.writeString(imageUrl);
        dest.writeString(name.toString());
        dest.writeString(description.toString());
        dest.writeLong(created.getTime());
        dest.writeValue(host);
        dest.writeString(sport.getCode());
        dest.writeString(type.getCode());
        dest.writeString(style.getCode());
        Competitor.Util.writeToParcel(winner, dest);
        dest.writeInt(numLegs);
        dest.writeInt(numRounds);
        dest.writeInt(currentRound);
        dest.writeInt(numCompetitors);
        dest.writeByte((byte) (singleFinal ? 0x01 : 0x00));
    }

    public static final Creator<TournamentEntity> CREATOR = new Creator<TournamentEntity>() {
        @Override
        public TournamentEntity createFromParcel(Parcel in) {
            return new TournamentEntity(in);
        }

        @Override
        public TournamentEntity[] newArray(int size) {
            return new TournamentEntity[size];
        }
    };
}
