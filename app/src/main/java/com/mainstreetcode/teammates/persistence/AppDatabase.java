package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
import com.mainstreetcode.teammates.persistence.typeconverters.DateTypeConverter;
import com.mainstreetcode.teammates.persistence.typeconverters.TeamTypeConverter;

/**
 * App Database
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Database(entities = {UserEntity.class, TeamEntity.class, EventEntity.class, Role.class}, version = 1)
@TypeConverters({DateTypeConverter.class, TeamTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract TeamDao teamDao();

    public abstract RoleDao roleDao();

    public abstract EventDao eventDao();

    public static AppDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(Application.getInstance(),
                    AppDatabase.class, "database-name").build();
        }
        return INSTANCE;
    }

}
