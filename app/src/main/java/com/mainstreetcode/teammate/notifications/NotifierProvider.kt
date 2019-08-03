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

package com.mainstreetcode.teammate.notifications

import android.app.NotificationChannel

import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Dummy
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.repository.ModelRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.SingletonCache

class NotifierProvider private constructor() {

    private val singletonCache: SingletonCache<Model<*>, Notifier<*>> = SingletonCache(
            { itemClass ->
                when (itemClass) {
                    Team::class.java -> TeamNotifier::class.java
                    Role::class.java -> RoleNotifier::class.java
                    Chat::class.java -> ChatNotifier::class.java
                    Game::class.java -> GameNotifier::class.java
                    Media::class.java -> MediaNotifier::class.java
                    Event::class.java -> EventNotifier::class.java
                    Tournament::class.java -> TournamentNotifier::class.java
                    Competitor::class.java -> CompetitorNotifier::class.java
                    JoinRequest::class.java -> JoinRequestNotifier::class.java
                    else -> falseNotifier.javaClass
                }
            },
            ::get,
            falseNotifier.javaClass to falseNotifier,
            TeamNotifier::class.java to TeamNotifier(),
            RoleNotifier::class.java to RoleNotifier(),
            ChatNotifier::class.java to ChatNotifier(),
            GameNotifier::class.java to GameNotifier(),
            MediaNotifier::class.java to MediaNotifier(),
            EventNotifier::class.java to EventNotifier(),
            TournamentNotifier::class.java to TournamentNotifier(),
            CompetitorNotifier::class.java to CompetitorNotifier(),
            JoinRequestNotifier::class.java to JoinRequestNotifier()
    )

    companion object {

        private lateinit var ourInstance: NotifierProvider

        private val instance: NotifierProvider
            get() {
                if (!::ourInstance.isInitialized) ourInstance = NotifierProvider()
                return ourInstance
            }

        @Suppress("UNCHECKED_CAST")
        fun <T : Model<T>> forModel(itemClass: Class<out T>): Notifier<T> =
                instance.singletonCache.forModel(itemClass) as Notifier<T>

        @Suppress("UNCHECKED_CAST")
        fun <T : Model<T>, R : Notifier<T>> forNotifier(itemClass: Class<R>): R =
                instance.singletonCache.forInstance(itemClass) as R

        private operator fun get(unknown: Class<out Notifier<*>>): Notifier<*> {
            run { Logger.log("NotifierProvider", "Dummy Notifier created for unrecognized class" + unknown.name) }
            return falseNotifier
        }

        @Suppress("UNCHECKED_CAST")
        private val falseNotifier = object : Notifier<Dummy>() {
            override val notifyId: String
                get() = ""

            override val repository: ModelRepo<Dummy>
                get() = RepoProvider.falseRepo as ModelRepo<Dummy>

            override val notificationChannels: Array<NotificationChannel>
                get() = arrayOf()
        }
    }
}
