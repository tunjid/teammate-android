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

import com.mainstreetcode.teammate.model.BlockedUser
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Config
import com.mainstreetcode.teammate.model.Device
import com.mainstreetcode.teammate.model.Dummy
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.Prefs
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Stat
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.model.Tournament
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.util.Logger
import com.mainstreetcode.teammate.util.SingletonCache
import io.reactivex.Flowable
import io.reactivex.Single

class RepoProvider private constructor() {

    private val singletonCache: SingletonCache<Model<*>, ModelRepo<*>> = SingletonCache(
            { itemClass ->
                when (itemClass) {
                    User::class.java -> UserRepo::class.java
                    Team::class.java -> TeamRepo::class.java
                    Role::class.java -> RoleRepo::class.java
                    Chat::class.java -> ChatRepo::class.java
                    Game::class.java -> GameRepo::class.java
                    Stat::class.java -> StatRepo::class.java
                    Prefs::class.java -> PrefsRepo::class.java
                    Media::class.java -> MediaRepo::class.java
                    Guest::class.java -> GuestRepo::class.java
                    Event::class.java -> EventRepo::class.java
                    Config::class.java -> ConfigRepo::class.java
                    Device::class.java -> DeviceRepo::class.java
                    Tournament::class.java -> TournamentRepo::class.java
                    Competitor::class.java -> CompetitorRepo::class.java
                    BlockedUser::class.java -> BlockedUserRepo::class.java
                    JoinRequest::class.java -> JoinRequestRepo::class.java
                    TeamMember::class.java -> TeamMemberRepo::class.java
                    else -> falseRepo.javaClass
                }
            },
            ::get,
            falseRepo.javaClass to falseRepo,
            UserRepo::class.java to UserRepo(),
            TeamRepo::class.java to TeamRepo(),
            RoleRepo::class.java to RoleRepo(),
            ChatRepo::class.java to ChatRepo(),
            GameRepo::class.java to GameRepo(),
            StatRepo::class.java to StatRepo(),
            PrefsRepo::class.java to PrefsRepo(),
            MediaRepo::class.java to MediaRepo(),
            GuestRepo::class.java to GuestRepo(),
            EventRepo::class.java to EventRepo(),
            ConfigRepo::class.java to ConfigRepo(),
            DeviceRepo::class.java to DeviceRepo(),
            GameRoundRepo::class.java to GameRoundRepo(),
            TournamentRepo::class.java to TournamentRepo(),
            CompetitorRepo::class.java to CompetitorRepo(),
            BlockedUserRepo::class.java to BlockedUserRepo(),
            JoinRequestRepo::class.java to JoinRequestRepo(),
            TeamMemberRepo::class.java to TeamMemberRepo<JoinRequest>()
    )

    companion object {

        private lateinit var ourInstance: RepoProvider

        private val instance: RepoProvider
            get() {
                if (!::ourInstance.isInitialized) ourInstance = RepoProvider()
                return ourInstance
            }

        fun initialized(): Boolean = ::ourInstance.isInitialized

        @Suppress("UNCHECKED_CAST")
        fun <T : Model<T>, R : ModelRepo<T>> forRepo(itemClass: Class<R>): R =
                instance.singletonCache.forInstance(itemClass) as R

        @Suppress("UNCHECKED_CAST")
        fun <T : Model<T>> forModel(itemClass: Class<T>): ModelRepo<T> =
                instance.singletonCache.forModel(itemClass) as ModelRepo<T>

        @Suppress("UNCHECKED_CAST")
        fun <T : Model<T>> forModel(item: T): ModelRepo<T> =
                instance.singletonCache.forModel(item::class.java) as ModelRepo<T>

        private operator fun get(unknown: Class<out ModelRepo<*>>): ModelRepo<*> {
            Logger.log("RepoProvider", "Dummy Repo created for unrecognized class" + unknown.name)
            return falseRepo
        }

        val falseRepo: ModelRepo<*> = object : ModelRepo<Dummy>() {
            override fun dao(): EntityDao<Dummy> = EntityDao.daDont()

            override fun createOrUpdate(model: Dummy): Single<Dummy> = Single.just(model)

            override operator fun get(id: String): Flowable<Dummy> = Flowable.error(UnsupportedOperationException())

            override fun delete(model: Dummy): Single<Dummy> = Single.error(UnsupportedOperationException())

            override fun provideSaveManyFunction(): (List<Dummy>) -> List<Dummy> =
                    { _ -> emptyList() }
        }

    }

}
