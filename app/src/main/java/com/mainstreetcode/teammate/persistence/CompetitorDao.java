package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.persistence.entity.CompetitorEntity;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Event}
 */

@Dao
public abstract class CompetitorDao extends EntityDao<CompetitorEntity> {

    @Override
    protected String getTableName() {
        return "competitors";
    }

    @Query("SELECT * FROM competitors" +
            " WHERE :tournamentId = competitor_tournament" +
            " ORDER BY competitor_created DESC" +
            " LIMIT 40")
    public abstract Maybe<List<Competitor>> getCompetitors(String tournamentId);

    @Query("SELECT * FROM competitors" +
            " WHERE :id = competitor_id")
    public abstract Maybe<Competitor> get(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<CompetitorEntity> tournaments);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<CompetitorEntity> tournaments);

    @Delete
    public abstract void delete(CompetitorEntity tournament);
}
