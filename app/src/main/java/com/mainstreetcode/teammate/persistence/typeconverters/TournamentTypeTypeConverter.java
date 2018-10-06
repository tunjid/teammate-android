package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.enums.TournamentType;


public class TournamentTypeTypeConverter {

    @TypeConverter
    public String toCode(TournamentType type) {
        return type.getCode();
    }

    @TypeConverter
    public TournamentType fromCode(String code) {
        return Config.tournamentTypeFromCode(code);
    }
}
