package com.mainstreetcode.teammates.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamDao;


public class TeamTypeConverter {

    private final TeamDao teamDao = AppDatabase.getInstance().teamDao();

    @TypeConverter
    public String toId(Team team) {
        return team.getId();
    }

    @TypeConverter
    public Team fromId(String id) {
        return teamDao.getTeam(id);
    }
}
