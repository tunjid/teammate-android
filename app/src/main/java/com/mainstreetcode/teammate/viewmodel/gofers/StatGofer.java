package com.mainstreetcode.teammate.viewmodel.gofers;

import android.arch.core.util.Function;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.Supplier;
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

    private final List<Identifiable> items;
    private final List<Team> eligibleTeams;
    private final Function<Team, User> teamUserFunction;
    private final Function<Stat, Single<Stat>> upsertFunction;
    private final Function<Stat, Single<Stat>> deleteFunction;
    private final Function<Stat, Flowable<Team>> eligibleTeamSource;


    public StatGofer(Stat model, Consumer<Throwable> onError,
                     Function<Team, User> teamUserFunction,
                     Function<Stat, Single<Stat>> upsertFunction,
                     Function<Stat, Single<Stat>> deleteFunction,
                     Function<Stat, Flowable<Team>> eligibleTeamSource) {
        super(model, onError);
        this.teamUserFunction = teamUserFunction;
        this.upsertFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.eligibleTeamSource = eligibleTeamSource;

        this.eligibleTeams = new ArrayList<>();
        this.items = new ArrayList<>(model.asItems());
        items.add(model.getTeam());
        items.add(model.getUser());
    }

    public List<Identifiable> getItems() {
        return items;
    }

    public boolean canEdit() {
        return !eligibleTeams.isEmpty();
    }

    @Override
    public Completable prepare() {
        eligibleTeams.clear();
        return Flowable.defer(() -> eligibleTeamSource.apply(model))
                .doOnNext(eligibleTeams::add).ignoreElements().andThen(updateDefaultTeam());
    }

    @Override
    @Nullable
    public String getImageClickMessage(Fragment fragment) {
        return null;
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        return Flowable.empty();
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Identifiable>> source = Single.defer(() -> upsertFunction.apply(model)).map(Stat::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
    }

    public Single<DiffUtil.DiffResult> chooseUser(User otherUser) {
        Single<List<Identifiable>> sourceSingle = Single.just(Collections.singletonList(otherUser));
        return swap(sourceSingle, model::getUser, User::update);
    }

    public Flowable<DiffUtil.DiffResult> switchTeams() {
        if (eligibleTeams.size() <= 1)
            return Flowable.error(new TeammateException(App.getInstance().getString(R.string.stat_only_team)));

        Single<List<Identifiable>> sourceSingle = Flowable.fromIterable(eligibleTeams)
                .filter(team -> !model.getTeam().equals(team)).collect(ArrayList::new, List::add);

        return swap(sourceSingle, model::getTeam, Team::update).concatWith(chooseUser(teamUserFunction.apply(model.getTeam())));
    }

    public Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }

    private Completable updateDefaultTeam() {
        return Completable.defer(() -> {
            boolean hasNoDefaultTeam = !model.getTeam().isEmpty() || eligibleTeams.isEmpty();
            if (hasNoDefaultTeam) return Completable.complete();
            Single<List<Identifiable>> sourceSingle = Single.just(Collections.singletonList(eligibleTeams.get(0)));
            return swap(sourceSingle, model::getTeam, Team::update).toCompletable();
        });
    }

    @SuppressWarnings("unchecked")
    private <T extends Identifiable> Single<DiffUtil.DiffResult> swap(Single<List<Identifiable>> swapSource,
                                                                      Supplier<T> swapDestination,
                                                                      BiConsumer<T, T> onSwapComplete) {

        AtomicReference<T> cache = new AtomicReference<>();
        return Identifiable.diff(swapSource, this::getItems, (sourceCopy, fetched) -> {
            T toSwap = (T) fetched.get(0);
            sourceCopy.remove(swapDestination.get());
            sourceCopy.add(toSwap);
            cache.set(toSwap);

            Collections.sort(sourceCopy, Identifiable.COMPARATOR);
            return sourceCopy;
        }).doOnSuccess(ignored -> onSwapComplete.accept(swapDestination.get(), cache.get()));
    }
}
