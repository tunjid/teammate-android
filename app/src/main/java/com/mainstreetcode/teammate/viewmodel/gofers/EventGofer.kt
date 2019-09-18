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

package com.mainstreetcode.teammate.viewmodel.gofers

import android.annotation.SuppressLint
import android.location.Address
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.repository.GuestRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.asDifferentiables
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import java.util.*

class EventGofer @SuppressLint("CheckResult")
constructor(
        model: Event,
        onError: (Throwable) -> Unit,
        blockedUserFlowable: Flowable<BlockedUser>,
        private val getFunction: (Event) -> Flowable<Event>,
        private val updateFunction: (Event) -> Single<Event>,
        private val deleteFunction: (Event) -> Single<Event>,
        private val rsvpFunction: (Guest) -> Single<Guest>
) : TeamHostingGofer<Event>(model, onError) {

    var isSettingLocation: Boolean = false
    private val guestRepository: GuestRepo = RepoProvider.forRepo(GuestRepo::class.java)

    val rsvpStatus: Single<Int>
        get() = Single.fromCallable {
            ArrayList(items)
                    .filterIsInstance(Guest::class.java)
                    .filter(Guest::isAttending)
                    .map(Guest::user)
                    .filter { signedInUser == it }
                    .let {
                        if (it.isEmpty()) R.drawable.ic_event_available_white_24dp
                        else R.drawable.ic_event_busy_white_24dp
                    }
        }.subscribeOn(Schedulers.io()).observeOn(mainThread())

    init {

        items.addAll(model.asDifferentiables())
        items.add(model.team)

        blockedUserFlowable.subscribe(this::onUserBlocked, ErrorHandler.EMPTY::invoke)
    }

    fun getToolbarTitle(fragment: Fragment): CharSequence {
        return when {
            model.isEmpty -> fragment.getString(R.string.create_event)
            else -> model.name
        }
    }

    override fun getImageClickMessage(fragment: Fragment): String? =
            if (hasPrivilegedRole()) null else fragment.getString(R.string.no_permission)

    override fun fetch(): Flowable<DiffUtil.DiffResult> {
        if (isSettingLocation) return Flowable.empty()
        val eventFlowable = getFunction.invoke(model).map(Event::asDifferentiables)
        val guestsFlowable = guestRepository.modelsBefore(model, Date()).map(::asDifferentiables)
        val sourceFlowable = Flowable.concatDelayError(listOf(eventFlowable, guestsFlowable))
        return FunctionalDiff.of(sourceFlowable, items, this::preserveItems)
    }

    override fun upsert(): Single<DiffUtil.DiffResult> {
        val source = updateFunction.invoke(model).map(Event::asDifferentiables)
        return FunctionalDiff.of(source, items, this::preserveItems)
    }

    fun rsvpEvent(attending: Boolean): Single<DiffUtil.DiffResult> {
        val single: Single<List<Differentiable>> = rsvpFunction.invoke(Guest.forEvent(model, attending)).map { listOf(it) }

        return FunctionalDiff.of(single, items) { staleCopy, singletonGuestList ->
            (staleCopy - singletonGuestList) + singletonGuestList
        }
    }

    override fun delete(): Completable = deleteFunction.invoke(model).ignoreElement()

    fun setAddress(address: Address): Single<DiffUtil.DiffResult> {
        isSettingLocation = true
        model.setAddress(address)
        return FunctionalDiff.of(Single.just(model.asDifferentiables()), items, this::preserveItems)
                .doFinally { isSettingLocation = false }
    }

    private fun onUserBlocked(blockedUser: BlockedUser) {
        if (blockedUser.team != model.team) return

        val iterator = items.iterator()

        while (iterator.hasNext()) {
            val identifiable = iterator.next() as? Guest ?: continue
            val blocked = blockedUser.user
            val guestUser = identifiable.user

            if (blocked == guestUser) iterator.remove()
        }
    }
}
