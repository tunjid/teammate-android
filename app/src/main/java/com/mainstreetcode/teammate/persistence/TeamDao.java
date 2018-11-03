package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.persistence.entity.TeamEntity;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Team}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public abstract class TeamDao extends EntityDao<TeamEntity> {

    @Override
    protected String getTableName() {
        return "teams";
    }

    @Query("SELECT * FROM teams" +
            " WHERE :id = team_id")
    public abstract Maybe<Team> get(String id);

    @Query("SELECT * FROM teams")
    public abstract Maybe<List<Team>> getTeams();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<TeamEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<TeamEntity> teams);

    @Delete
    public abstract void delete(TeamEntity teamEntity);
}
