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

import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.notifications.ChatNotifier
import com.mainstreetcode.teammate.notifications.NotifierProvider
import com.mainstreetcode.teammate.repository.ChatRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.areDifferentDays
import com.mainstreetcode.teammate.util.calendarPrint
import com.tunjid.androidbootstrap.functions.collections.Lists.findFirst
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.socket.engineio.client.EngineIOException
import java.io.EOFException
import java.util.*


class ChatViewModel : TeamMappedViewModel<Chat>() {

    private val repository: ChatRepo = RepoProvider.forRepo(ChatRepo::class.java)
    private val notifier: ChatNotifier = NotifierProvider.forNotifier(ChatNotifier::class.java)

    override fun hasNativeAds(): Boolean = false

    override fun sortsAscending(): Boolean = true

    override fun valueClass(): Class<Chat> = Chat::class.java

    fun updateLastSeen(team: Team) {
        repository.updateLastSeen(team)

        val chat = findFirst(getModelList(team), Chat::class.java)
        if (chat != null) clearNotifications(chat)
    }

    fun onScrollPositionChanged(team: Team, position: Int): String {
        val item = getModelList(team)[position] as? Chat ?: return ""

        val created = item.created
        val then = Calendar.getInstance()
        val now = Calendar.getInstance()
        then.time = created

        val isToday = !areDifferentDays(created, now.time)
        val isYesterday = (!isToday && now.get(Calendar.DAY_OF_MONTH) - then.get(Calendar.DAY_OF_MONTH) == 1
                && now.get(Calendar.MONTH) == then.get(Calendar.MONTH)
                && now.get(Calendar.YEAR) == then.get(Calendar.YEAR))

        return when {
            isToday -> ""
            isYesterday -> App.getInstance().getString(R.string.chat_yesterday)
            else -> created.calendarPrint()
        }
    }

    fun listenForChat(team: Team): Flowable<Chat> {
        return repository.listenForChat(team)
                .onErrorResumeNext(listenRetryFunction(team)::invoke)
                .doOnSubscribe { notifier.setChatVisibility(team, true) }
                .doFinally { notifier.setChatVisibility(team, false) }
                .observeOn(mainThread())
    }

    fun post(chat: Chat): Single<Chat> = repository.createOrUpdate(chat).observeOn(mainThread())

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<Chat>> =
            repository.modelsBefore(key, getQueryDate(fetchLatest, key) { it.created })

    private fun listenRetryFunction(team: Team): (Throwable) -> Flowable<Chat> = { throwable: Throwable ->
        if (shouldRetry(throwable))
            repository.listenForChat(team)
                    .onErrorResumeNext(listenRetryFunction(team)::invoke)
                    .doOnSubscribe { notifier.setChatVisibility(team, true) }
                    .doFinally { notifier.setChatVisibility(team, false) }
                    .observeOn(mainThread())
        else Flowable.error(throwable)
    }

    private fun shouldRetry(throwable: Throwable): Boolean {
        val retry = XHR_POST_ERROR == throwable.message || throwable is EngineIOException && throwable.cause is EOFException
        if (retry) Logger.log("CHAT", "Retrying because of predictable error", throwable)
        return retry
    }

    override fun getQueryDate(fetchLatest: Boolean, key: Team, dateFunction: (Chat) -> Date): Date? {
        if (fetchLatest) return null

        // Chats use find first
        val value = getModelList(key).filterIsInstance(valueClass()).firstOrNull()
        return if (value == null) null else dateFunction.invoke(value)
    }

    companion object {

        private const val XHR_POST_ERROR = "xhr post error"
    }
}
