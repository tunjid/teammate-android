package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.persistence.AppDatabase;


public class GameTypeConverter {

    @TypeConverter
    public String toId(Game game) {
        return game.isEmpty() ? null : game.getId();
    }

    @TypeConverter
    public Game fromId(String id) {
        return AppDatabase.getInstance().gameDao().get(id).blockingGet(Game.empty(Team.empty()));
    }
}
