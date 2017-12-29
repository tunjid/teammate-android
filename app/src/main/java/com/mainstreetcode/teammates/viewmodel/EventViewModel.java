package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;
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

public class EventViewModel extends ListViewModel<Event> {

    private final EventRepository repository;

    public EventViewModel() {repository = EventRepository.getInstance();}

    public List<Identifiable> fromEvent(Event event){
       try {return eventListFunction.apply(event);}
       catch (Exception e) {return new ArrayList<>();}
    }

    public Flowable<DiffUtil.DiffResult> getEvents(Team team) {
        return Identifiable.diff(repository.getEvents(team), () -> getModelList(team), ModelUtils::preserveList);
    }

    public Flowable<DiffUtil.DiffResult> getEvent(Event event, List<Identifiable> eventItems) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.get(event), event, event.getTeam()).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList);
    }

    public Single<DiffUtil.DiffResult> updateEvent(final Event event, List<Identifiable> eventItems) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.createOrUpdate(event).toFlowable(), event, event.getTeam()).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList).firstOrError();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final Event event, List<Identifiable> eventItems, boolean attending) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.rsvpEvent(event, attending).toFlowable(), event, event.getTeam()).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList).firstOrError();
    }

    public Single<Event> delete(final Event event) {
        return checkForInvalidObject(repository.delete(event).toFlowable(), event, event.getTeam())
                .firstOrError()
                .doOnSuccess(getModelList(event.getTeam())::remove);
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
