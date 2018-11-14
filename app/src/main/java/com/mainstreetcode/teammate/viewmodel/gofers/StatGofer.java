package com.mainstreetcode.teammate.viewmodel.gofers;

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

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
        Flowable<List<Identifiable>> source = getFunction.apply(model).map(Stat::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Identifiable>> source = upsertFunction.apply(model).map(Stat::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
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
    private <T extends Identifiable> Single<DiffUtil.DiffResult> swap(Identifiable item,
                                                                      Supplier<T> swapDestination,
                                                                      BiConsumer<T, T> onSwapComplete) {

        AtomicReference<T> cache = new AtomicReference<>();
        Single<List<Identifiable>> swapSource = Single.just(Collections.singletonList(item));
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
