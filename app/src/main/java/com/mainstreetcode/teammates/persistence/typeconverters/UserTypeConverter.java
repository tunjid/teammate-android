package com.mainstreetcode.teammates.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.UserDao;


public class UserTypeConverter {

    private final UserDao userDao = AppDatabase.getInstance().userDao();

    @TypeConverter
    public String toId(User user) {
        return user.getId();
    }

    @TypeConverter
    public User fromId(String id) {
        return userDao.get(id).blockingGet();
    }
}
