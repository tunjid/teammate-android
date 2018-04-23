package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.EventSearchRequest;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.repository.EventRepository;
import com.mainstreetcode.teammate.repository.GuestRepository;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

import static android.location.Location.distanceBetween;
import static com.mainstreetcode.teammate.util.ModelUtils.findLast;
import static io.reactivex.Single.concat;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends TeamMappedViewModel<Event> {

    private static final LatLngBounds DEFAULT_BOUNDS;
    private static final int DEFAULT_BAR_RANGE = 60;

    private final EventRepository repository;
    private final GuestRepository guestRepository;
    private final List<Event> publicEvents = new ArrayList<>();
    private final EventSearchRequest eventRequest = EventSearchRequest.empty();

    private Map<Event, List<Identifiable>> eventItemMap = new HashMap<>();

    static { DEFAULT_BOUNDS = new LatLngBounds.Builder().include(new LatLng(0, 0)).build(); }

    public EventViewModel() {
        repository = EventRepository.getInstance();
        guestRepository = GuestRepository.getInstance();
    }

    public List<Identifiable> getEventItems(Event event) {
        List<Identifiable> items = eventItemMap.get(event);
        if (!eventItemMap.containsKey(event))
            eventItemMap.put(event, items = new ArrayList<>(event.asItems()));

        return items;
    }

    @Override
    @SuppressLint("CheckResult")
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        if (!(alert instanceof Alert.UserBlocked)) return;

        User blocked = ((Alert.UserBlocked) alert).getModel();
        Flowable.fromIterable(eventItemMap.values())
                .map(List::iterator)
                .subscribe(iterator -> removeBlockedUser(blocked, iterator), ErrorHandler.EMPTY);
    }

    @Override
    Flowable<List<Event>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest))
                .doOnError(throwable -> checkForInvalidTeam(throwable, key));
    }

    public Flowable<DiffUtil.DiffResult> getEvent(Event event) {
        Flowable<List<Identifiable>> eventFlowable = checkForInvalidObject(repository.get(event), event.getTeam(), event).cast(Event.class).map(Event::asIdentifiables);
        Flowable<List<Identifiable>> guestsFlowable = guestRepository.modelsBefore(event, new Date()).map(ModelUtils::asIdentifiables);
        Flowable<List<Identifiable>> sourceFlowable = Flowable.mergeDelayError(eventFlowable, guestsFlowable);
        return Identifiable.diff(sourceFlowable, () -> getEventItems(event), updateEventList(event));
    }

    public Single<DiffUtil.DiffResult> createOrUpdateEvent(final Event event) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.createOrUpdate(event).toFlowable(), event.getTeam(), event).cast(Event.class).map(Event::asIdentifiables);
        return Identifiable.diff(sourceFlowable, () -> getEventItems(event), updateEventList(event)).firstOrError();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final Event event, boolean attending) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(guestRepository.createOrUpdate(Guest.forEvent(event, attending)).toFlowable(), event.getTeam(), event).map(Collections::singletonList);
        return Identifiable.diff(sourceFlowable, () -> getEventItems(event), (staleCopy, singletonGuestList) -> {
            staleCopy.removeAll(singletonGuestList);
            staleCopy.addAll(singletonGuestList);
            return staleCopy;
        }).firstOrError();
    }

    public Single<Event> delete(final Event event) {
        return checkForInvalidObject(repository.delete(event).toFlowable(), event.getTeam(), event)
                .firstOrError()
                .doOnSuccess(getModelList(event.getTeam())::remove)
                .cast(Event.class)
                .observeOn(mainThread());
    }

    public Flowable<List<Event>> getPublicEvents(GoogleMap map) {
        Single<List<Event>> fetched = TeammateService.getApiInstance()
                .getPublicEvents(fromMap(map))
                .map(this::collatePublicEvents)
                .map(this::filterPublicEvents);

        return concat(Single.just(publicEvents).map(this::filterPublicEvents), fetched).observeOn(mainThread());
    }

    public EventSearchRequest getEventRequest() {
        return eventRequest;
    }

    public void onEventTeamChanged(Event event, Team newTeam) {
        getModelList(event.getTeam()).remove(event);
        event.setTeam(newTeam);
    }

    @Nullable
    public LatLng getLastPublicSearchLocation() {
        LatLng location = eventRequest.getLocation();
        if (location == null) return null;
        if (milesBetween(DEFAULT_BOUNDS.getCenter(), location) < DEFAULT_BAR_RANGE) return null;
        return location;
    }

    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;

        Event event = findLast(getModelList(team), Event.class);
        return event == null ? null : event.getStartDate();
    }

    private BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>> updateEventList(Event event) {
        return (staleList, fetched) -> {
            List<Identifiable> staleItems = new ArrayList<>();
            Flowable.fromIterable(staleList).filter(identifiable -> identifiable instanceof Item)
                    .collect(() -> staleItems, List::add).blockingGet();

            staleList.removeAll(staleItems);
            staleList.addAll(event.asItems());

            ModelUtils.preserveAscending(staleList, fetched);
            return staleList;
        };
    }

    private EventSearchRequest fromMap(GoogleMap map) {
        LatLng location = map.getCameraPosition().target;

        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
        LatLngBounds bounds = visibleRegion.latLngBounds;
        LatLng southwest = bounds.southwest;
        LatLng northeast = bounds.northeast;

        int miles = milesBetween(southwest, northeast);

        eventRequest.setDistance(String.valueOf(miles));
        eventRequest.setLocation(location);

        return eventRequest;
    }

    private List<Event> collatePublicEvents(List<Event> newEvents) {
        ModelUtils.preserveAscending(publicEvents, newEvents);
        return publicEvents;
    }

    private List<Event> filterPublicEvents(List<Event> source) {
        Sport sport = eventRequest.getSport();
        if (sport.isInvalid()) return source;

        List<Event> filtered = new ArrayList<>();

        return Flowable.fromIterable(publicEvents).filter(event -> event.getTeam().getSport().equals(sport))
                .collect(() -> filtered, List::add)
                .blockingGet();
    }

    private int milesBetween(LatLng locationA, LatLng locationB) {
        float[] distance = new float[1];
        distanceBetween(locationA.latitude, locationA.longitude, locationB.latitude, locationB.longitude, distance);

        return (int) (distance[0] * 0.000621371);
    }

    private void removeBlockedUser(User user, Iterator<Identifiable> iterator) {
        while (iterator.hasNext()) {
            Identifiable identifiable = iterator.next();
            if (!(identifiable instanceof Guest)) continue;

            if (((Guest) identifiable).getUser().equals(user)) iterator.remove();
        }
    }
}
