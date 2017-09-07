package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;

import java.util.List;

import io.reactivex.Maybe;

/**
 * DAO for {@link User}
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Dao
public abstract class MediaDao extends EntityDao<Media> {

    @Override
    protected String getTableName() {
        return "team_media";
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract void insert(List<Media> roles);

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
            " WHERE :team = media_team")
    public abstract Maybe<List<Media>> getTeamMedia(Team team);
}
