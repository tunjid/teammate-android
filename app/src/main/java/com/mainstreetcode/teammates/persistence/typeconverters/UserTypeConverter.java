package com.mainstreetcode.teammates.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;


public class UserTypeConverter {

    @TypeConverter
    public String toId(User user) {
        return user.getId();
    }

    @TypeConverter
    public User fromId(String id) {
        return AppDatabase.getInstance().userDao().get(id).blockingGet();
    }
}
