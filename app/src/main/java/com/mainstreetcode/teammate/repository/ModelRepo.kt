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


import android.annotation.SuppressLint
import android.webkit.MimeTypeMap
import com.mainstreetcode.teammate.model.Message
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.persistence.EntityDao
import com.mainstreetcode.teammate.util.ErrorHandler
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Maybe.concatDelayError
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File
import java.util.concurrent.atomic.AtomicReference

/**
 * Repository that manages [Model] CRUD operations
 */

abstract class ModelRepo<T : Model<T>> {

    @Suppress("LeakingThis")
    internal val saveManyFunction = provideSaveManyFunction()
    internal val saveFunction = { model: T -> saveManyFunction.invoke(listOf(model))[0] }

    abstract fun dao(): EntityDao<in T>

    abstract fun createOrUpdate(model: T): Single<T>

    abstract operator fun get(id: String): Flowable<T>

    abstract fun delete(model: T): Single<T>

    internal abstract fun provideSaveManyFunction(): (List<T>) -> List<T>

    operator fun get(model: T): Flowable<T> = when {
        model.isEmpty -> Flowable.error(IllegalArgumentException("Model does not exist"))
        else -> get(model.id).map(getLocalUpdateFunction(model))
    }

    @SuppressLint("CheckResult")
    fun queueForLocalDeletion(model: T) {
        Completable.fromRunnable { deleteLocally(model) }
                .subscribeOn(Schedulers.io())
                .subscribe({ }, ErrorHandler.EMPTY::invoke)
    }

    internal fun getLocalUpdateFunction(original: T): (T) -> T = { original.update(it); original }

    internal fun saveAsNested(): (List<T>) -> List<T> = inner@{ models ->
        if (models.isEmpty()) return@inner models

        val parted = models.partition { it.isEmpty || it.hasMajorFields() }

        saveManyFunction.invoke(parted.first) // Nested save empty models or complete ones with upsert
        dao().insert(parted.second) // For non empty models with incomplete data, insert, if a copy exists, it will be rejected

        models
    }


    internal fun fetchThenGetModel(localSource: Maybe<T>, remoteSource: Maybe<T>): Flowable<T> {
        var local = localSource
        var remote = remoteSource
        val reference = AtomicReference<T>()
        local = local.doOnSuccess(reference::set)
        remote = remote.doOnError { throwable -> deleteInvalidModel(reference.get(), throwable) }

        return fetchThenGet(local, remote)
    }

    internal fun deleteInvalidModel(model: T?, throwable: Throwable) {
        if (model == null || throwable !is HttpException) return

        val message = Message(throwable)
        if (message.isInvalidObject || message.isIllegalTeamMember) deleteLocally(model)
    }

    internal open fun deleteLocally(model: T): T {
        dao().delete(model)
        return model
    }

    internal fun getBody(path: CharSequence?, photoKey: String): MultipartBody.Part? {
        path ?: return null

        val pathString = path.toString()
        val file = File(pathString)

        if (!file.exists()) return null
        val extension = MimeTypeMap.getFileExtensionFromUrl(pathString) ?: return null

        val type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: return null

        val requestBody = RequestBody.create(MediaType.parse(type), file)

        return MultipartBody.Part.createFormData(photoKey, file.name, requestBody)
    }

    companion object {

        internal const val DEF_QUERY_LIMIT = 12

        internal fun <R> fetchThenGet(local: Maybe<R>, remote: Maybe<R>): Flowable<R> =
                concatDelayError(listOf(local, remote))
    }

}
