package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Event;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.notifications.FeedItem;
import com.mainstreetcode.teammates.repository.EventRepository;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

/**
 * ViewModel for roles in a team
 */

public class FeedViewModel extends ViewModel {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final RoleRepository roleRepository = RoleRepository.getInstance();
    private final EventRepository eventRepository = EventRepository.getInstance();
    private final JoinRequestRepository joinRequestRepository = JoinRequestRepository.getInstance();

    private final List<FeedItem> feedItems = new ArrayList<>();

    public FeedViewModel() {
    }

    public List<FeedItem> getFeedItems() {
        return feedItems;
    }

    public Flowable<DiffUtil.DiffResult> getFeed() {
        Flowable<List<FeedItem>> sourceFlowable = api.getFeed().toFlowable();
        Callable<List<FeedItem>> listCallable = () -> feedItems;

        return Identifiable.diff(sourceFlowable, listCallable, (oldFeed, newFeed) -> {
            Collections.sort(newFeed, (itemA, itemB) -> Model.COMPARATOR.compare(itemA.getModel(), itemB.getModel()));
            return newFeed;
        });
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final FeedItem<Event> feedItem, boolean attending) {
        Flowable<List<FeedItem>> sourceFlowable = eventRepository.rsvpEvent(feedItem.getModel(), attending)
                .map(model -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .toFlowable();

        return Identifiable.diff(sourceFlowable, () -> feedItems, onFeedItemProcessed()).firstOrError();
    }

    public Single<DiffUtil.DiffResult> processJoinRequest(FeedItem<JoinRequest> feedItem, boolean approved) {
        Flowable<List<FeedItem>> sourceFlowable = (approved
                ? roleRepository.approveUser(feedItem.getModel())
                : joinRequestRepository.dropJoinRequest(feedItem.getModel()))
                .map(model -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .toFlowable();

        final Callable<List<FeedItem>> listCallable = () -> feedItems;

        return Identifiable.diff(sourceFlowable, listCallable, onFeedItemProcessed()).firstOrError();
    }

    private BiFunction<List<FeedItem>, List<FeedItem>, List<FeedItem>> onFeedItemProcessed() {
        return (feedItems, processed) -> {
            feedItems.removeAll(processed);
            return feedItems;
        };
    }
}
