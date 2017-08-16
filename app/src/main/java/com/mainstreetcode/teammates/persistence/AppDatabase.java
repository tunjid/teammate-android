package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.TeamChat;
import com.mainstreetcode.teammates.persistence.entity.EventEntity;
import com.mainstreetcode.teammates.persistence.entity.JoinRequestEntity;
import com.mainstreetcode.teammates.persistence.entity.RoleEntity;
import com.mainstreetcode.teammates.persistence.entity.TeamChatRoomEntity;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;
import com.mainstreetcode.teammates.persistence.entity.UserEntity;
import com.mainstreetcode.teammates.persistence.typeconverters.DateTypeConverter;
import com.mainstreetcode.teammates.persistence.typeconverters.TeamTypeConverter;
import com.mainstreetcode.teammates.persistence.typeconverters.UserTypeConverter;

/**
 * App Database
 */

@Database(entities = {UserEntity.class, TeamEntity.class, EventEntity.class,
        RoleEntity.class, JoinRequestEntity.class, TeamChatRoomEntity.class,
        TeamChat.class}, version = 1)
@TypeConverters({DateTypeConverter.class, TeamTypeConverter.class, UserTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();

    public abstract TeamDao teamDao();

    public abstract RoleDao roleDao();

    public abstract EventDao eventDao();

    public abstract JoinRequestDao joinRequestDao();

    public abstract TeamChatDao teamChatDao();

    public abstract TeamChatRoomDao teamChatRoomDao();

    public static AppDatabase getInstance() {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(Application.getInstance(),
                    AppDatabase.class, "database-name").build();
        }
        return INSTANCE;
    }

}
