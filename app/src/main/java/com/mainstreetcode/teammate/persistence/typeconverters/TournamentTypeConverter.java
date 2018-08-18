package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.persistence.AppDatabase;


public class TournamentTypeConverter {

    @TypeConverter
    public String toId(Tournament tournament) {
        return tournament.getId();
    }

    @TypeConverter
    public Tournament fromId(String id) {
        return AppDatabase.getInstance().tournamentDao().get(id)
                .onErrorReturn(error -> Tournament.empty(Team.empty())).blockingGet();
    }
}
