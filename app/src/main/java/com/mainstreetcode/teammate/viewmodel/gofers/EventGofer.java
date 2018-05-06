package com.mainstreetcode.teammate.viewmodel.gofers;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.google.android.gms.location.places.Place;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.GuestRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class EventGofer extends TeamHostingGofer<Event> {

    private final List<Identifiable> items;
    private final Function<Event, Flowable<Event>> getFunction;
    private final Function<Event, Single<Event>> deleteFunction;
    private final Function<Event, Single<Event>> updateFunction;
    private final GuestRepository guestRepository;

    @SuppressLint("CheckResult")
    public EventGofer(Event model,
                      Consumer<Throwable> onError,
                      Flowable<BlockedUser> blockedUserFlowable,
                      Function<Event, Flowable<Event>> getFunction,
                      Function<Event, Single<Event>> upsertFunction,
                      Function<Event, Single<Event>> deleteFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.updateFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.items = new ArrayList<>(model.asItems());
        this.guestRepository = GuestRepository.getInstance();

        items.add(model.getTeam());
        blockedUserFlowable.subscribe(this::onUserBlocked, ErrorHandler.EMPTY);
    }

    public List<Identifiable> getItems() {
        return items;
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        if (hasPrivilegedRole()) return null;
        return fragment.getString(R.string.no_permission);
    }

    @Override
    Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Identifiable>> eventFlowable = Flowable.defer(() -> getFunction.apply(model)).map(Event::asIdentifiables);
        Flowable<List<Identifiable>> guestsFlowable = guestRepository.modelsBefore(model, new Date()).map(ModelUtils::asIdentifiables);
        Flowable<List<Identifiable>> sourceFlowable = Flowable.mergeDelayError(eventFlowable, guestsFlowable);
        return Identifiable.diff(sourceFlowable, this::getItems, this::preserveItems);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Identifiable>> source = Single.defer(() -> updateFunction.apply(model)).map(Event::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(boolean attending) {
        Single<List<Identifiable>> single = guestRepository.createOrUpdate(Guest.forEvent(model, attending)).map(Collections::singletonList);
        return Identifiable.diff(single, this::getItems, (staleCopy, singletonGuestList) -> {
            staleCopy.removeAll(singletonGuestList);
            staleCopy.addAll(singletonGuestList);
            return staleCopy;
        });
    }

    public Single<Integer> getRSVPStatus() {
        return Flowable.fromIterable(items)
                .filter(identifiable -> identifiable instanceof Guest)
                .cast(Guest.class)
                .filter(Guest::isAttending)
                .map(Guest::getUser)
                .filter(getSignedInUser()::equals)
                .collect(ArrayList::new, List::add)
                .map(List::isEmpty)
                .map(notAttending -> notAttending ? R.drawable.ic_event_available_white_24dp : R.drawable.ic_event_busy_white_24dp);
    }

    Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }

    public Single<DiffUtil.DiffResult> setPlace(Place place) {
        model.setPlace(place);
        return Identifiable.diff(Single.just(model.asIdentifiables()), this::getItems, this::preserveItems);
    }

    private List<Identifiable> preserveItems(List<Identifiable> old, List<Identifiable> fetched) {
        ModelUtils.preserveAscending(old, fetched);
        return old;
    }

    private void onUserBlocked(BlockedUser blockedUser) {
        if (!blockedUser.getTeam().equals(model.getTeam())) return;

        Iterator<Identifiable> iterator = items.iterator();

        while (iterator.hasNext()) {
            Identifiable identifiable = iterator.next();
            if (!(identifiable instanceof Guest)) continue;
            User blocked = blockedUser.getUser();
            User guestUser = ((Guest) identifiable).getUser();

            if (blocked.equals((guestUser))) iterator.remove();
        }
    }
}
