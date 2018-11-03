package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.persistence.AppDatabase;


public class TournamentTypeConverter {

    @TypeConverter
    public String toId(Tournament tournament) {
        return tournament.isEmpty() ? null : tournament.getId();
    }

    @TypeConverter
    public Tournament fromId(String id) {
        return AppDatabase.getInstance().tournamentDao().get(id)
                .onErrorReturn(error -> Tournament.empty(Team.empty())).blockingGet(Tournament.empty(Team.empty()));
    }
}
