package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.entity.UserEntity;

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

    @Query("SELECT * FROM users WHERE :id = user_id")
    public abstract Maybe<User> get(String id );

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<UserEntity> roles);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<UserEntity> roles);

    @Delete
    public abstract void delete(UserEntity user);
}
