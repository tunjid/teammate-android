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

package com.mainstreetcode.teammate.viewmodel

import android.annotation.SuppressLint
import android.location.Location.distanceBetween
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.EventSearchRequest
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.enums.BlockReason
import com.mainstreetcode.teammate.model.toMessage
import com.mainstreetcode.teammate.repository.EventRepo
import com.mainstreetcode.teammate.repository.GuestRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.preserveAscending
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.mainstreetcode.teammate.viewmodel.gofers.EventGofer
import com.mainstreetcode.teammate.viewmodel.gofers.GuestGofer
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.Single.concat
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.processors.PublishProcessor
import kotlin.math.min

/**
 * ViewModel for [events][Event]
 */

class EventViewModel : TeamMappedViewModel<Event>() {

    val eventRequest: EventSearchRequest = EventSearchRequest.empty()

    private val publicEvents = mutableListOf<Event>()
    private val repository: EventRepo = RepoProvider.forRepo(EventRepo::class.java)
    private val blockedUserAlert = PublishProcessor.create<BlockedUser>()

    val lastPublicSearchLocation: LatLng?
        get() {
            val location = eventRequest.location ?: return null
            return if (milesBetween(DEFAULT_BOUNDS.center, location) < DEFAULT_BAR_RANGE) null else location
        }

    fun gofer(event: Event): EventGofer = EventGofer(
            event,
            onError(event),
            blockedUserAlert,
            this::getEvent,
            this::createOrUpdateEvent,
            this::delete,
            rsvpFunction = { source: Guest ->
                RepoProvider.forRepo(GuestRepo::class.java)
                        .createOrUpdate(source)
                        .doOnSuccess { if (!it.isAttending) pushModelAlert(Alert.eventAbsentee(it.event)) }
            })

    fun gofer(guest: Guest): GuestGofer = GuestGofer(
            guest,
            onError@{ throwable ->
                val message = throwable.toMessage()
                if (message == null || !message.isInvalidObject) return@onError

                val guestUser = guest.user
                val guestTeam = guest.event.team
                val reason = BlockReason.empty()
                pushModelAlert(Alert.creation(BlockedUser.block(guestUser, guestTeam, reason)))
            },
            RepoProvider.forRepo(GuestRepo::class.java)::get
    )

    override fun onCleared() {
        super.onCleared()
        blockedUserAlert.onComplete()
    }

    override fun valueClass(): Class<Event> = Event::class.java

    @SuppressLint("CheckResult")
    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        alert.matches(
                Alert.of(Alert.Deletion::class.java, Game::class.java, this::onGameDeleted),
                Alert.of(Alert.Creation::class.java, BlockedUser::class.java, blockedUserAlert::onNext)
        )
    }

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<Event>> =
            repository.modelsBefore(key, getQueryDate(fetchLatest, key) { it.startDate })

    private fun getEvent(event: Event): Flowable<Event> =
            if (event.isEmpty) Flowable.empty() else repository[event]

    private fun createOrUpdateEvent(event: Event): Single<Event> = repository.createOrUpdate(event)

    private fun delete(event: Event): Single<Event> {
        return repository.delete(event).doOnSuccess { deleted ->
            getModelList(event.team).remove(deleted)
            pushModelAlert(Alert.eventAbsentee(deleted))
        }
    }

    fun getPublicEvents(map: GoogleMap): Flowable<List<Event>> {
        val fetched = TeammateService.getApiInstance()
                .getPublicEvents(fromMap(map))
                .map(this::collatePublicEvents)
                .map { this.filterPublicEvents(it) }

        return concat(Single.just<List<Event>>(publicEvents).map(this::filterPublicEvents), fetched)
                .observeOn(mainThread())
    }

    fun onEventTeamChanged(event: Event, newTeam: Team) {
        getModelList(event.team).remove(event)
        event.updateTeam(newTeam)
    }

    private fun fromMap(map: GoogleMap): EventSearchRequest {
        val location = map.cameraPosition.target

        val visibleRegion = map.projection.visibleRegion
        val bounds = visibleRegion.latLngBounds
        val southwest = bounds.southwest
        val northeast = bounds.northeast

        val miles = min(DEFAULT_BAR_RANGE, milesBetween(southwest, northeast))

        eventRequest.setDistance(miles.toString())
        eventRequest.location = location

        return eventRequest
    }

    private fun collatePublicEvents(newEvents: List<Event>): List<Event> {
        preserveAscending(publicEvents, newEvents)
        return publicEvents
    }

    private fun filterPublicEvents(source: List<Event>): List<Event> {
        val sport = eventRequest.sport
        if (sport.isInvalid) return source

        return publicEvents.filter { event -> event.team.sport == sport }
    }

    private fun milesBetween(locationA: LatLng, locationB: LatLng): Int {
        val distance = FloatArray(1)
        distanceBetween(locationA.latitude, locationA.longitude, locationB.latitude, locationB.longitude, distance)

        return (distance[0] * 0.000621371).toInt()
    }

    private fun onGameDeleted(game: Game) {
        for (list in modelListMap.values) {
            val iterator = list.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next() as? Event ?: return
                if (game.id == next.gameId) iterator.remove()
            }
        }
    }

    companion object {

        private const val DEFAULT_BAR_RANGE = 50
        private val DEFAULT_BOUNDS: LatLngBounds = LatLngBounds.Builder().include(LatLng(0.0, 0.0)).build()

    }
}
