package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

/**
 * DAO for {@link Team}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public interface TeamDao {
    @Query("SELECT * FROM user WHERE primaryEmail LIKE :primaryEmail LIMIT 1")
    User findByEmail(String primaryEmail);

    @Insert
    void insert(Team user);

    @Delete
    void delete(Team user);
}
