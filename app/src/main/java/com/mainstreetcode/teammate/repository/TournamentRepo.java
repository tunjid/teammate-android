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
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.TournamentDao;
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
import okhttp3.MultipartBody;

import static io.reactivex.schedulers.Schedulers.io;

public class TournamentRepo extends TeamQueryRepo<Tournament> {

    private final TeammateApi api;
    private final TournamentDao tournamentDao;

    TournamentRepo() {
        api = TeammateService.getApiInstance();
        tournamentDao = AppDatabase.getInstance().tournamentDao();
    }

    @Override
    public EntityDao<? super Tournament> dao() {
        return tournamentDao;
    }

    public Single<Tournament> addCompetitors(Tournament tournament, List<Competitor> competitors) {
        return api.addCompetitors(tournament.getId(), competitors)
                .map(getLocalUpdateFunction(tournament))
                .map(getSaveFunction());
    }

    @Override
    public Single<Tournament> createOrUpdate(Tournament tournament) {
        Single<Tournament> tournamentSingle = tournament.isEmpty()
                ? api.createTournament(tournament.getHost().getId(), tournament).map(getLocalUpdateFunction(tournament))
                : api.updateTournament(tournament.getId(), tournament)
                .map(getLocalUpdateFunction(tournament))
                .doOnError(throwable -> deleteInvalidModel(tournament, throwable));

        MultipartBody.Part body = getBody(tournament.getHeaderItem().getValue(), Tournament.PHOTO_UPLOAD_KEY);
        if (body != null) {
            tournamentSingle = tournamentSingle.flatMap(put -> api.uploadTournamentPhoto(tournament.getId(), body));
        }

        return tournamentSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Tournament> get(String id) {
        Maybe<Tournament> local = tournamentDao.get(id).subscribeOn(io());
        Maybe<Tournament> remote = api.getTournament(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Tournament> delete(Tournament tournament) {
        return api.deleteTournament(tournament.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(tournament, throwable));
    }

    @Override
    Maybe<List<Tournament>> localModelsBefore(Team team, @Nullable Date date) {
        if (date == null) date = getFutureDate();
        // To concatenate team to account for the way the id is stored in the db to accommodate users and teams
        String teamId = team.getId();
        return tournamentDao.getTournaments(teamId, date, DEF_QUERY_LIMIT).subscribeOn(io());
    }

    @Override
    Maybe<List<Tournament>> remoteModelsBefore(Team team, @Nullable Date date) {
        return api.getTournaments(team.getId(), date, DEF_QUERY_LIMIT).map(getSaveManyFunction()).toMaybe();
    }

    @Override
    Function<List<Tournament>, List<Tournament>> provideSaveManyFunction() {
        return models -> {
            int size = models.size();
            List<User> users = new ArrayList<>(size);
            List<Team> teams = new ArrayList<>(size);
            List<Competitor> competitors = new ArrayList<>(size);

            for (Tournament tournament : models) {
                teams.add(tournament.getTeam());
                Competitor competitor = tournament.getWinner();

                if (competitor.isEmpty()) continue;
                competitors.add(competitor);

                Competitive competitive = competitor.getEntity();
                if (competitive.isEmpty()) continue;

                if (competitive instanceof User) users.add((User) competitive);
                if (competitive instanceof Team) teams.add((Team) competitive);
            }

            RepoProvider.forModel(User.class).saveAsNested().apply(users);
            RepoProvider.forModel(Team.class).saveAsNested().apply(teams);

            tournamentDao.upsert(Collections.unmodifiableList(models));
            RepoProvider.forModel(Competitor.class).saveAsNested().apply(competitors);

            return models;
        };
    }

    @Override
    Tournament deleteLocally(Tournament model) {
        tournamentDao.deleteTournamentEvents(model.getId());
        return super.deleteLocally(model);
    }
}
