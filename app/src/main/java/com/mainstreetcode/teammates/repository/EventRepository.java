package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.EntityDao;
import com.mainstreetcode.teammates.persistence.EventDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

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

public class EventRepository extends QueryRepository<Event> {

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
    Maybe<List<Event>> localModelsBefore(Team team, Date date) {
        return eventDao.getEvents(team.getId(), date).subscribeOn(io());
    }

    @Override
    Maybe<List<Event>> remoteModelsBefore(Team team, Date date) {
        return api.getEvents(team.getId(), date, date == null).map(getSaveManyFunction()).toMaybe();
    }

    public Single<Event> rsvpEvent(final Event event, boolean attending) {
        return api.rsvpEvent(event.getId(), attending)
                .map(getLocalUpdateFunction(event))
                .map(getSaveFunction());
    }

    @Override
    Function<List<Event>, List<Event>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            for (Event event : models) teams.add(event.getTeam());

            teamRepository.getSaveManyFunction().apply(teams);
            eventDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
