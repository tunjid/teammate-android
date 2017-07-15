package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;

import java.util.List;

/**
 * DAO for {@link User}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE user_primary_email LIKE :primaryEmail LIMIT 1")
    User findByEmail(String primaryEmail );

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<UserEntity> users);

    @Delete
    void delete(UserEntity user);
}
