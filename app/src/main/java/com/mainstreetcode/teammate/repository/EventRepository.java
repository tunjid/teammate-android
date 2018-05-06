package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.EventDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

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
        return eventDao.getEvents(team.getId(), date).subscribeOn(io());
    }

    @Override
    Maybe<List<Event>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getEvents(team.getId(), date).map(getSaveManyFunction()).toMaybe();
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
}
