package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.entity.TeamEntity;


public class TeamTypeConverter {

    @TypeConverter
    public String toId(Team team) {
        return team.getId();
    }

    @TypeConverter
    public Team fromId(String id) {
        TeamEntity entity = AppDatabase.getInstance().teamDao().getAsEntity(id).blockingGet();
        return new Team(entity.getId(), entity.getName(), entity.getCity(), entity.getState(),
                entity.getZip(), entity.getDescription(), entity.getImageUrl(),
                entity.getCreated(), entity.getLocation(), entity.getSport(),
                entity.getStorageUsed(), entity.getMaxStorage(), entity.getMinAge(), entity.getMaxAge());
    }
}
