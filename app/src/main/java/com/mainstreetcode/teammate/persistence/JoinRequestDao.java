package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;

import java.util.List;

/**
 * DAO for {@link com.mainstreetcode.teammate.model.JoinRequest}
 */

@Dao
public abstract class JoinRequestDao extends EntityDao<JoinRequestEntity> {

    @Override
    protected String getTableName() {
        return "join_requests";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<JoinRequestEntity> teams);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<JoinRequestEntity> teams);

    @Delete
    public abstract void delete(List<JoinRequestEntity> roles);

    @Query("DELETE FROM join_requests WHERE join_request_team = :teamId")
    public abstract void deleteByTeam(String teamId);
}
