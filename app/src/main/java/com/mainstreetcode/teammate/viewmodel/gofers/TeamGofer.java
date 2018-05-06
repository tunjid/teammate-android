package com.mainstreetcode.teammate.viewmodel.gofers;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Team;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class TeamGofer extends TeamHostingGofer<Team> {

    private static final int CREATING = 0;
    private static final int EDITING = 1;

    private int state;
    private final List<Item<Team>> items;
    private final Function<Team, Flowable<Team>> getFunction;
    private final Function<Team, Single<Team>> deleteFunction;
    private final Function<Team, Single<Team>> upsertFunction;

    public TeamGofer(Team model,
                     Consumer<Throwable> onError,
                     Function<Team, Flowable<Team>> getFunction,
                     Function<Team, Single<Team>> upsertFunction,
                     Function<Team, Single<Team>> deleteFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.upsertFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.items = new ArrayList<>(model.asItems());
        state = model.isEmpty() ? CREATING : EDITING;
    }

    public List<Item<Team>> getItems() {
        return items;
    }

    public boolean showsFab() {
        return state == CREATING || hasPrivilegedRole();
    }

   public Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }

     Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Item<Team>>> source = Flowable.defer(() -> getFunction.apply(model)).map(Team::asItems);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

     Single<DiffUtil.DiffResult> upsert() {
        Single<List<Item<Team>>> source = Single.defer(() -> upsertFunction.apply(model)).map(Team::asItems);
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated).doOnSuccess(ignored -> state = EDITING);
    }

    public Single<DiffUtil.DiffResult> setAddress(Address address) {
        model.setAddress(address);
        Single<List<Item<Team>>> source = Single.just(model.<List<Item<Team>>>asItems());
        return Identifiable.diff(source, this::getItems, (itemsCopy, updated) -> updated);
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        if (state == CREATING) return fragment.getString(R.string.create_team_first);
        else if (!hasPrivilegedRole()) return fragment.getString(R.string.no_permission);
        return null;
    }

    public String getToolbarTitle(Fragment fragment) {
        return fragment.getString(state == CREATING ? R.string.create_team : R.string.edit_team);
    }

    @NonNull
    public String getModelUpdateMessage(Fragment fragment) {
        return state == CREATING ? fragment.getString(R.string.created_team, model.getName()) : fragment.getString(R.string.updated_team);
    }
}
