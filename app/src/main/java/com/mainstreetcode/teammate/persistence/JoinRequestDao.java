package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.text.TextUtils;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammate.util.Logger;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

import static com.mainstreetcode.teammate.BuildConfig.DEV;

/**
 * DAO for {@link com.mainstreetcode.teammate.model.JoinRequest}
 */

@Dao
public abstract class JoinRequestDao extends EntityDao<JoinRequestEntity> {

    private static final String MULTI_DELETION_STATEMENT = "DELETE FROM join_requests WHERE join_request_team = '%1$s' AND join_request_user IN (%2$s)";
    private static final String COMMA_DELIMITER = ", ";

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

    @Query("DELETE FROM join_requests WHERE join_request_user = :userId AND join_request_team = :teamId")
    public abstract void deleteUsers(String userId, String teamId);

    @Query("DELETE FROM join_requests WHERE join_request_user IN :userIds AND join_request_team = :teamId")
    public void deleteRequestsFromTeam(String teamId, String... userIds) {
        for (int i = 0; i < userIds.length; i++) userIds[i] = "'" + userIds[i] + "'";

        String formattedIds = TextUtils.join(COMMA_DELIMITER, userIds);
        String sql = String.format(MULTI_DELETION_STATEMENT, teamId, formattedIds);

        int deleted = AppDatabase.getInstance().compileStatement(sql).executeUpdateDelete();
        if (DEV) Logger.log(getTableName(), "Deleted " + deleted + " rows");
    }

    @Query("SELECT * FROM join_requests as request" +
            " WHERE :teamId = join_request_team" +
            " AND join_request_created < :date" +
            " ORDER BY join_request_created DESC" +
            " LIMIT 40")
    public abstract Maybe<List<JoinRequest>> getRequests(String teamId, Date date);
}
