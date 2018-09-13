package com.mainstreetcode.teammate.persistence.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.EmptyCompetitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;

import java.util.Date;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "competitors",
        foreignKeys = {
                @ForeignKey(entity = TournamentEntity.class, parentColumns = "tournament_id", childColumns = "competitor_tournament_id", onDelete = CASCADE),
        }
)
public class CompetitorEntity implements Parcelable {

    @NonNull @PrimaryKey
    @ColumnInfo(name = "competitor_id") protected String id;
    @ColumnInfo(name = "competitor_ref_path") protected String refPath;
    @ColumnInfo(name = "competitor_tournament_id") protected String tournamentId;
    @ColumnInfo(name = "competitor_entity_id") protected Competitive entity;
    @ColumnInfo(name = "competitor_created") protected Date created;
    @ColumnInfo(name = "competitor_seed") protected int seed;

    public CompetitorEntity(@NonNull String id, String refPath, String tournamentId, Competitive entity, Date created, int seed) {
        this.id = id;
        this.refPath = refPath;
        this.tournamentId = tournamentId;
        this.entity = entity;
        this.created = created;
        this.seed = seed;
    }

    protected CompetitorEntity(Parcel in) {
        id = in.readString();
        refPath = in.readString();
        tournamentId = in.readString();
        entity = fromParcel(refPath, in);
        created = new Date(in.readLong());
        seed = in.readInt();
    }

    public String getId() { return id; }

    public String getRefPath() { return refPath; }

    public String getSeedText() {return seed > -1 ? String.valueOf(seed + 1) : "";}

    public String getTournamentId() { return tournamentId; }

    public Competitive getEntity() { return entity; }

    public Date getCreated() { return created; }

    public int getSeed() { return seed; }

    public void setSeed(int seed) { this.seed = seed; }

    private static Competitive fromParcel(String refPath, Parcel in) {
        switch (refPath) {
            case User.COMPETITOR_TYPE:
                return (User) in.readValue(User.class.getClassLoader());
            case Team.COMPETITOR_TYPE:
                return (Team) in.readValue(Team.class.getClassLoader());
            default:
                return (EmptyCompetitor) in.readValue(EmptyCompetitor.class.getClassLoader());
        }
    }

    private static void writeToParcel(Competitive competitive, Parcel dest) {
        dest.writeValue(competitive);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompetitorEntity)) return false;

        CompetitorEntity that = (CompetitorEntity) o;

        return id.equals(that.id);
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
        dest.writeString(tournamentId);
        writeToParcel(entity, dest);
        dest.writeLong(created.getTime());
        dest.writeInt(seed);
    }

    public static final Creator<CompetitorEntity> CREATOR = new Creator<CompetitorEntity>() {
        @Override
        public CompetitorEntity createFromParcel(Parcel in) {
            return new CompetitorEntity(in);
        }

        @Override
        public CompetitorEntity[] newArray(int size) {
            return new CompetitorEntity[size];
        }
    };
}
