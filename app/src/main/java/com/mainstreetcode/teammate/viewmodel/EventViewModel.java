package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.VisibleRegion;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.EventSearchRequest;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.enums.BlockReason;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.repository.EventRepository;
import com.mainstreetcode.teammate.repository.GuestRepository;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.EventGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.GuestGofer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.processors.PublishProcessor;

import static android.location.Location.distanceBetween;
import static com.mainstreetcode.teammate.util.ModelUtils.findLast;
import static io.reactivex.Single.concat;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for {@link Event events}
 */

public class EventViewModel extends TeamMappedViewModel<Event> {

    private static final LatLngBounds DEFAULT_BOUNDS;
    private static final int DEFAULT_BAR_RANGE = 50;

    private final EventRepository repository;
    private final List<Event> publicEvents = new ArrayList<>();
    private final EventSearchRequest eventRequest = EventSearchRequest.empty();
    private final PublishProcessor<BlockedUser> blockedUserAlert = PublishProcessor.create();

    static { DEFAULT_BOUNDS = new LatLngBounds.Builder().include(new LatLng(0, 0)).build(); }

    public EventViewModel() {
        repository = EventRepository.getInstance();
    }

    public EventGofer gofer(Event event) {
        Function<Guest, Single<Guest>> rsvpFunction = source -> GuestRepository.getInstance().createOrUpdate(source).doOnSuccess(guest -> {
            if (!guest.isAttending()) pushModelAlert(Alert.eventAbsentee(guest.getEvent()));
        });
        return new EventGofer(event, onError(event), blockedUserAlert, this::getEvent, this::createOrUpdateEvent, this::delete, rsvpFunction);
    }

    public GuestGofer gofer(Guest guest) {
        return new GuestGofer(guest, throwable -> {
            Message message = Message.fromThrowable(throwable);
            if (message == null || !message.isInvalidObject()) return;

            User guestUser = guest.getUser();
            Team guestTeam = guest.getEvent().getTeam();
            BlockReason reason = BlockReason.empty();
            pushModelAlert(Alert.userBlocked(BlockedUser.block(guestUser, guestTeam, reason)));
        }, GuestRepository.getInstance()::get);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        blockedUserAlert.onComplete();
    }

    @Override
    @SuppressLint("CheckResult")
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        if (!(alert instanceof Alert.UserBlocked)) return;

        blockedUserAlert.onNext(((Alert.UserBlocked) alert).getModel());
    }

    @Override
    Flowable<List<Event>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(key, fetchLatest));
    }

    private Flowable<Event> getEvent(Event event) {
        return event.isEmpty() ? Flowable.empty() : repository.get(event);
    }

    private Single<Event> createOrUpdateEvent(final Event event) {
        return repository.createOrUpdate(event);
    }

    private Single<Event> delete(final Event event) {
        return repository.delete(event).doOnSuccess(deleted -> {
            getModelList(event.getTeam()).remove(deleted);
            pushModelAlert(Alert.eventAbsentee(deleted));
        });
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

    private EventSearchRequest fromMap(GoogleMap map) {
        LatLng location = map.getCameraPosition().target;

        VisibleRegion visibleRegion = map.getProjection().getVisibleRegion();
        LatLngBounds bounds = visibleRegion.latLngBounds;
        LatLng southwest = bounds.southwest;
        LatLng northeast = bounds.northeast;

        int miles = Math.min(DEFAULT_BAR_RANGE, milesBetween(southwest, northeast));

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
}
