package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;

import java.util.List;

/**
 * DAO for {@link Team}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public interface TeamDao {

    @Query("SELECT * FROM teams" +
            " WHERE :id = team_id")
    Team getTeam(String id);

    @Query("SELECT * FROM teams")
    List<Team> getTeams();

    @Query("SELECT *" +
            " FROM teams as team" +
            " INNER JOIN roles as role" +
            " ON team.team_id = role.role_team_id" +
            " WHERE :userId = role.user_id")
    List<Team> myTeams(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TeamEntity> teams);

    @Delete
    void delete(TeamEntity user);
}
