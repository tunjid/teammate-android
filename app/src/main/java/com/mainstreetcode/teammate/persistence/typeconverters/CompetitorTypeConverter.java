package com.mainstreetcode.teammate.persistence.typeconverters;

import androidx.room.TypeConverter;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.persistence.AppDatabase;


public class CompetitorTypeConverter {

    @TypeConverter
    public String toId(Competitor competitor) {
        return competitor.getId();
    }

    @TypeConverter
    public Competitor fromId(String id) {
        Competitor found = AppDatabase.getInstance().competitorDao().get(id).blockingGet();
        return found == null ? Competitor.empty() : found;
    }
}
