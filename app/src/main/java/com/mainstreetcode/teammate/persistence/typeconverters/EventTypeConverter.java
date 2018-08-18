package com.mainstreetcode.teammate.persistence.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.persistence.AppDatabase;


public class EventTypeConverter {

    @TypeConverter
    public String toId(Event event) {
        return event.getId();
    }

    @TypeConverter
    public Event fromId(String id) {
        return AppDatabase.getInstance().eventDao().get(id)
                .onErrorReturn(error -> Event.empty()).blockingGet();
    }
}
