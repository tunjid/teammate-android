package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Team;

import java.util.List;

/**
 * DAO for {@link Team}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public interface TeamDao {
    @Query("SELECT * FROM teams")
    List<Team> getTeams();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Team> teams);

    @Delete
    void delete(Team user);
}
