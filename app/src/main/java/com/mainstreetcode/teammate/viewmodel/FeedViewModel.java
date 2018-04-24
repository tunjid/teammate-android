package com.mainstreetcode.teammate.viewmodel;

import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.notifications.FeedItem;
import com.mainstreetcode.teammate.repository.GuestRepository;
import com.mainstreetcode.teammate.repository.JoinRequestRepository;
import com.mainstreetcode.teammate.repository.TeamMemberRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

public class FeedViewModel extends MappedViewModel<Class<FeedItem>, FeedItem> {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final GuestRepository guestRepository = GuestRepository.getInstance();
    private final JoinRequestRepository joinRequestRepository = JoinRequestRepository.getInstance();
    private final TeamMemberRepository<JoinRequest> memberRepository = TeamMemberRepository.getInstance();

    private final List<Identifiable> feedItems = new ArrayList<>();

    public FeedViewModel() {}

    @Override
    public List<Identifiable> getModelList(Class<FeedItem> key) {
        return feedItems;
    }

    @Override
    boolean sortsAscending() {
        return true;
    }

    @Override
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        if (!(alert instanceof Alert.JoinRequestProcessed)) return;
        JoinRequest request = ((Alert.JoinRequestProcessed) alert).getModel();
        removedProcessedRequest(request);
    }

    @Override
    Pair<Model, Class> notificationCancelMap(Identifiable identifiable) {
        if (!(identifiable instanceof FeedItem)) return new Pair<>(null, null);
        FeedItem feedItem = (FeedItem) identifiable;
        return new Pair<>(feedItem.getModel(), feedItem.getItemClass());
    }

    @Override
    Flowable<List<FeedItem>> fetch(Class<FeedItem> key, boolean fetchLatest) {
        return api.getFeed().toFlowable();
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(final FeedItem<Event> feedItem, boolean attending) {
        Flowable<List<Identifiable>> sourceFlowable = guestRepository.createOrUpdate(Guest.forEvent(feedItem.getModel(), attending))
                .map(model -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .toFlowable().map(toIdentifiable);

        return Identifiable.diff(sourceFlowable, () -> feedItems, onFeedItemProcessed(false)).firstOrError();
    }

    public Single<DiffUtil.DiffResult> processJoinRequest(FeedItem<JoinRequest> feedItem, boolean approved) {
        JoinRequest request = feedItem.getModel();

        boolean isOwner = UserRepository.getInstance().getCurrentUser().equals(request.getUser());
        boolean leaveUnchanged = approved && request.isUserApproved() && isOwner;

        Single<? extends Model> sourceSingle = leaveUnchanged
                ? Single.just(request)
                : approved && request.isTeamApproved()
                ? memberRepository.createOrUpdate(TeamMember.fromModel(request))
                : approved && request.isUserApproved()
                ? memberRepository.createOrUpdate(TeamMember.fromModel(request))
                : joinRequestRepository.delete(request);

        Flowable<List<Identifiable>> sourceFlowable = sourceSingle
                .map(model -> feedItem)
                .cast(FeedItem.class)
                .map(Collections::singletonList)
                .toFlowable().map(toIdentifiable);

        return Identifiable.diff(sourceFlowable, () -> feedItems, onFeedItemProcessed(leaveUnchanged)).firstOrError();
    }

    private BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>> onFeedItemProcessed(boolean leaveUnchanged) {
        if (leaveUnchanged) return (feedItems, ignored) -> feedItems;

        return (feedItems, processed) -> {
            feedItems.removeAll(processed);
            return feedItems;
        };
    }

    private void removedProcessedRequest(JoinRequest request) {
        Iterator<Identifiable> iterator = feedItems.iterator();
        while (iterator.hasNext()) {
            Identifiable identifiable = iterator.next();
            if (!(identifiable instanceof FeedItem)) continue;

            Model model = ((FeedItem) identifiable).getModel();
            if (model instanceof JoinRequest && model.equals(request)) {
                iterator.remove();
            }
        }
    }
}
