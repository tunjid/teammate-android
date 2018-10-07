package com.mainstreetcode.teammate.viewmodel.gofers;

import android.arch.core.util.Function;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class GameGofer extends Gofer<Game> {

    private final List<Team> eligibleTeams;
    private final Function<Game, Flowable<Game>> getFunction;
    private final Function<Game, Single<Game>> upsertFunction;
    private final Function<Game, Single<Game>> deleteFunction;
    private final Function<Game, Flowable<Team>> eligibleTeamSource;


    public GameGofer(Game model, Consumer<Throwable> onError,
                     Function<Game, Flowable<Game>> getFunction,
                     Function<Game, Single<Game>> upsertFunction,
                     Function<Game, Single<Game>> deleteFunction,
                     Function<Game, Flowable<Team>> eligibleTeamSource) {
        super(model, onError);
        this.getFunction = getFunction;
        this.upsertFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.eligibleTeamSource = eligibleTeamSource;

        this.eligibleTeams = new ArrayList<>();
        this.items.addAll(model.isEmpty()
                ? Arrays.asList(model.getHome(), model.getAway())
                : model.asItems());
    }

    public boolean canEdit() {
        boolean canEdit = !model.isEnded() && !eligibleTeams.isEmpty() && !model.competitorsNotAccepted();
        return model.isEmpty() || canEdit;
    }

    public boolean canDelete(User user) {
        if (!model.getTournament().isEmpty()) return false;
        if (model.getHome().isEmpty()) return true;
        if (model.getAway().isEmpty()) return true;

        Competitive entity = model.getHome().getEntity();
        if (entity.equals(user)) return true;
        for (Team team : eligibleTeams) if (entity.equals(team)) return true;

        return false;
    }

    @Override
    Flowable<Boolean> changeEmitter() {
        int count = eligibleTeams.size();
        eligibleTeams.clear();
        return eligibleTeamSource.apply(model)
                .doOnNext(eligibleTeams::add).ignoreElements()
                .andThen(Flowable.just(count != eligibleTeams.size()));
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Identifiable>> source = getFunction.apply(model).map(Game::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
    }

    public Completable delete() {
        return Single.defer(() -> deleteFunction.apply(model)).toCompletable();
    }

    @Override
    @Nullable
    public String getImageClickMessage(Fragment fragment) {
        return null;
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Identifiable>> source = upsertFunction.apply(model).map(Game::asIdentifiables);
        return Identifiable.diff(source, this::getItems, this::preserveItems);
    }

    @Override
    List<Identifiable> preserveItems(List<Identifiable> old, List<Identifiable> fetched) {
        List<Identifiable> result = super.preserveItems(old, fetched);
        Iterator<Identifiable> iterator = result.iterator();
        Function<Identifiable, Boolean> filter = item -> item instanceof Competitor && ((Competitor) item).isEmpty();

        int currentSize = result.size();
        while (iterator.hasNext()) if (filter.apply(iterator.next())) iterator.remove();

        if (currentSize == result.size() || model.isEmpty()) return result;
        if (currentSize != model.asItems().size()) return result;

        result.add(model.getHome());
        result.add(model.getAway());

        return result;
    }
}
