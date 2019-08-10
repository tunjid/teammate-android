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
import com.mainstreetcode.teammate.model.Competitor
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Guest
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.model.Model
import com.mainstreetcode.teammate.model.toTeamMember
import com.mainstreetcode.teammate.notifications.FeedItem
import com.mainstreetcode.teammate.repository.CompetitorRepo
import com.mainstreetcode.teammate.repository.GuestRepo
import com.mainstreetcode.teammate.repository.RepoProvider
import com.mainstreetcode.teammate.repository.TeamMemberRepo
import com.mainstreetcode.teammate.repository.UserRepo
import com.mainstreetcode.teammate.rest.TeammateService
import com.mainstreetcode.teammate.util.FunctionalDiff
import com.mainstreetcode.teammate.util.asDifferentiables
import com.mainstreetcode.teammate.viewmodel.events.Alert
import com.mainstreetcode.teammate.viewmodel.events.matches
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable
import io.reactivex.Flowable
import io.reactivex.Single

class FeedViewModel : MappedViewModel<Class<FeedItem<*>>, FeedItem<*>>() {

    private val api = TeammateService.getApiInstance()

    private val guestRepository = RepoProvider.forRepo(GuestRepo::class.java)
    private val teamMemberRepository = RepoProvider.forRepo(TeamMemberRepo::class.java)
    private val competitorRepository = RepoProvider.forRepo(CompetitorRepo::class.java)

    private val feedItems = mutableListOf<Differentiable>()

    override fun sortsAscending(): Boolean = true

    override fun getModelList(key: Class<FeedItem<*>>): MutableList<Differentiable> = feedItems

    override fun valueClass(): Class<FeedItem<*>> = FeedItem::class.java

    override fun onModelAlert(alert: Alert<*>) {
        super.onModelAlert(alert)

        alert.matches(Alert.of(Alert.JoinRequestProcessed::class.java, JoinRequest::class.java, this::removedProcessedRequest))
    }

    override fun itemToModel(identifiable: Differentiable): Model<*>? = when (identifiable) {
        is FeedItem<*> -> identifiable.model
        else -> null
    }

    override fun fetch(key: Class<FeedItem<*>>, fetchLatest: Boolean): Flowable<List<FeedItem<*>>> =
            api.feed.toFlowable()

    fun rsvpEvent(feedItem: FeedItem<Event>, attending: Boolean): Single<DiffUtil.DiffResult> {
        val sourceFlowable = guestRepository.createOrUpdate(Guest.forEvent(feedItem.model, attending))
                .map { feedItem }
                .cast(FeedItem::class.java)
                .map { listOf(it) }
                .toFlowable()
                .map(List<Differentiable>::asDifferentiables)

        return FunctionalDiff.of(sourceFlowable, feedItems, onFeedItemProcessed(false)).firstOrError()
    }

    fun processCompetitor(feedItem: FeedItem<Competitor>, accepted: Boolean): Single<DiffUtil.DiffResult> {
        val model = feedItem.model
        if (accepted) model.accept()
        else model.decline()

        val sourceFlowable = competitorRepository.createOrUpdate(model)
                .map { feedItem }
                .cast(FeedItem::class.java)
                .map { listOf(it) }
                .toFlowable().map(List<Differentiable>::asDifferentiables)

        return FunctionalDiff.of(sourceFlowable, feedItems, onFeedItemProcessed(false)).firstOrError()
    }

    fun processJoinRequest(feedItem: FeedItem<JoinRequest>, approved: Boolean): Single<DiffUtil.DiffResult> {
        val request = feedItem.model

        val isOwner = RepoProvider.forRepo(UserRepo::class.java).currentUser == request.user
        val leaveUnchanged = approved && request.isUserApproved && isOwner
        val member = request.toTeamMember()

        val sourceSingle = when {
            leaveUnchanged -> Single.just(member)
            approved && request.isTeamApproved -> teamMemberRepository.createOrUpdate(member)
            approved && request.isUserApproved -> teamMemberRepository.createOrUpdate(member)
            else -> teamMemberRepository.delete(member)
        }

        val sourceFlowable = checkForInvalidObject(sourceSingle, FeedItem::class.java, feedItem)
                .map { feedItem }
                .map { listOf(it) }
                .map(List<Differentiable>::asDifferentiables)

        return FunctionalDiff.of(sourceFlowable, feedItems, onFeedItemProcessed(leaveUnchanged))
    }

    private fun onFeedItemProcessed(leaveUnchanged: Boolean): (MutableList<Differentiable>, MutableList<Differentiable>) -> MutableList<Differentiable> = when {
        leaveUnchanged -> { feedItems, _ -> feedItems }
        else -> { feedItems, processed -> feedItems.removeAll(processed); feedItems }
    }

    private fun removedProcessedRequest(request: JoinRequest) {
        val iterator = feedItems.iterator()
        while (iterator.hasNext()) {
            val identifiable = iterator.next() as? FeedItem<*> ?: continue

            val model = identifiable.model
            if (model is JoinRequest && model == request) iterator.remove()
        }
    }
}
