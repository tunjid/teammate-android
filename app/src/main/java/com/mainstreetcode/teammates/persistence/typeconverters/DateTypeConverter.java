package com.mainstreetcode.teammates.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;


public class DateTypeConverter {
    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
