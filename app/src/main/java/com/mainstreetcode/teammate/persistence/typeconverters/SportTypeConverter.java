package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Sport;


public class SportTypeConverter {

    @TypeConverter
    public String toCode(Sport sport) {
        return sport.getCode();
    }

    @TypeConverter
    public Sport fromCode(String code) {
        return Config.sportFromCode(code);
    }
}
