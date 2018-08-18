package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.Team;
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

public class StatRepository extends TeamQueryRepository<Stat> {

    private static StatRepository ourInstance;

    private final TeammateApi api;
    private final StatDao statDao;
    private final ModelRepository<Team> teamRepository;

    private StatRepository() {
        api = TeammateService.getApiInstance();
        statDao = AppDatabase.getInstance().statDao();
        teamRepository = TeamRepository.getInstance();
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
                ? api.createStat(stat.getTeam().getId(), stat.getGame().getId(), stat).map(getLocalUpdateFunction(stat))
                : api.updateStat(stat.getTeam().getId(), stat.getGame().getId(),stat.getId(), stat)
                .map(getLocalUpdateFunction(stat))
                .doOnError(throwable -> deleteInvalidModel(stat, throwable));

        return statSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Stat> get(String id) {
        return statDao.get(id).subscribeOn(io()).toFlowable();
    }

    @Override
    public Single<Stat> delete(Stat stat) {
        return api.deleteStat(stat.getTeam().getId(), stat.getGame().getId(), stat.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(stat, throwable));
    }

    @Override
    Maybe<List<Stat>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        return statDao.getStats(team.getId(), date).subscribeOn(io());
    }

    @Override
    Maybe<List<Stat>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getStats(team.getId(), date).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Stat>, List<Stat>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            for (Stat stat : models) teams.add(stat.getTeam());

            teamRepository.saveAsNested().apply(teams);
            statDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
