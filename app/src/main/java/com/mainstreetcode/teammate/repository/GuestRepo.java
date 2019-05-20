/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.repository;

import androidx.annotation.Nullable;

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

public class GuestRepo extends QueryRepo<Guest, Event, Date> {

    private final TeammateApi api;
    private final GuestDao guestDao;

    GuestRepo() {
        api = TeammateService.getApiInstance();
        guestDao = AppDatabase.getInstance().guestDao();
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
        return guestDao.getGuests(key.getId(), date, DEF_QUERY_LIMIT).subscribeOn(io());
    }

    @Override
    Maybe<List<Guest>> remoteModelsBefore(Event key, @Nullable Date date) {
        return api.getEventGuests(key.getId(), date, DEF_QUERY_LIMIT).map(getSaveManyFunction()).toMaybe();
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

            if (!users.isEmpty()) RepoProvider.forModel(User.class).saveAsNested().apply(users);
            if (!events.isEmpty()) RepoProvider.forModel(Event.class).saveAsNested().apply(events);

            dao().upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
