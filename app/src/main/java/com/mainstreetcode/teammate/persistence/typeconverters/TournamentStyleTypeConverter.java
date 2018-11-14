package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.enums.TournamentStyle;


public class TournamentStyleTypeConverter {

    @TypeConverter
    public String toCode(TournamentStyle style) {
        return style.getCode();
    }

    @TypeConverter
    public TournamentStyle fromCode(String code) {
        return Config.tournamentStyleFromCode(code);
    }
}
