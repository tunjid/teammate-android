package com.mainstreetcode.teammate.viewmodel.gofers;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class StatGofer extends Gofer<Stat> {

    private final List<Identifiable> items;
    private final List<Team> eligibleTeams;
    private final Function<Stat, Single<Stat>> upsertFunction;
    private final Function<Stat, Single<Stat>> deleteFunction;
    private final Function<Stat, Flowable<Team>> eligibleTeamSource;


    public StatGofer(Stat model, Consumer<Throwable> onError,
                     Function<Stat, Single<Stat>> upsertFunction,
                     Function<Stat, Single<Stat>> deleteFunction,
                     Function<Stat, Flowable<Team>> eligibleTeamSource) {
        super(model, onError);
        this.upsertFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.items = makeItems();
        this.eligibleTeamSource = eligibleTeamSource;
        this.eligibleTeams = new ArrayList<>();
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
                .doOnNext(eligibleTeams::add).ignoreElements()
                .doOnComplete(this::updateDefaultTeam);
    }

    @Nullable
    @Override
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
        return Identifiable.diff(sourceSingle, this::getItems, (sourceCopy, fetched) -> {
            User current = model.getUser();
            sourceCopy.remove(current);
            sourceCopy.add(otherUser);
            current.update(otherUser);
            return sourceCopy;
        });
    }

    public Single<DiffUtil.DiffResult> switchTeams() {
        if (eligibleTeams.size() <= 1)
            return Single.error(new TeammateException(App.getInstance().getString(R.string.stat_only_team)));

        Single<List<Identifiable>> sourceSingle = Flowable.fromIterable(eligibleTeams)
                .filter(team -> ! model.getTeam().equals(team)).collect(ArrayList::new, List::add);

        return Identifiable.diff(sourceSingle, this::getItems, (sourceCopy, fetched) -> {
            Team current = model.getTeam();
            Team otherTeam = (Team) fetched.get(0);
            sourceCopy.remove(current);
            sourceCopy.add(otherTeam);
            current.update(otherTeam);
            return sourceCopy;
        });
    }

    public Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }

    private void updateDefaultTeam() {
        Team statTeam = model.getTeam();
        if (!statTeam.isEmpty()) return;

        Team defaultTeam = eligibleTeams.isEmpty() ? null : eligibleTeams.get(0);
        if (defaultTeam == null) return;

        statTeam.update(defaultTeam);
        items.clear();
        items.addAll(makeItems());
        Collections.sort(items, Identifiable.COMPARATOR);
    }

    private List<Identifiable> makeItems() {
        List<Identifiable> result = new ArrayList<>(model.asItems());
        result.add(model.getTeam());
        result.add(model.getUser());
        return result;
    }
}
