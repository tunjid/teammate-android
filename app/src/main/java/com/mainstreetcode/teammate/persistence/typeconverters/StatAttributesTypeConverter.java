package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.enums.StatAttributes;
import com.mainstreetcode.teammate.rest.TeammateService;


public class StatAttributesTypeConverter {

    @TypeConverter
    public String toString(StatAttributes attributes) {
        return TeammateService.getGson().toJson(attributes);
    }

    @TypeConverter
    public StatAttributes fromJson(String json) {
        return TeammateService.getGson().fromJson(json, StatAttributes.class);
    }
}
