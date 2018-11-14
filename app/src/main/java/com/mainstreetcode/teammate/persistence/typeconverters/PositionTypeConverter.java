package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.enums.Position;


public class PositionTypeConverter {

    @TypeConverter
    public String toCode(Position position) {
        return position.getCode();
    }

    @TypeConverter
    public Position fromCode(String code) {
        return Config.positionFromCode(code);
    }
}
