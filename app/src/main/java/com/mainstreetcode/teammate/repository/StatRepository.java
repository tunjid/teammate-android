package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.StatDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class StatRepository extends QueryRepository<Stat, Game, Date> {

    private static StatRepository ourInstance;

    private final TeammateApi api;
    private final StatDao statDao;
    private final ModelRepository<User> userRepository;
    private final ModelRepository<Team> teamRepository;
    private final ModelRepository<Game> gameRepository;

    private StatRepository() {
        api = TeammateService.getApiInstance();
        statDao = AppDatabase.getInstance().statDao();
        userRepository = UserRepository.getInstance();
        teamRepository = TeamRepository.getInstance();
        gameRepository = GameRoundRepository.getInstance();
    }

    public static StatRepository getInstance() {
        if (ourInstance == null) ourInstance = new StatRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Stat> dao() {
        return statDao;
    }

    @Override
    public Single<Stat> createOrUpdate(Stat stat) {
        Single<Stat> statSingle = stat.isEmpty()
                ? api.createStat(stat.getGame().getId(), stat).map(getLocalUpdateFunction(stat))
                : api.updateStat(stat.getId(), stat)
                .map(getLocalUpdateFunction(stat))
                .doOnError(throwable -> deleteInvalidModel(stat, throwable));

        return statSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Stat> get(String id) {
        Maybe<Stat> local = statDao.get(id).subscribeOn(io());
        Maybe<Stat> remote = api.getStat(id).subscribeOn(io()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Stat> delete(Stat stat) {
        return api.deleteStat(stat.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(stat, throwable));
    }

    @Override
    Maybe<List<Stat>> localModelsBefore(Game game, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        return statDao.getStats(game.getId(), date, DEF_QUERY_LIMIT).subscribeOn(io());
    }

    @Override
    Maybe<List<Stat>> remoteModelsBefore(Game game, @Nullable Date date) {
        return api.getStats(game.getId(), date, DEF_QUERY_LIMIT).map(getSaveManyFunction())
                .doOnSuccess(stats -> { for (Stat stat : stats) stat.getGame().update(game); })
                .toMaybe();
    }

    @Override
    Function<List<Stat>, List<Stat>> provideSaveManyFunction() {
        return models -> {
            List<User> users = new ArrayList<>(models.size());
            List<Team> teams = new ArrayList<>(models.size());
            List<Game> games = new ArrayList<>(models.size());

            for (Stat stat : models) {
                users.add(stat.getUser());
                teams.add(stat.getTeam());
                games.add(stat.getGame());
            }

            userRepository.saveAsNested().apply(users);
            teamRepository.saveAsNested().apply(teams);
            gameRepository.saveAsNested().apply(games);
            statDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
