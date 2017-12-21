package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.util.ModelDiffCallback;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static android.support.v7.util.DiffUtil.calculateDiff;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends ViewModel {

    private final EventRepository repository;

    public EventViewModel(){
        repository = EventRepository.getInstance();
    }

    public Flowable<DiffUtil.DiffResult> getEvents(List<Event> source, String teamId) {
        return repository.getEvents(teamId).map(updatedEvents -> {
            List<Event> copy = new ArrayList<>(source);
            ModelUtils.preserveList(source, updatedEvents);
            Collections.sort(source);

            return calculateDiff(new ModelDiffCallback(source, copy));
        });
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
