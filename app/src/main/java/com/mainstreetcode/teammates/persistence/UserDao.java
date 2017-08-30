package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link User}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public abstract class UserDao extends EntityDao<UserEntity> {

    @Override
    protected String getTableName() {
        return "users";
    }

    @Query("SELECT * FROM users WHERE user_primary_email LIKE :primaryEmail LIMIT 1")
    public abstract Maybe<User> findByEmail(String primaryEmail );

    @Query("SELECT * FROM users WHERE :id = user_id")
    public abstract Maybe<User> get(String id );

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<UserEntity> roles);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<UserEntity> roles);

    @Delete
    public abstract void delete(UserEntity user);
}
