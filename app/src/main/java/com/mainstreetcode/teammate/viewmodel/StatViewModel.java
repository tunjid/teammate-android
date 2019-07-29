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

package com.mainstreetcode.teammate.viewmodel;

import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.model.Game;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.StatAggregate;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.StatRepo;
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

    private final StatRepo repository;

    public StatViewModel() { repository = RepoProvider.forRepo(StatRepo.class); }

    public StatGofer gofer(Stat stat) {
        Consumer<Throwable> onError = throwable -> checkForInvalidObject(throwable, stat, stat.getGame());
        Function<Team, User> userFunction = team -> RepoProvider.forRepo(UserRepo.class).getCurrentUser();
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
        pushModelAlert(Alert.deletion(key));
    }

    public List<Differentiable> getStatAggregates() {
        return aggregates;
    }

    public Single<Stat> delete(final Stat stat) {
        return repository.delete(stat).doOnSuccess(deleted -> getModelList(stat.getGame()).remove(deleted));
    }

    public Single<Boolean> canEditGameStats(Game game) {
        User current = RepoProvider.forRepo(UserRepo.class).getCurrentUser();
        return isPrivilegedInGame(game).map(isPrivileged -> isPrivilegedOrIsReferee(isPrivileged, current, game));
    }

    public Single<Boolean> isPrivilegedInGame(Game game) {
        User current = RepoProvider.forRepo(UserRepo.class).getCurrentUser();
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
