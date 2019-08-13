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

import android.net.Uri
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.notifications.MediaNotifier
import com.mainstreetcode.teammate.notifications.NotifierProvider
import com.mainstreetcode.teammate.persistence.AppDatabase
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.persistence.MediaDao
import com.mainstreetcode.teammate.rest.ProgressRequestBody
import com.mainstreetcode.teammate.rest.TeammateApi
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.TeammateException
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers.io
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*

class MediaRepo internal constructor() : TeamQueryRepo<Media>() {

    private val api: TeammateApi = TeammateService.getApiInstance()
    private val mediaDao: MediaDao = AppDatabase.instance.mediaDao()
    private val numCallsToIgnore: Int = {
        var callsToIgnore = 0
        val client = TeammateService.getHttpClient()

        for (i in client.interceptors()) if (logsRequestBody(i)) callsToIgnore++
        callsToIgnore
    }()

    override fun dao(): EntityDao<in Media> = mediaDao

    override fun createOrUpdate(model: Media): Single<Media> {
        val body = getBody(model.url, Media.UPLOAD_KEY)
                ?: return Single.error(TeammateException("Unable to upload media"))

        val mediaSingle = api.uploadTeamMedia(model.team.id, body)
                .map(getLocalUpdateFunction(model))
                .map(saveFunction)

        return NotifierProvider.forNotifier(MediaNotifier::class.java).notifyOfUploads(mediaSingle, body.body())
    }

    override fun get(id: String): Flowable<Media> {
        val local = mediaDao.get(id).subscribeOn(io())
        val remote = api.getMedia(id).map(saveFunction).toMaybe()

        return fetchThenGetModel(local, remote)
    }

    override fun delete(model: Media): Single<Media> =
            api.deleteMedia(model.id)
                    .map(this::deleteLocally)
                    .doOnError { throwable -> deleteInvalidModel(model, throwable) }


    fun flag(model: Media): Single<Media> =
            api.flagMedia(model.id)
                    .map(getLocalUpdateFunction(model))
                    .map(saveFunction)


    override fun provideSaveManyFunction(): (List<Media>) -> List<Media> = { models ->
        val users = ArrayList<User>(models.size)
        val teams = ArrayList<Team>(models.size)

        for (media in models) {
            users.add(media.user)
            teams.add(media.team)
        }

        RepoProvider.forModel(User::class.java).saveAsNested().invoke((users))
        RepoProvider.forModel(Team::class.java).saveAsNested().invoke((teams))

        mediaDao.upsert(Collections.unmodifiableList(models))

        models
    }

    override fun localModelsBefore(key: Team, pagination: Date?): Maybe<List<Media>> {
        var date = pagination
        if (date == null) date = Date()
        return mediaDao.getTeamMedia(key, date, DEF_QUERY_LIMIT).subscribeOn(io())
    }

    override fun remoteModelsBefore(key: Team, pagination: Date?): Maybe<List<Media>> =
            api.getTeamMedia(key.id, pagination, DEF_QUERY_LIMIT).map(saveManyFunction).toMaybe()

    fun ownerDelete(models: List<Media>): Single<List<Media>> =
            api.deleteMedia(models).doAfterSuccess(this::delete)

    fun privilegedDelete(team: Team, models: List<Media>): Single<List<Media>> =
            api.adminDeleteMedia(team.id, models).doAfterSuccess(this::delete)

    private fun getBody(path: String, photoKey: String): MultipartBody.Part? {
        val uri = Uri.parse(path)
        val type = App.instance.contentResolver.getType(uri) ?: return null

        val requestBody = ProgressRequestBody(uri, numCallsToIgnore, MediaType.parse(type))

        return MultipartBody.Part.createFormData(photoKey, "test.jpg", requestBody)
    }

    private fun delete(list: List<Media>) = mediaDao.delete(list)

    private fun logsRequestBody(interceptor: Interceptor): Boolean =
            interceptor is HttpLoggingInterceptor && interceptor.level == HttpLoggingInterceptor.Level.BODY
}
