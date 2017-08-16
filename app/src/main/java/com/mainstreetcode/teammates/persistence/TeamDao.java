package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.persistence.typeconverters.EntityDao;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link Team}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public abstract class TeamDao extends EntityDao<TeamEntity> {

    @Query("SELECT * FROM teams" +
            " WHERE :id = team_id")
    public abstract Maybe<Team> get(String id);

    @Query("SELECT * FROM teams")
    public abstract Maybe<List<Team>> getTeams();

    @Query("SELECT *" +
            " FROM teams as team" +
            " INNER JOIN roles as role" +
            " ON team.team_id = role.role_team_id" +
            " WHERE :userId = role.user_id")
    public abstract Maybe<List<Team>> myTeams(String userId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<TeamEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<TeamEntity> teams);

    @Delete
    public abstract void delete(TeamEntity teamEntity);
}
