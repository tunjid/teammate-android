package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.repository.EventRepository;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends ViewModel {

    private final EventRepository repository;

    public EventViewModel(){
        repository = EventRepository.getInstance();
    }

    public Observable<List<Event>> getEvents(String userId) {
        return repository.getEvents(userId).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Event> updateEvent(final Event event) {
        return repository.updateEvent(event).observeOn(AndroidSchedulers.mainThread());
    }
}
