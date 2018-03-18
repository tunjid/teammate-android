package com.mainstreetcode.teammate.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.repository.EventRepository;
import com.mainstreetcode.teammate.repository.JoinRequestRepository;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

/**
 * ViewModel for roles in a team
 */

public class FeedViewModel extends MappedViewModel<Class<FeedItem>, FeedItem> {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final RoleRepository roleRepository = RoleRepository.getInstance();
    private final EventRepository eventRepository = EventRepository.getInstance();
    private final JoinRequestRepository joinRequestRepository = JoinRequestRepository.getInstance();

    private final List<Identifiable> feedItems = new ArrayList<>();

    public FeedViewModel() {
    }

    @Override
    public List<Identifiable> getModelList(Class<FeedItem> key) {
        return feedItems;
    }

    @Override
    <T extends Model<T>> List<Class<T>> notifiedClasses() {return Collections.emptyList();}

    @Override
    Flowable<List<FeedItem>> fetch(Class<FeedItem> key, boolean fetchLatest) {
        return api.getFeed().toFlowable();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final FeedItem<Event> feedItem, boolean attending) {
        Flowable<List<Identifiable>> sourceFlowable = eventRepository.rsvpEvent(feedItem.getModel(), attending)
                .map(model -> Collections.singletonList(dropType(feedItem)))
                .toFlowable().map(toIdentifiable);

        return Identifiable.diff(sourceFlowable, () -> feedItems, onFeedItemProcessed()).firstOrError();
    }

    public Single<DiffUtil.DiffResult> processJoinRequest(FeedItem<JoinRequest> feedItem, boolean approved) {
        JoinRequest request = feedItem.getModel();
        Single<Role> roleSingle = (request.isTeamApproved()
                ? roleRepository.acceptInvite(request)
                : roleRepository.approveUser(request));

        Flowable<List<Identifiable>> sourceFlowable = (approved
                ? roleSingle
                : joinRequestRepository.delete(request))
                .map(model -> Collections.singletonList(dropType(feedItem)))
                .toFlowable().map(toIdentifiable);

        return Identifiable.diff(sourceFlowable, () -> feedItems, onFeedItemProcessed()).firstOrError();
    }

    private <T extends Model<T>> FeedItem dropType(FeedItem<T> feedItem) { return feedItem;}

    private BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>> onFeedItemProcessed() {
        return (feedItems, processed) -> {
            feedItems.removeAll(processed);
            return feedItems;
        };
    }
}
