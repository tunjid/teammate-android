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

package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.repository.CompetitorRepo;
import com.mainstreetcode.teammate.repository.GuestRepo;
import com.mainstreetcode.teammate.repository.JoinRequestRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.TeamMemberRepo;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.tunjid.androidbootstrap.functions.BiFunction;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class FeedViewModel extends MappedViewModel<Class<FeedItem>, FeedItem> {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final GuestRepo guestRepository = RepoProvider.forRepo(GuestRepo.class);
    private final CompetitorRepo competitorRepository = RepoProvider.forRepo(CompetitorRepo.class);
    private final JoinRequestRepo joinRequestRepository = RepoProvider.forRepo(JoinRequestRepo.class);
    @SuppressWarnings("unchecked") private final TeamMemberRepo<JoinRequest> memberRepository = RepoProvider.forRepo(TeamMemberRepo.class);

    private final List<Differentiable> feedItems = new ArrayList<>();

    public FeedViewModel() {}

    @Override
    boolean sortsAscending() {
        return true;
    }

    @Override
    public List<Differentiable> getModelList(Class<FeedItem> key) {
        return feedItems;
    }

    @Override
    Class<FeedItem> valueClass() { return FeedItem.class; }

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);

        //noinspection unchecked
        Alert.matches(alert, Alert.of(Alert.JoinRequestProcessed.class, JoinRequest.class, this::removedProcessedRequest));
    }

    @Override
    Pair<Model, Class> notificationCancelMap(Differentiable identifiable) {
        if (!(identifiable instanceof FeedItem)) return new Pair<>(null, null);
        FeedItem feedItem = (FeedItem) identifiable;
        return new Pair<>(feedItem.getModel(), feedItem.getItemClass());
    }

    @Override
    Flowable<List<FeedItem>> fetch(Class<FeedItem> key, boolean fetchLatest) {
        return api.getFeed().toFlowable();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final FeedItem<Event> feedItem, boolean attending) {
        Flowable<List<Differentiable>> sourceFlowable = guestRepository.createOrUpdate(Guest.forEvent(feedItem.getModel(), attending))
                .map(model -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .toFlowable().map(this::toDifferentiable);

        return FunctionalDiff.of(sourceFlowable, feedItems, onFeedItemProcessed(false)).firstOrError();
    }

    public Single<DiffUtil.DiffResult> processCompetitor(final FeedItem<Competitor> feedItem, boolean accepted) {
        Competitor model = feedItem.getModel();
        if (accepted) model.accept();
        else model.decline();

        Flowable<List<Differentiable>> sourceFlowable = competitorRepository.createOrUpdate(model)
                .map(mapped -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .toFlowable().map(this::toDifferentiable);

        return FunctionalDiff.of(sourceFlowable, feedItems, onFeedItemProcessed(false)).firstOrError();
    }

    public Single<DiffUtil.DiffResult> processJoinRequest(FeedItem<JoinRequest> feedItem, boolean approved) {
        JoinRequest request = feedItem.getModel();

        boolean isOwner = RepoProvider.forRepo(UserRepo.class).getCurrentUser().equals(request.getUser());
        boolean leaveUnchanged = approved && request.isUserApproved() && isOwner;

        Single<? extends Model> sourceSingle = leaveUnchanged
                ? Single.just(request)
                : approved && request.isTeamApproved()
                ? memberRepository.createOrUpdate(TeamMember.fromModel(request))
                : approved && request.isUserApproved()
                ? memberRepository.createOrUpdate(TeamMember.fromModel(request))
                : joinRequestRepository.delete(request);

        Single<List<Differentiable>> sourceFlowable = checkForInvalidObject(sourceSingle, FeedItem.class, feedItem)
                .map(model -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .map(this::toDifferentiable);

        return FunctionalDiff.of(sourceFlowable, feedItems, onFeedItemProcessed(leaveUnchanged));
    }

    private BiFunction<List<Differentiable>, List<Differentiable>, List<Differentiable>> onFeedItemProcessed(boolean leaveUnchanged) {
        if (leaveUnchanged) return (feedItems, ignored) -> feedItems;

        return (feedItems, processed) -> {
            feedItems.removeAll(processed);
            return feedItems;
        };
    }

    private void removedProcessedRequest(JoinRequest request) {
        Iterator<Differentiable> iterator = feedItems.iterator();
        while (iterator.hasNext()) {
            Differentiable identifiable = iterator.next();
            if (!(identifiable instanceof FeedItem)) continue;

            Model model = ((FeedItem) identifiable).getModel();
            if (model instanceof JoinRequest && model.equals(request)) {
                iterator.remove();
            }
        }
    }
}
