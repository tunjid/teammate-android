package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import android.location.Address;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Team;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class TeamGofer extends TeamHostingGofer<Team> {

    private static final int CREATING = 0;
    private static final int EDITING = 1;

    private int state;
    private boolean isSettingAddress;
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

        items.addAll(model.asItems());
        state = model.isEmpty() ? CREATING : EDITING;
    }

    public void setSettingAddress(boolean settingAddress) {
        isSettingAddress = settingAddress;
    }

    public boolean isSettingAddress() {
        return isSettingAddress;
    }

    public boolean canEditTeam() {
        return state == CREATING || hasPrivilegedRole();
    }

    public Completable delete() {
        return deleteFunction.apply(model).toCompletable();
    }

    Flowable<DiffUtil.DiffResult> fetch() {
        if (isSettingAddress) return Flowable.empty();
        Flowable<List<Differentiable>> source = getFunction.apply(model).map(Team::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Differentiable>> source = upsertFunction.apply(model).map(Team::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated).doOnSuccess(ignored -> state = EDITING);
    }

    public Single<DiffUtil.DiffResult> setAddress(Address address) {
        isSettingAddress = true;
        model.setAddress(address);
        Single<List<Differentiable>> source = Single.just(model.asDifferentiables());
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated).doFinally(() -> isSettingAddress = false);
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
}
