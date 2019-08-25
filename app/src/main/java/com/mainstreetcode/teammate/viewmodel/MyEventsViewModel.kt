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

import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.repository.EventRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable

/**
 * ViewModel for [events][Event]
 */

class MyEventsViewModel : MappedViewModel<Class<Event>, Event>() {

    private val attending = mutableListOf<Differentiable>()
    private val repository: EventRepo = RepoProvider.forRepo(EventRepo::class.java)

    override fun valueClass(): Class<Event> = Event::class.java

    override fun getModelList(key: Class<Event>): MutableList<Differentiable> = attending

    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        alert.matches(
                Alert.of(Alert.Deletion::class.java, Team::class.java, this::removeDeletedTeamEvents),
                Alert.of(Alert.EventAbsentee::class.java, Event::class.java) { getModelList(Event::class.java).remove(it) },
                Alert.of(Alert.Creation::class.java, BlockedUser::class.java) { blockedUser: BlockedUser -> removeDeletedTeamEvents(blockedUser.team) }
        )
    }

    override fun fetch(key: Class<Event>, fetchLatest: Boolean): Flowable<List<Event>> =
            repository.attending(getQueryDate(fetchLatest, key) { it.startDate })

    private fun removeDeletedTeamEvents(matcher: Team?) {
        if (matcher == null) return

        val iterator = getModelList(Event::class.java).iterator()

        while (iterator.hasNext()) {
            val identifiable = iterator.next() as? Event ?: continue
            if (identifiable.team == matcher) iterator.remove()
        }
    }
}
