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


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamSearchRequest;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.TeamDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import okhttp3.MultipartBody;

import static io.reactivex.schedulers.Schedulers.io;

public class TeamRepo extends ModelRepo<Team> {

    private static final String TEAM_REPOSITORY_KEY = "TeamRepository";
    private static final String DEFAULT_TEAM = "default.team";

    private final App app;
    private final TeammateApi api;
    private final TeamDao teamDao;

    TeamRepo() {
        app = App.getInstance();
        api = TeammateService.getApiInstance();
        teamDao = AppDatabase.getInstance().teamDao();
    }

    @Override
    public EntityDao<? super Team> dao() {
        return teamDao;
    }

    @Override
    public Single<Team> createOrUpdate(Team model) {
        Single<Team> teamSingle = model.isEmpty()
                ? api.createTeam(model).map(getLocalUpdateFunction(model))
                .flatMap(team -> RepoProvider.forRepo(RoleRepo.class).getMyRoles().lastOrError())
                .map(roles -> model)
                : api.updateTeam(model.getId(), model).map(getLocalUpdateFunction(model))
                .doOnError(throwable -> deleteInvalidModel(model, throwable));

        MultipartBody.Part body = getBody(model.getHeaderItem().getValue(), Team.PHOTO_UPLOAD_KEY);
        if (body != null) {
            teamSingle = teamSingle.flatMap(put -> api.uploadTeamLogo(model.getId(), body).map(getLocalUpdateFunction(model)));
        }

        return teamSingle.map(getSaveFunction());
    }

    @Override
    public Flowable<Team> get(String id) {
        Maybe<Team> local = teamDao.get(id).subscribeOn(io());
        Maybe<Team> remote = api.getTeam(id).map(getSaveFunction()).toMaybe();

        return fetchThenGetModel(local, remote);
    }

    @Override
    public Single<Team> delete(Team team) {
        return api.deleteTeam(team.getId())
                .map(this::deleteLocally)
                .doOnError(throwable -> deleteInvalidModel(team, throwable));
    }

    @Override
    Function<List<Team>, List<Team>> provideSaveManyFunction() {
        return models -> {
            teamDao.upsert(Collections.unmodifiableList(models));
            return models;
        };
    }

    public Single<List<Team>> findTeams(TeamSearchRequest request) {
        return api.findTeam(request.getName(), request.getScreenName(), request.getSport());
    }

    public Maybe<Team> getDefaultTeam() {
        SharedPreferences preferences = app.getSharedPreferences(TEAM_REPOSITORY_KEY, Context.MODE_PRIVATE);
        String defaultTeamId = preferences.getString(DEFAULT_TEAM, "");

        return TextUtils.isEmpty(defaultTeamId) ? Maybe.empty() : teamDao.get(defaultTeamId).subscribeOn(io());
    }

    public void saveDefaultTeam(Team team) {
        SharedPreferences preferences = app.getSharedPreferences(TEAM_REPOSITORY_KEY, Context.MODE_PRIVATE);
        preferences.edit().putString(DEFAULT_TEAM, team.getId()).apply();
    }

    @SuppressWarnings("unused")
    private void clearStaleTeamMembers(Team team) {
        // Clear stale join requests and roles because the api version has the latest
        AppDatabase database = AppDatabase.getInstance();
        database.roleDao().deleteByTeam(team.getId());
        database.joinRequestDao().deleteByTeam(team.getId());
    }
}
