package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;

import com.mainstreetcode.teammates.persistence.entity.JoinRequestEntity;

import java.util.List;

/**
 * DAO for {@link com.mainstreetcode.teammates.model.JoinRequest}
 */

@Dao
public interface JoinRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<JoinRequestEntity> roles);

    @Delete
    void delete(List<JoinRequestEntity> roles);
}
