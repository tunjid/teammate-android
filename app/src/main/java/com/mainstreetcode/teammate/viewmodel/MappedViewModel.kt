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


import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.model.toMessage
import com.mainstreetcode.teammate.notifications.NotifierProvider
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.asDifferentiables
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

abstract class MappedViewModel<K, V : Differentiable> internal constructor() : BaseViewModel() {

    private val pullToRefreshCount = AtomicInteger(0)

    internal abstract fun valueClass(): Class<V>

    abstract fun getModelList(key: K): MutableList<Differentiable>

    internal abstract fun fetch(key: K, fetchLatest: Boolean): Flowable<List<V>>

    internal fun checkForInvalidObject(source: Flowable<out Differentiable>, key: K, value: V): Flowable<Differentiable> =
            source.cast(Differentiable::class.java).doOnError { throwable -> checkForInvalidObject(throwable, value, key) }

    internal fun checkForInvalidObject(source: Single<out Differentiable>, key: K, value: V): Single<Differentiable> =
            source.cast(Differentiable::class.java).doOnError { throwable -> checkForInvalidObject(throwable, value, key) }

    fun getMany(key: K, fetchLatest: Boolean): Flowable<DiffUtil.DiffResult> =
            if (fetchLatest) getLatest(key) else getMore(key)

    fun getMore(key: K): Flowable<DiffUtil.DiffResult> = FunctionalDiff.of(
            fetch(key, false).map(::asDifferentiables),
            getModelList(key),
            this::preserveList
    )
            .doOnError { throwable -> checkForInvalidKey(throwable, key) }

    fun refresh(key: K): Flowable<DiffUtil.DiffResult> = FunctionalDiff.of(
            fetch(key, true).map(::asDifferentiables),
            getModelList(key),
            this::pullToRefresh
    )
            .doOnError { throwable -> checkForInvalidKey(throwable, key) }
            .doOnTerminate { pullToRefreshCount.set(0) }

    fun swap(from: K, to: K): Flowable<DiffUtil.DiffResult> = FunctionalDiff.of(
            Flowable.fromCallable { getModelList(from) }.map(::asDifferentiables),
            getModelList(to)
    ) { _, copiedFrom -> copiedFrom }
            .concatWith(Flowable.defer { refresh(to) })

    private fun getLatest(key: K): Flowable<DiffUtil.DiffResult> = FunctionalDiff.of(
            fetch(key, true).map(::asDifferentiables),
            getModelList(key),
            this::preserveList
    )
            .doOnError { throwable -> checkForInvalidKey(throwable, key) }

    fun clearNotifications(value: V) = clearNotification(itemToModel(value))

    fun clearNotifications(key: K) =
            getModelList(key).mapNotNull(this::itemToModel).forEach(this::clearNotification)

    private fun pullToRefresh(source: List<Differentiable>, additions: List<Differentiable>): List<Differentiable> {
        val next = if (pullToRefreshCount.getAndIncrement() == 0) listOf() else source

        return afterPullToRefreshDiff(preserveList(next, additions))
    }

    internal open fun afterPullToRefreshDiff(source: List<Differentiable>): List<Differentiable> = source

    internal open fun onInvalidKey(key: K) {}

    internal open fun onErrorMessage(message: Message, key: K, invalid: Differentiable) {
        if (message.isInvalidObject) getModelList(key).remove(invalid)
    }

    internal open fun itemToModel(identifiable: Differentiable): Model<*>? =
            if (identifiable is Model<*>) identifiable
            else null

    private fun clearNotification(model: Model<*>?) = when (model) {
        is User -> NotifierProvider.forModel(User::class.java).clearNotifications(model)
        is Team -> NotifierProvider.forModel(Team::class.java).clearNotifications(model)
        is Role -> NotifierProvider.forModel(Role::class.java).clearNotifications(model)
        is Chat -> NotifierProvider.forModel(Chat::class.java).clearNotifications(model)
        is Game -> NotifierProvider.forModel(Game::class.java).clearNotifications(model)
        is Media -> NotifierProvider.forModel(Media::class.java).clearNotifications(model)
        is Event -> NotifierProvider.forModel(Event::class.java).clearNotifications(model)
        is Tournament -> NotifierProvider.forModel(Tournament::class.java).clearNotifications(model)
        is JoinRequest -> NotifierProvider.forModel(JoinRequest::class.java).clearNotifications(model)
        else -> Unit
    }

    internal fun checkForInvalidObject(throwable: Throwable, model: V, key: K) {
        val message = throwable.toMessage()
        if (message != null) onErrorMessage(message, key, model)
    }

    internal open fun getQueryDate(fetchLatest: Boolean, key: K, dateFunction: (V) -> Date): Date? {
        if (fetchLatest) return null

        val value = getModelList(key).filterIsInstance(valueClass()).lastOrNull()
        return if (value == null) null else dateFunction.invoke(value)
    }

    private fun checkForInvalidKey(throwable: Throwable, key: K) {
        val message = throwable.toMessage()
        val isInvalidModel = message != null && !message.isValidModel

        if (isInvalidModel) onInvalidKey(key)
    }
}