package com.mainstreetcode.teammate.viewmodel;

import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.model.Game;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.StatRepository;
import com.mainstreetcode.teammate.repository.UserRepository;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for {@link Stat tournaments}
 */

public class StatViewModel extends MappedViewModel<Game, Stat> {

    private final TeammateApi api = TeammateService.getApiInstance();

    private final Map<Game, List<Differentiable>> modelListMap = new HashMap<>();
    private final List<Differentiable> aggregates = new ArrayList<>();

    private final StatRepository repository;

    public StatViewModel() {
        repository = StatRepository.getInstance();
    }

    public StatGofer gofer(Stat stat) {
        Consumer<Throwable> onError = throwable -> checkForInvalidObject(throwable, stat, stat.getGame());
        Function<Team, User> userFunction = team -> UserRepository.getInstance().getCurrentUser();
        Function<Stat, Flowable<Team>> eligibleTeamSource = sourceStat -> GameViewModel.getEligibleTeamsForGame(stat.getGame());
        return new StatGofer(stat, onError, userFunction, repository::get, repository::createOrUpdate, this::delete, eligibleTeamSource);
    }

    @Override
    Class<Stat> valueClass() { return Stat.class; }

    @Override
    Flowable<List<Stat>> fetch(Game key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, Stat::getCreated));
    }

    public List<Differentiable> getModelList(Game game) {
        List<Differentiable> modelList = modelListMap.get(game);
        if (!modelListMap.containsKey(game)) modelListMap.put(game, modelList = new ArrayList<>());

        return modelList;
    }

    @Override
    void onInvalidKey(Game key) {
        super.onInvalidKey(key);
        pushModelAlert(Alert.gameDeletion(key));
    }

    public List<Differentiable> getStatAggregates() {
        return aggregates;
    }

    public Single<Stat> delete(final Stat stat) {
        return repository.delete(stat).doOnSuccess(deleted -> getModelList(stat.getGame()).remove(deleted));
    }

    public Single<Boolean> canEditGameStats(Game game) {
        User current = UserRepository.getInstance().getCurrentUser();
        return isPrivilegedInGame(game).map(isPrivileged -> isPrivilegedOrIsReferee(isPrivileged, current, game));
    }

    public Single<Boolean> isPrivilegedInGame(Game game) {
        User current = UserRepository.getInstance().getCurrentUser();
        if (game.betweenUsers()) return Single.just(game.isCompeting(current));
        return GameViewModel.getEligibleTeamsForGame(game).count().map(value -> value > 0);
    }

    public Single<DiffUtil.DiffResult> aggregate(StatAggregate.Request request) {
        Single<List<Differentiable>> sourceSingle = api.statsAggregate(request)
                .map(StatAggregate.Result::getAggregates)
                .map(ArrayList<Differentiable>::new);
        return FunctionalDiff.of(sourceSingle, aggregates, ModelUtils::replaceList).observeOn(mainThread());
    }

    private boolean isPrivilegedOrIsReferee(boolean isPrivileged, User current, Game game) {
        User referee = game.getReferee();
        return (referee.isEmpty() ? isPrivileged : current.equals(referee)) && !game.competitorsNotAccepted();
    }
}
