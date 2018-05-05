package com.mainstreetcode.teammate.repository;

import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.GuestDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class GuestRepository extends QueryRepository<Guest, Event> {

    private final TeammateApi api;
    private final GuestDao guestDao;

    private static GuestRepository ourInstance;

    private GuestRepository() {
        api = TeammateService.getApiInstance();
        guestDao = AppDatabase.getInstance().guestDao();
    }

    public static GuestRepository getInstance() {
        if (ourInstance == null) ourInstance = new GuestRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Guest> dao() {
        return guestDao;
    }

    @Override
    public Single<Guest> createOrUpdate(Guest model) {
        return api.rsvpEvent(model.getEvent().getId(), model.isAttending())
                .map(getLocalUpdateFunction(model))
                .map(getSaveFunction());
    }

    @Override
    public Flowable<Guest> get(String id) {
        Maybe<Guest> local = guestDao.get(id).subscribeOn(io());
        Maybe<Guest> remote = api.getGuest(id).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Guest> delete(Guest model) {
        return Single.error(new TeammateException("Unimplemented"));
    }

    @Override
    Maybe<List<Guest>> localModelsBefore(Event key, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        return guestDao.getGuests(key.getId(), date).subscribeOn(io());
    }

    @Override
    Maybe<List<Guest>> remoteModelsBefore(Event key, @Nullable Date date) {
        return api.getEventGuests(key.getId(), date).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Guest>, List<Guest>> provideSaveManyFunction() {
        return models -> {
            List<User> users = new ArrayList<>(models.size());
            List<Event> events = new ArrayList<>(models.size());

            for (Guest guest : models) {
                users.add(guest.getUser());
                events.add(guest.getEvent());
            }

            if (!users.isEmpty()) UserRepository.getInstance().saveAsNested().apply(users);
            if (!events.isEmpty()) EventRepository.getInstance().saveAsNested().apply(events);

            dao().upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
