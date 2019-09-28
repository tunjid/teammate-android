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
import com.mainstreetcode.teammate.App
import com.mainstreetcode.teammate.MediaTransferIntentService
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.repository.MediaRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.ErrorHandler
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.asDifferentiables
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.tunjid.androidx.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers.io
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class MediaViewModel : TeamMappedViewModel<Media>() {

    private val repository: MediaRepo
    private val selectionMap: MutableMap<Team, MutableSet<Media>>
    private val uploadCompletionProcessor: PublishProcessor<DiffUtil.DiffResult>

    init {
        selectionMap = HashMap()
        repository = RepoProvider.forRepo(MediaRepo::class.java)
        uploadCompletionProcessor = PublishProcessor.create()
    }

    override fun hasNativeAds(): Boolean = false

    override fun valueClass(): Class<Media> = Media::class.java

    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        //noinspection unchecked,ResultOfMethodCallIgnored
        alert.matches(Alert.of(Alert.Creation::class.java, Media::class.java) { media ->

            val single = Single.fromCallable<Differentiable> { media }
                    .subscribeOn(io())
                    .map { listOf(it) }

            FunctionalDiff.of(single, getModelList(media.team), this::preserveList)
                    .subscribe({ uploadCompletionProcessor.onNext(it) }, ErrorHandler.EMPTY::invoke)
        })
    }

    override fun fetch(key: Team, fetchLatest: Boolean): Flowable<List<Media>> =
            repository.modelsBefore(key, getQueryDate(fetchLatest, key, Media::created))

    fun getMedia(model: Media): Flowable<Media> =
            checkForInvalidObject(repository[model], model.team, model).cast(Media::class.java)
                    .doOnNext { media -> if (media.isFlagged) getModelList(media.team).remove(media) }

    fun listenForUploads(): Flowable<DiffUtil.DiffResult> = uploadCompletionProcessor

    fun deleteMedia(team: Team, isAdmin: Boolean): Maybe<Pair<Boolean, DiffUtil.DiffResult>> {
        val partialDelete = AtomicBoolean()
        val source = getModelList(team)

        val toDelete = selectionMap[team]?.let { ArrayList(it) }

        if (toDelete == null || toDelete.isEmpty()) return Maybe.empty()

        val sourceFlowable = when {
            isAdmin -> repository.privilegedDelete(team, toDelete)
            else -> repository.ownerDelete(toDelete)
        }.toFlowable().map( ::asDifferentiables)

        return FunctionalDiff.of(sourceFlowable, source) { sourceCopy, deleted ->
            partialDelete.set(deleted.size != toDelete.size)
            sourceCopy - deleted
        }
                .map { diffResult -> partialDelete.get() to diffResult }
                .firstElement()
                .doOnSuccess { clearSelections(team) }
    }

    fun downloadMedia(team: Team): Boolean {
        val toDownload = selectionMap[team]?.let { ArrayList(it) }
        if (toDownload == null || toDownload.isEmpty()) return false

        MediaTransferIntentService.startActionDownload(App.instance, toDownload)
        clearSelections(team)
        return true
    }

    fun flagMedia(model: Media): Single<Media> =
            checkForInvalidObject(repository.flag(model).toFlowable(), model.team, model)
                    .firstOrError().cast(Media::class.java).doOnSuccess { getModelList(model.team).remove(it) }

    fun clearSelections(team: Team) {
        selectionMap[team]?.clear()
    }

    fun hasSelections(team: Team): Boolean = getNumSelected(team) != 0

    fun getNumSelected(team: Team): Int = selectionMap[team]?.size ?: 0

    fun isSelected(media: Media): Boolean = selectionMap[media.team]?.contains(media) ?: false

    fun select(media: Media): Boolean = selectionMap.getOrPut(media.team) { mutableSetOf() }.let {
        when {
            it.contains(media) -> it.remove(media).not()
            else -> it.add(media)
        }
    }

}
