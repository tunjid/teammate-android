package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.enums.Visibility;


public class VisibilityTypeConverter {

    @TypeConverter
    public String toCode(Visibility visibility) {
        return visibility.getCode();
    }

    @TypeConverter
    public Visibility fromCode(String code) {
        return Config.visibilityFromCode(code);
    }
}
