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

package com.mainstreetcode.teammate.util


import android.os.HandlerThread
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.Item
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Media
import com.mainstreetcode.teammate.model.Role
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.TeamMember
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.notifications.FeedItem
import com.tunjid.androidbootstrap.functions.collections.Lists
import com.tunjid.androidbootstrap.recyclerview.diff.Diff
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

object FunctionalDiff {

    private val diffThread: HandlerThread = HandlerThread("Diffing").apply { start() }

    val COMPARATOR = Comparator<Differentiable> comparing@{ modelA, modelB ->
        val pointsA = getPoints(modelA)
        val pointsB = getPoints(modelB)

        var a: Int
        val b: Int
        val modelComparison: Int

        modelComparison = pointsA.compareTo(pointsB)
        b = modelComparison
        a = b

        @Suppress("UNCHECKED_CAST")
        if (!isComparable(modelA, modelB)) return@comparing modelComparison
        else a += (modelA as Comparable<Any>).compareTo(modelB)

        a.compareTo(b)
    }

    val DESCENDING_COMPARATOR = Comparator<Differentiable> { modelA, modelB -> -COMPARATOR.compare(modelA, modelB) }

    fun <T : Differentiable> of(sourceFlowable: Flowable<out List<T>>,
                                original: List<T>,
                                accumulator: (List<T>, List<T>) -> List<T>): Flowable<DiffUtil.DiffResult> =
            sourceFlowable.concatMapDelayError { list ->
                Flowable.fromCallable { Diff.calculate(original, list, accumulator) }
                        .subscribeOn(AndroidSchedulers.from(diffThread.looper))
                        .observeOn(mainThread())
                        .doOnNext { diff -> Lists.replace(original, diff.items) }
                        .map { diff -> diff.result }
            }

    fun <T : Differentiable> of(sourceSingle: Single<out List<T>>,
                                original: List<T>,
                                accumulator: (List<T>, List<T>) -> List<T>): Single<DiffUtil.DiffResult> =
            sourceSingle.flatMap { list ->
                Single.fromCallable { Diff.calculate(original, list, accumulator) }
                        .subscribeOn(AndroidSchedulers.from(diffThread.looper))
                        .observeOn(mainThread())
                        .doOnSuccess { diff -> Lists.replace(original, diff.items) }
                        .map { diff -> diff.result }
            }

    private fun getPoints(identifiable: Differentiable): Int {
        val it = when (identifiable) {
            is FeedItem<*> -> identifiable.model
            is TeamMember -> identifiable.wrappedModel
            else -> identifiable
        }

        return when (it.javaClass) {
            Item::class.java -> 0
            JoinRequest::class.java -> 5
            Competitor::class.java -> 10
            Role::class.java -> 15
            Event::class.java -> 20
            Media::class.java -> 25
            Team::class.java -> 30
            User::class.java -> 35
            Guest::class.java -> 40
            else -> 0
        }
    }

    private fun isComparable(modelA: Differentiable, modelB: Differentiable): Boolean =
            modelA is Comparable<*> && modelB is Comparable<*> && modelA.javaClass == modelB.javaClass
}
