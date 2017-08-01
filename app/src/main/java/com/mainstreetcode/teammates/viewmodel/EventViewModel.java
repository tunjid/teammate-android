package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.repository.EventRepository;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends ViewModel {

    private final EventRepository repository;

    public EventViewModel(){
        repository = EventRepository.getInstance();
    }

    public Flowable<List<Event>> getEvents(String userId) {
        return repository.getEvents(userId);
    }

    public Flowable<Event> getEvent(Event event) {
        return repository.get(event);
    }

    public Single<Event> updateEvent(final Event event) {
        return repository.createOrUpdate(event);
    }

    public Single<Event> rsvpEvent(final Event event, boolean attending) {
        return repository.rsvpEvent(event, attending);
    }

    public Single<Event> delete(final Event event) {
        return repository.delete(event);
    }
}
