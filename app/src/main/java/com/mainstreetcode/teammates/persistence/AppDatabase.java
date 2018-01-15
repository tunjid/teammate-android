package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.support.v4.util.Pair;
import android.util.Log;

import com.mainstreetcode.teammates.App;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Chat;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammates.persistence.entity.RoleEntity;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
import com.mainstreetcode.teammates.persistence.typeconverters.DateTypeConverter;
import com.mainstreetcode.teammates.persistence.typeconverters.LatLngTypeConverter;
import com.mainstreetcode.teammates.persistence.typeconverters.TeamTypeConverter;
import com.mainstreetcode.teammates.persistence.typeconverters.UserTypeConverter;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

import static com.mainstreetcode.teammates.BuildConfig.DEBUG;

/**
 * App Database
 */

@Database(entities = {UserEntity.class, TeamEntity.class, EventEntity.class,
        RoleEntity.class, JoinRequestEntity.class, Chat.class, Media.class}, version = 1)

@TypeConverters({LatLngTypeConverter.class, DateTypeConverter.class,
        TeamTypeConverter.class, UserTypeConverter.class})

public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(App.getInstance(),
                    AppDatabase.class, "database-name").build();
        }
        return INSTANCE;
    }

    public abstract UserDao userDao();

    public abstract TeamDao teamDao();

    public abstract RoleDao roleDao();

    public abstract EventDao eventDao();

    public abstract MediaDao mediaDao();

    public abstract JoinRequestDao joinRequestDao();

    public abstract ChatDao teamChatDao();

    public DeviceDao deviceDao() {return new DeviceDao();}

    public Single<List<Pair<String, Integer>>> clearTables() {
        final List<Single<Pair<String, Integer>>> singles = new ArrayList<>();
        final List<Pair<String, Integer>> collector = new ArrayList<>();

        singles.add(clearTable(teamChatDao()));
        singles.add(clearTable(joinRequestDao()));
        singles.add(clearTable(eventDao()));
        singles.add(clearTable(mediaDao()));
        singles.add(clearTable(roleDao()));
        singles.add(clearTable(teamDao()));
        singles.add(clearTable(userDao()));
        singles.add(clearTable(deviceDao()));

        return Single.concat(singles).collectInto(collector, List::add);
    }

    private Single<Pair<String, Integer>> clearTable(EntityDao<?> entityDao) {
        final String tableName = entityDao.getTableName();

        return entityDao.deleteAll()
                .map(rowsDeleted -> new Pair<>(tableName, rowsDeleted))
                .onErrorResumeNext(throwable -> {
                    if (DEBUG) Log.e(TAG, "Error clearing table: " + tableName, throwable);
                    return Single.just(new Pair<>(tableName, 0));
                });
    }
}
