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

import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.repository.CompetitorRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

import java.util.ArrayList
import java.util.Collections

import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

import io.reactivex.android.schedulers.AndroidSchedulers.mainThread

/**
 * View model for User and Auth
 */

class CompetitorViewModel : MappedViewModel<Class<User>, Competitor>() {

    private val repository: CompetitorRepo = RepoProvider.forRepo(CompetitorRepo::class.java)
    private val declined = ArrayList<Differentiable>()

    override fun valueClass(): Class<Competitor> {
        return Competitor::class.java
    }

    override fun getModelList(key: Class<User>): MutableList<Differentiable> {
        return declined
    }

    fun updateCompetitor(competitor: Competitor): Completable {
        return if (competitor.isEmpty) Completable.complete() else repository[competitor].ignoreElements().observeOn(mainThread())
    }

    fun respond(competitor: Competitor, accept: Boolean): Single<DiffUtil.DiffResult> {
        if (accept) competitor.accept()
        else competitor.decline()

        val single = repository.createOrUpdate(competitor).map<List<Differentiable>>({ listOf(it) })

        return FunctionalDiff.of(single, declined) biFunction@{ sourceCopy, fetched ->
            if (accept) sourceCopy.removeAll(fetched)
            else sourceCopy.addAll(fetched)

            if (accept) return@biFunction sourceCopy

            pushModelAlert(when {
                competitor.inOneOffGame() -> Alert.deletion(competitor.game)
                else -> Alert.deletion(competitor.tournament)
            })

            return@biFunction sourceCopy
        }
    }

    override fun fetch(key: Class<User>, fetchLatest: Boolean): Flowable<List<Competitor>> {
        return repository.getDeclined(getQueryDate(fetchLatest, key, { it.created })!!).toFlowable()
    }
}
