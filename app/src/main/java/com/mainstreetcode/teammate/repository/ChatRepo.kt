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

import android.content.Context
import android.util.Pair

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.model.Chat
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.ChatDao
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.socket.SocketFactory
import com.mainstreetcode.teammate.util.TeammateException

import org.json.JSONObject

import java.lang.reflect.Type
import java.util.ArrayList
import java.util.Date
import java.util.concurrent.TimeUnit
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

import com.mainstreetcode.teammate.socket.SocketFactory.Companion.EVENT_NEW_MESSAGE
import io.reactivex.schedulers.Schedulers.io
import io.socket.client.Socket.EVENT_ERROR

class ChatRepo internal constructor() : TeamQueryRepo<Chat>() {

    private val app: App = App.instance
    private val api: TeammateApi = TeammateService.getApiInstance()
    private val chatDao: ChatDao = AppDatabase.instance.teamChatDao()

    override fun dao(): EntityDao<in Chat> = chatDao

    override fun createOrUpdate(model: Chat): Single<Chat> =
            post(model).andThen(Single.just(model)).map(saveFunction)

    override fun get(id: String): Flowable<Chat> {
        val local = chatDao.get(id).subscribeOn(io())
        val remote = api.getTeamChat(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Chat): Single<Chat> = api.deleteChat(model.id).map {
        chatDao.delete(model)
        model
    }

    override fun provideSaveManyFunction(): (List<Chat>) -> List<Chat> = { chats ->
        val size = chats.size
        val users = ArrayList<User>(size)
        val teams = ArrayList<Team>(size)

        for (chat in chats) {
            users.add(chat.user)
            teams.add(chat.team)
        }

        RepoProvider.forModel(User::class.java).saveAsNested().invoke(users)
        RepoProvider.forModel(Team::class.java).saveAsNested().invoke(teams)

        chatDao.upsert(chats)
        chats
    }

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<Chat>> {
        var date = pagination
        if (date == null) date = Date()
        return chatDao.chatsBefore(key.id, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<Chat>> =
            api.chatsBefore(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    fun fetchUnreadChats(): Flowable<List<Chat>> = RepoProvider.forRepo(RoleRepo::class.java).myRoles
            .firstElement()
            .toFlowable()
            .flatMap { Flowable.fromIterable(it) }
            .map(Role::team)
            .map { team -> Pair(team.id, getLastTeamSeen(team)) }
            .flatMapMaybe { teamDatePair -> chatDao.unreadChats(teamDatePair.first, teamDatePair.second) }
            .filter { chats -> chats.isNotEmpty() }

    fun listenForChat(team: Team): Flowable<Chat> = SocketFactory.instance.teamChatSocket.flatMapPublisher { socket ->
        val result: JSONObject = try {
            JSONObject(CHAT_GSON.toJson(team))
        } catch (e: Exception) {
            return@flatMapPublisher Flowable.error<Chat>(e)
        }

        socket.emit(SocketFactory.EVENT_JOIN, result)
        val signedInUser = RepoProvider.forRepo(UserRepo::class.java).currentUser

        Flowable.create<Chat>(
                { emitter ->
                    socket.on(EVENT_NEW_MESSAGE) { parseChat(*it)?.apply { emitter.onNext(this) } }
                    socket.once(EVENT_ERROR) { if (!emitter.isCancelled) emitter.onError(it[0] as Throwable) }
                },
                BackpressureStrategy.DROP)
                .filter { chat -> team == chat.team && signedInUser != chat.user }
    }

    private fun post(chat: Chat): Completable = SocketFactory.instance.teamChatSocket.flatMapCompletable { socket ->
        Completable.create { emitter ->
            val result = JSONObject(CHAT_GSON.toJson(chat))
            socket.emit(EVENT_NEW_MESSAGE, arrayOf<Any>(result)) { args ->
                parseChat(*args)?.let { chat.update(it) }
                        ?: return@emit emitter.onError(TeammateException("Unable to post chat"))

                if (!emitter.isDisposed) emitter.onComplete()
            }
        }
    }.onErrorResumeNext { throwable -> postRetryFunction(throwable, chat, 0) }

    private fun postRetryFunction(throwable: Throwable, chat: Chat, previousRetries: Int): Completable = when (val retries = previousRetries + 1) {
        in 0..3 -> Completable.timer(300, TimeUnit.MILLISECONDS)
                .andThen(post(chat).onErrorResumeNext { thrown -> postRetryFunction(thrown, chat, retries) })
        else -> Completable.error(throwable)
    }

    fun updateLastSeen(team: Team) {
        val preferences = app.getSharedPreferences(TEAM_SEEN_TIMES, Context.MODE_PRIVATE)
        preferences.edit().putLong(team.id, Date().time).apply()
    }

    private fun getLastTeamSeen(team: Team): Date {
        val preferences = app.getSharedPreferences(TEAM_SEEN_TIMES, Context.MODE_PRIVATE)
        var timeStamp = preferences.getLong(team.id, TEAM_NOT_SEEN.toLong())
        if (timeStamp == TEAM_NOT_SEEN.toLong()) {
            updateLastSeen(team)
            timeStamp = Date().time - 1000 * 60 * 2
        }
        return Date(timeStamp)
    }

    companion object {


        private const val TEAM_SEEN_TIMES = "TeamRepository.team.seen.times"
        private const val TEAM_NOT_SEEN = -1
        private val CHAT_GSON = chatGson

        private fun parseChat(vararg args: Any): Chat? = try {
            CHAT_GSON.fromJson(args[0].toString(), Chat::class.java)
        } catch (e: Exception) {
            null
        }

        private val chatGson: Gson
            get() {
                val teamAdapter = object : Team.GsonAdapter() {
                    override fun serialize(src: Team, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                        val result = super.serialize(src, typeOfSrc, context).asJsonObject
                        result.addProperty("_id", src.id)
                        return result
                    }
                }
                val chatAdapter = object : Chat.GsonAdapter() {
                    override fun serialize(src: Chat, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
                        val result = super.serialize(src, typeOfSrc, context).asJsonObject
                        result.addProperty("_id", src.id)
                        return result
                    }
                }
                return GsonBuilder()
                        .registerTypeAdapter(Team::class.java, teamAdapter)
                        .registerTypeAdapter(Chat::class.java, chatAdapter)
                        .registerTypeAdapter(User::class.java, User.GsonAdapter())
                        .create()
            }
    }
}
