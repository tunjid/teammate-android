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
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.EventDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.tunjid.androidbootstrap.functions.collections.Lists
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import java.util.*

class EventRepo internal constructor() : TeamQueryRepo<Event>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val eventDao: EventDao = AppDatabase.getInstance().eventDao()

    override fun dao(): EntityDao<in Event> = eventDao

    override fun createOrUpdate(model: Event): Single<Event> {
        var eventSingle = if (model.isEmpty) api.createEvent(model).map(getLocalUpdateFunction(model))
        else api.updateEvent(model.id, model)
                .map(getLocalUpdateFunction(model))
                .doOnError { throwable -> deleteInvalidModel(model, throwable) }

        val body = getBody(model.headerItem.getValue(), Event.PHOTO_UPLOAD_KEY)
        if (body != null) eventSingle = eventSingle.flatMap { api.uploadEventPhoto(model.id, body) }

        return eventSingle.map(saveFunction)
    }

    override fun get(id: String): Flowable<Event> {
        val local = eventDao.get(id).subscribeOn(io())
        val remote = api.getEvent(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Event): Single<Event> =
            api.deleteEvent(model.id)
                    .map { this.deleteLocally(it) }
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<Event>> {
        var date = pagination
        if (date == null) date = futureDate
        return eventDao.getEvents(key.id, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<Event>> =
            api.getEvents(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    fun attending(date: Date?): Flowable<List<Event>> {
        val current = RepoProvider.forRepo(UserRepo::class.java).currentUser
        val localDate = date ?: futureDate

        val local = AppDatabase.getInstance().guestDao().getRsvpList(current.id, localDate)
                .map<List<Event>> { guests -> ArrayList(Lists.transform<Guest, Event>(guests) { it.event }) }
                .subscribeOn(io())

        val remote = api.eventsAttending(date, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

        return fetchThenGet(local, remote)
    }

    override fun provideSaveManyFunction(): (List<Event>) -> List<Event> = { models ->
        val teams = ArrayList<Team>(models.size)
        for (event in models) teams.add(event.team)

        RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))
        eventDao.upsert(models)

        models
    }

    override fun deleteLocally(model: Event): Event {
        val game = Game.withId(model.gameId)
        if (!game.isEmpty) AppDatabase.getInstance().gameDao().delete(game)
        return super.deleteLocally(model)
    }
}
