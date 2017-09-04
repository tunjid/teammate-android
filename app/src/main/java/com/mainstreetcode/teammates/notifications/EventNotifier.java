package com.mainstreetcode.teammates.notifications;


import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.repository.ModelRespository;


public class EventNotifier extends Notifier<Event> {

    private static EventNotifier INSTANCE;

    private EventNotifier() {

    }

    public static EventNotifier getInstance() {
        if (INSTANCE == null) INSTANCE = new EventNotifier();
        return INSTANCE;
    }

    @Override
    protected ModelRespository<Event> getRepository() {
        return EventRepository.getInstance();
    }
}
