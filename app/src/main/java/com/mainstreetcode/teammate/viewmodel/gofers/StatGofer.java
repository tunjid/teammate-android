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

package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.tunjid.androidbootstrap.functions.Supplier;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class StatGofer extends Gofer<Stat> {

    private final List<Team> eligibleTeams;
    private final Function<Team, User> teamUserFunction;
    private final Function<Stat, Flowable<Stat>> getFunction;
    private final Function<Stat, Single<Stat>> upsertFunction;
    private final Function<Stat, Single<Stat>> deleteFunction;
    private final Function<Stat, Flowable<Team>> eligibleTeamSource;


    public StatGofer(Stat model, Consumer<Throwable> onError,
                     Function<Team, User> teamUserFunction,
                     Function<Stat, Flowable<Stat>> getFunction,
                     Function<Stat, Single<Stat>> upsertFunction,
                     Function<Stat, Single<Stat>> deleteFunction,
                     Function<Stat, Flowable<Team>> eligibleTeamSource) {
        super(model, onError);
        this.teamUserFunction = teamUserFunction;
        this.getFunction = getFunction;
        this.upsertFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.eligibleTeamSource = eligibleTeamSource;

        this.eligibleTeams = new ArrayList<>();
        items.addAll(model.asItems());
        items.add(model.getTeam());
        items.add(model.getUser());
    }

    public boolean canEdit() {
        return !eligibleTeams.isEmpty();
    }

    @Override
    Flowable<Boolean> changeEmitter() {
        int count = eligibleTeams.size();
        eligibleTeams.clear();

        return eligibleTeamSource.apply(model)
                .collectInto(eligibleTeams, List::add)
                .flatMapPublisher(this::updateDefaultTeam)
                .map(ignored -> count != eligibleTeams.size());
    }

    @Override
    @Nullable
    public String getImageClickMessage(Fragment fragment) {
        return null;
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Differentiable>> source = getFunction.apply(model).map(Stat::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), this::preserveItems);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Differentiable>> source = upsertFunction.apply(model).map(Stat::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), this::preserveItems);
    }

    public Single<DiffUtil.DiffResult> chooseUser(User otherUser) {
        return swap(otherUser, model::getUser, User::update);
    }

    public Flowable<DiffUtil.DiffResult> switchTeams() {
        if (eligibleTeams.size() <= 1)
            return Flowable.error(new TeammateException(App.getInstance().getString(R.string.stat_only_team)));

        Team toSwap = eligibleTeams.get(0).equals(model.getTeam()) ? eligibleTeams.get(1) : eligibleTeams.get(0);
        return swap(toSwap, model::getTeam, Team::update).concatWith(updateDefaultUser());
    }

    public Completable delete() {
        return deleteFunction.apply(model).toCompletable();
    }

    private Flowable<DiffUtil.DiffResult> updateDefaultTeam(List<Team> teams) {
        boolean hasNoDefaultTeam = !model.getTeam().isEmpty() || teams.isEmpty();
        if (hasNoDefaultTeam) return Flowable.empty();

        Team toSwap = teams.get(0);
        return swap(toSwap, model::getTeam, Team::update).concatWith(updateDefaultUser());
    }

    private Single<DiffUtil.DiffResult> updateDefaultUser() {
        return chooseUser(teamUserFunction.apply(model.getTeam()));
    }

    @SuppressWarnings("unchecked")
    private <T extends Differentiable> Single<DiffUtil.DiffResult> swap(Differentiable item,
                                                                      Supplier<T> swapDestination,
                                                                      BiConsumer<T, T> onSwapComplete) {

        AtomicReference<T> cache = new AtomicReference<>();
        Single<List<Differentiable>> swapSource = Single.just(Collections.singletonList(item));
        return FunctionalDiff.of(swapSource, getItems(), (sourceCopy, fetched) -> {
            T toSwap = (T) fetched.get(0);
            sourceCopy.remove(swapDestination.get());
            sourceCopy.add(toSwap);
            cache.set(toSwap);

            Collections.sort(sourceCopy, FunctionalDiff.COMPARATOR);
            return sourceCopy;
        }).doOnSuccess(ignored -> onSwapComplete.accept(swapDestination.get(), cache.get()));
    }
}
