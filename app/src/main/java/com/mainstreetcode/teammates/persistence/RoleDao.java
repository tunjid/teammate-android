package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;

import java.util.List;

/**
 * DAO for {@link User}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public interface RoleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Role> roles);

    @Delete
    void delete(List<Role> roles);
}
