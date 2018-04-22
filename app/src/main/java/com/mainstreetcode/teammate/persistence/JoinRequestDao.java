package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

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

    @Query("DELETE FROM join_requests WHERE join_request_user = :userId")
    public abstract void deleteUser(String userId);

    @Query("SELECT * FROM join_requests as request" +
            " WHERE :teamId = join_request_team" +
            " AND join_request_created < :date" +
            " ORDER BY join_request_created DESC" +
            " LIMIT 40")
    public abstract Maybe<List<JoinRequest>> getRequests(String teamId, Date date);
}
