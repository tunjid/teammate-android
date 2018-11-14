package com.mainstreetcode.teammate.repository;


import androidx.annotation.Nullable;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.EventDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TransformingSequentialList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static io.reactivex.schedulers.Schedulers.io;

public class EventRepository extends TeamQueryRepository<Event> {

    private static EventRepository ourInstance;

    private final TeammateApi api;
    private final EventDao eventDao;
    private final ModelRepository<Team> teamRepository;

    private EventRepository() {
        api = TeammateService.getApiInstance();
        eventDao = AppDatabase.getInstance().eventDao();
        teamRepository = TeamRepository.getInstance();
    }

    public static EventRepository getInstance() {
        if (ourInstance == null) ourInstance = new EventRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Event> dao() {
        return eventDao;
    }

    @Override
    public Single<Event> createOrUpdate(Event event) {
        Single<Event> eventSingle = event.isEmpty()
                ? api.createEvent(event).map(getLocalUpdateFunction(event))
                : api.updateEvent(event.getId(), event)
                .map(getLocalUpdateFunction(event))
                .doOnError(throwable -> deleteInvalidModel(event, throwable));

        MultipartBody.Part body = getBody(event.getHeaderItem().getValue(), Event.PHOTO_UPLOAD_KEY);
        if (body != null) {
            eventSingle = eventSingle.flatMap(put -> api.uploadEventPhoto(event.getId(), body));
        }

        return eventSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Event> get(String id) {
        Maybe<Event> local = eventDao.get(id).subscribeOn(io());
        Maybe<Event> remote = api.getEvent(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Event> delete(Event event) {
        return api.deleteEvent(event.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(event, throwable));
    }

    @Override
    Maybe<List<Event>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        return eventDao.getEvents(team.getId(), date, DEF_QUERY_LIMIT).subscribeOn(io());
    }

    @Override
    Maybe<List<Event>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getEvents(team.getId(), date, DEF_QUERY_LIMIT).map(getSaveManyFunction()).toMaybe();
    }

    public Flowable<List<Event>> attending(@Nullable Date date) {
        User current = UserRepository.getInstance().getCurrentUser();
        Date localDate = date == null ? getFutureDate() : date;

        Maybe<List<Event>> local = AppDatabase.getInstance().guestDao().getRsvpList(current.getId(), localDate)
                .map(guests -> (List<Event>) new ArrayList<>(new TransformingSequentialList<>(guests, Guest::getEvent)))
                .subscribeOn(io());

        Maybe<List<Event>> remote = api.eventsAttending(date, DEF_QUERY_LIMIT).map(getSaveManyFunction()).toMaybe();

        return fetchThenGet(local, remote);
    }

    @Override
    Function<List<Event>, List<Event>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            for (Event event : models) teams.add(event.getTeam());

            teamRepository.saveAsNested().apply(teams);
            eventDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }

    @Override
    Event deleteLocally(Event model) {
        Game game = Game.withId(model.getGameId());
        if (!game.isEmpty()) AppDatabase.getInstance().gameDao().delete(game);
        return super.deleteLocally(model);
    }
}
