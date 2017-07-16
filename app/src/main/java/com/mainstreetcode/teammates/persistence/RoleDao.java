package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.entity.RoleEntity;

import java.util.List;

/**
 * DAO for {@link User}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<RoleEntity> roles);

    @Delete
    void delete(List<RoleEntity> roles);
}
