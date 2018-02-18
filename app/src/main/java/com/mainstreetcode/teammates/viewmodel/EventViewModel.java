package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.EventRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends TeamMappedViewModel<Event> {

    private final EventRepository repository;

    public EventViewModel() {repository = EventRepository.getInstance();}

    public List<Identifiable> fromEvent(Event event) {
        try {return eventListFunction.apply(event);}
        catch (Exception e) {return new ArrayList<>();}
    }

    public Flowable<DiffUtil.DiffResult> getEvents(Team team) {
        Flowable<List<Identifiable>> sourceFlowable = repository.modelsBefore(team, getQueryDate(team)).map(toIdentifiable)
                .doOnError(throwable -> checkForInvalidTeam(throwable, team));
        return Identifiable.diff(sourceFlowable, () -> getModelList(team), preserveList);
    }

    public Flowable<DiffUtil.DiffResult> getEvent(Event event, List<Identifiable> eventItems) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.get(event), event.getTeam(), event).cast(Event.class).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList);
    }

    public Single<DiffUtil.DiffResult> createOrUpdateEvent(final Event event, List<Identifiable> eventItems) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.createOrUpdate(event).toFlowable(), event.getTeam(), event).cast(Event.class).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList).firstOrError();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final Event event, List<Identifiable> eventItems, boolean attending) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.rsvpEvent(event, attending).toFlowable(), event.getTeam(), event).cast(Event.class).map(eventListFunction);
        return Identifiable.diff(sourceFlowable, () -> eventItems, (sourceEventList, newEventList) -> newEventList).firstOrError();
    }

    public Single<Event> delete(final Event event) {
        return checkForInvalidObject(repository.delete(event).toFlowable(), event.getTeam(), event)
                .firstOrError()
                .doOnSuccess(getModelList(event.getTeam())::remove)
                .cast(Event.class)
                .observeOn(mainThread());
    }

    public void onEventTeamChanged(Event event, Team newTeam) {
        getModelList(event.getTeam()).remove(event);
        event.setTeam(newTeam);
    }

    private Function<Event, List<Identifiable>> eventListFunction = event -> {
        List<Identifiable> result = new ArrayList<>();
        int eventSize = event.size();

        for (int i = 0; i < eventSize; i++) result.add(event.get(i));
        result.add(event.getTeam());
        result.addAll(event.getGuests());

        return result;
    };

    private Date getQueryDate(Team team) {
        List<Identifiable> list = getModelList(team);

        if (list.isEmpty()) return null;

        ListIterator<Identifiable> li = list.listIterator(list.size());
        while (li.hasPrevious()) {
            Identifiable item = li.previous();
            if (item instanceof Event) return ((Event) item).getStartDate();
        }

        return new Date();
    }
}
