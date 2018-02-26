package com.mainstreetcode.teammates.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.entity.TeamEntity;


public class TeamTypeConverter {

    @TypeConverter
    public String toId(Team team) {
        return team.getId();
    }

    @TypeConverter
    public Team fromId(String id) {
        TeamEntity entity = AppDatabase.getInstance().teamDao().getAsEntity(id).blockingGet();
        return new Team(entity.getId(), entity.getName(), entity.getCity(),
                entity.getState(), entity.getZip(), entity.getImageUrl(),
                entity.getCreated(), entity.getLocation(), entity.getStorageUsed(), entity.getMaxStorage());
    }
}
