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

package com.mainstreetcode.teammate.repository;


import androidx.annotation.Nullable;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
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
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class CompetitorRepo extends QueryRepo<Competitor, Tournament, Integer> {

    private final TeammateApi api;
    private final CompetitorDao competitorDao;

    CompetitorRepo() {
        api = TeammateService.getApiInstance();
        competitorDao = AppDatabase.getInstance().competitorDao();
    }

    @Override
    public EntityDao<? super Competitor> dao() {
        return competitorDao;
    }

    @Override
    public Single<Competitor> createOrUpdate(Competitor competitor) {
        Single<Competitor> competitorSingle = competitor.isEmpty()
                ? Single.error(new TeammateException(""))
                : api.updateCompetitor(competitor.getId(), competitor)
                .map(getLocalUpdateFunction(competitor))
                .doOnError(throwable -> deleteInvalidModel(competitor, throwable));

        return competitorSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Competitor> get(String id) {
        Maybe<Competitor> local = competitorDao.get(id).subscribeOn(io());
        Maybe<Competitor> remote = api.getCompetitor(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Competitor> delete(Competitor competitor) {
        return Single.error(new TeammateException(""));
    }

    public Single<List<Competitor>> getDeclined(Date date) {
        return api.getDeclinedCompetitors(date, DEF_QUERY_LIMIT).map(getSaveManyFunction());
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
            List<Game> games = new ArrayList<>(models.size());

            for (Competitor competitor : models) {
                Game game = competitor.getGame();
                Competitive entity = competitor.getEntity();
                if (entity instanceof Team) teams.add((Team) entity);
                else if (entity instanceof User) users.add((User) entity);
                if (!game.isEmpty()) games.add(game);
            }

            RepoProvider.forModel(User.class).saveAsNested().apply(users);
            RepoProvider.forModel(Team.class).saveAsNested().apply(teams);
            RepoProvider.forModel(Game.class).saveAsNested().apply(games);
            competitorDao.upsert(Collections.unmodifiableList(models));

            return models;
        };
    }
}
