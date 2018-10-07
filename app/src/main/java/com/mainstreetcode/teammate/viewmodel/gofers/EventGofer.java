package com.mainstreetcode.teammate.viewmodel.gofers;

import android.annotation.SuppressLint;
import android.arch.core.util.Function;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class EventGofer extends TeamHostingGofer<Event> {

    private boolean isSettingLocation;
    private final Function<Guest, Single<Guest>> rsvpFunction;
    private final Function<Event, Flowable<Event>> getFunction;
    private final Function<Event, Single<Event>> deleteFunction;
    private final Function<Event, Single<Event>> updateFunction;
    private final GuestRepository guestRepository;

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public EventGofer(Event model,
                      Consumer<Throwable> onError,
                      Flowable<BlockedUser> blockedUserFlowable,
                      Function<Event, Flowable<Event>> getFunction,
                      Function<Event, Single<Event>> upsertFunction,
                      Function<Event, Single<Event>> deleteFunction,
                      Function<Guest, Single<Guest>> rsvpFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.rsvpFunction = rsvpFunction;
        this.updateFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.guestRepository = GuestRepository.getInstance();

        items.addAll(model.asItems());
        items.add(model.getTeam());

        blockedUserFlowable.subscribe(this::onUserBlocked, ErrorHandler.EMPTY);
    }

    public void setSettingLocation(boolean settingLocation) {
        isSettingLocation = settingLocation;
    }

    public boolean isSettingLocation() {
        return isSettingLocation;
    }

    public String getToolbarTitle(Fragment fragment) {
        return model.isEmpty()
                ? fragment.getString(R.string.create_event)
                : fragment.getString(R.string.edit_event, model.getName());
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        if (hasPrivilegedRole()) return null;
        return fragment.getString(R.string.no_permission);
    }

    @Override
    Flowable<DiffUtil.DiffResult> fetch() {
        if (isSettingLocation) return Flowable.empty();
        Flowable<List<Identifiable>> eventFlowable = getFunction.apply(model).map(Event::asIdentifiables);
        Flowable<List<Identifiable>> guestsFlowable = guestRepository.modelsBefore(model, new Date()).map(ModelUtils::asIdentifiables);
        Flowable<List<Identifiable>> sourceFlowable = Flowable.concatDelayError(Arrays.asList(eventFlowable, guestsFlowable));
        return Identifiable.diff(sourceFlowable, this::getItems, this::preserveItems);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Identifiable>> source = updateFunction.apply(model).map(Event::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
    }

    public Single<DiffUtil.DiffResult> rsvpEvent(boolean attending) {
        Single<List<Identifiable>> single = rsvpFunction.apply(Guest.forEvent(model, attending)).map(Collections::singletonList);
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
        return deleteFunction.apply(model).toCompletable();
    }

    public Single<DiffUtil.DiffResult> setPlace(Place place) {
        isSettingLocation = true;
        model.setPlace(place);
        return Identifiable.diff(Single.just(model.asIdentifiables()), this::getItems, this::preserveItems).doFinally(() -> isSettingLocation = false);
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
