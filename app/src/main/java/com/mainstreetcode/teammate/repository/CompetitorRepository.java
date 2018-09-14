package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.CompetitorDao;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class CompetitorRepository extends QueryRepository<Competitor, Tournament, Integer> {

    private static CompetitorRepository ourInstance;

    private final TeammateApi api;
    private final CompetitorDao competitorDao;

    private CompetitorRepository() {
        api = TeammateService.getApiInstance();
        competitorDao = AppDatabase.getInstance().competitorDao();
    }

    public static CompetitorRepository getInstance() {
        if (ourInstance == null) ourInstance = new CompetitorRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super Competitor> dao() {
        return competitorDao;
    }

    @Override
    public Single<Competitor> createOrUpdate(Competitor competitor) {
        return Single.error(new TeammateException(""));
    }

    @Override
    public Flowable<Competitor> get(String id) {
        Maybe<Competitor> local = competitorDao.get(id).subscribeOn(io());
        //Maybe<Competitor> remote = api.getCompetitor(id).map(getSaveFunction()).toMaybe();

        return local.toFlowable();
    }

    @Override
    public Single<Competitor> delete(Competitor competitor) {
        return Single.error(new TeammateException(""));
    }

    @Override
    Maybe<List<Competitor>> localModelsBefore(Tournament tournament, @Nullable Integer voided) {
        return competitorDao.getCompetitors(tournament.getId()).subscribeOn(io());
    }

    @Override
    Maybe<List<Competitor>> remoteModelsBefore(Tournament tournament, @Nullable Integer voided) {
        return api.getCompetitors(tournament.getId()).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Competitor>, List<Competitor>> provideSaveManyFunction() {
        return models -> {
            List<Team> teams = new ArrayList<>(models.size());
            List<User> users = new ArrayList<>(models.size());

            for (Competitor competitor : models) {
                Competitive entity = competitor.getEntity();
                if (entity instanceof Team) teams.add((Team) entity);
                else if (entity instanceof User) users.add((User) entity);
            }

            UserRepository.getInstance().saveAsNested().apply(users);
            TeamRepository.getInstance().saveAsNested().apply(teams);
            competitorDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
