package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;

import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link User}
 */

@Dao
public abstract class MediaDao extends EntityDao<Media> {

    @Override
    protected String getTableName() {
        return "team_media";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insert(List<Media> roles);

    @Update(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void update(List<Media> roles);

    @Delete
    public abstract void delete(List<Media> roles);

    @Query("SELECT *" +
            " FROM team_media" +
            " WHERE :id = media_id")
    public abstract Maybe<Media> get(String id);

    @Query("SELECT *" +
            " FROM team_media" +
            " WHERE :team = media_team" +
            " AND media_created < :date" +
            " AND media_flagged = 0" +
            " ORDER BY media_created DESC" +
            " LIMIT :limit")
    public abstract Maybe<List<Media>> getTeamMedia(Team team, Date date, int limit);
}
