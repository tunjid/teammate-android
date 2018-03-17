package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.EventRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static com.mainstreetcode.teammates.util.ModelUtils.findLast;
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

    @Override
    @SuppressWarnings("unchecked")
    <T extends Model<T>> List<Class<T>> notifiedClasses() {
        return Collections.singletonList((Class<T>) Event.class);
    }

    @Override
    Flowable<List<Event>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest))
                .doOnError(throwable -> checkForInvalidTeam(throwable, key));
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

    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;

        Event event = findLast(getModelList(team), Event.class);
        return event == null ? null : event.getStartDate();
    }
}
