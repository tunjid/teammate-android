package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends ViewModel {

    private final EventRepository repository;

    public EventViewModel() {repository = EventRepository.getInstance();}

    public List<Identifiable> fromEvent(Event event){
       try {return eventListFunction.apply(event);}
       catch (Exception e) {return new ArrayList<>();}
    }

    public Flowable<DiffUtil.DiffResult> getEvents(List<Event> sourceEventList, String teamId) {
        return Identifiable.diff(repository.getEvents(teamId), () -> sourceEventList, ModelUtils::preserveList);
    }

    public Flowable<DiffUtil.DiffResult> getEvent(Event event, List<Identifiable> eventItems) {
        Flowable<List<Identifiable>> sourceFlowable = repository.get(event).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList);
    }

    public Single<DiffUtil.DiffResult> updateEvent(final Event event, List<Identifiable> eventItems) {
        Flowable<List<Identifiable>> sourceFlowable = repository.createOrUpdate(event).toFlowable().map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList).firstOrError();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final Event event, List<Identifiable> eventItems, boolean attending) {
        Flowable<List<Identifiable>> sourceFlowable = repository.rsvpEvent(event, attending).toFlowable().map(eventListFunction);

        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList).firstOrError();

    }

    public Single<Event> delete(final Event event) {
        return repository.delete(event);
    }

    private Function<Event, List<Identifiable>> eventListFunction = event -> {
        List<Identifiable> result = new ArrayList<>();
        int eventSize = event.size();

        for (int i = 0; i < eventSize; i++) result.add(event.get(i));
        result.add(event.getTeam());
        result.addAll(event.getGuests());

        return result;
    };
}
