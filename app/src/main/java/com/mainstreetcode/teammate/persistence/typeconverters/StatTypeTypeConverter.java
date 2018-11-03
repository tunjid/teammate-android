package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.enums.StatType;


public class StatTypeTypeConverter {

    @TypeConverter
    public String toCode(StatType type) {
        return type.getCode();
    }

    @TypeConverter
    public StatType fromCode(String code) {
        return Config.statTypeFromCode(code);
    }
}
