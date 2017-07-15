package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;

import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;

/**
 * App Database
 * <p>
 * Created by Shemanigans on 6/12/17.
 */

@Database(entities = {User.class, TeamEntity.class, Role.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TeamDao teamDao();
    public abstract RoleDao roleDao();

    public static AppDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(Application.getInstance(),
                    AppDatabase.class, "database-name").build();
        }
        return INSTANCE;
    }
}
