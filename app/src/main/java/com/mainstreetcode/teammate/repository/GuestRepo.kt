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

package com.mainstreetcode.teammate.repository

import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.GuestDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class GuestRepo internal constructor() : QueryRepo<Guest, Event, Date>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val guestDao: GuestDao = AppDatabase.instance.guestDao()

    override fun dao(): EntityDao<in Guest> = guestDao

    override fun createOrUpdate(model: Guest): Single<Guest> =
            api.rsvpEvent(model.event.id, model.isAttending)
                    .map(getLocalUpdateFunction(model))
                    .map(saveFunction)

    override fun get(id: String): Flowable<Guest> {
        val local = guestDao.get(id).subscribeOn(io())
        val remote = api.getGuest(id).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Guest): Single<Guest> =
            Single.error(TeammateException("Unimplemented"))

    override fun localModelsBefore(key: Event, pagination: Date?): Maybe<List<Guest>> {
        var date = pagination
        if (date == null) date = futureDate
        return guestDao.getGuests(key.id, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Event, pagination: Date?): Maybe<List<Guest>> =
            api.getEventGuests(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    override fun provideSaveManyFunction(): (List<Guest>) -> List<Guest> = { models ->
        val users = ArrayList<User>(models.size)
        val events = ArrayList<Event>(models.size)

        for (guest in models) {
            users.add(guest.user)
            events.add(guest.event)
        }

        if (users.isNotEmpty()) RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))
        if (events.isNotEmpty()) RepoProvider.forModel(Event::class.java).saveAsNested().invoke((events))

        dao().upsert(Collections.unmodifiableList(models))

        models
    }
}
