package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.persistence.AppDatabase;


public class TeamTypeConverter {

    @TypeConverter
    public String toId(Team team) {
        return team.getId();
    }

    @TypeConverter
    public Team fromId(String id) {
        return AppDatabase.getInstance().teamDao().get(id)
                .onErrorReturn(error -> Team.empty()).blockingGet();
    }
}
