package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Observable;

public class EventRepository {

    private static EventRepository ourInstance;

    private final TeammateApi api;

    private EventRepository() {
        api = TeammateService.getApiInstance();
    }

    public static EventRepository getInstance() {
        if (ourInstance == null) ourInstance = new EventRepository();
        return ourInstance;
    }

    public Observable<List<Event>> getEvents() {
        return api.getEvents();
    }
}
