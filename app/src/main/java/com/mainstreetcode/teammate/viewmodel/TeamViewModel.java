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

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamSearchRequest;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.TeamRepo;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.TeamGofer;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


/**
 * ViewModel for team
 */

public class TeamViewModel extends MappedViewModel<Class<Team>, Team> {

    private final AtomicReference<Team> defaultTeamRef = new AtomicReference<>(Team.empty());
    static final List<Differentiable> teams = Lists.transform(RoleViewModel.roles, role -> role instanceof Role ? ((Role) role).getTeam() : role);

    private final TeamRepo repository;
    private final PublishProcessor<Team> teamChangeProcessor;

    public TeamViewModel() {
        teamChangeProcessor = PublishProcessor.create();
        repository = RepoProvider.forRepo(TeamRepo.class);
    }

    @Override
    Class<Team> valueClass() { return Team.class; }

    @Override
    public List<Differentiable> getModelList(Class<Team> key) {
        return teams;
    }

    @Override
    Flowable<List<Team>> fetch(Class<Team> key, boolean fetchLatest) {
        return Flowable.empty();
    }

    public TeamGofer gofer(Team team) {
        return new TeamGofer(team, throwable -> checkForInvalidObject(throwable, team, Team.class), this::getTeam, this::createOrUpdate, this::deleteTeam);
    }

    public InstantSearch<TeamSearchRequest, Differentiable> instantSearch() {
        return new InstantSearch<>(repository::findTeams, team -> team);
    }

    public Flowable<Team> getTeamChangeFlowable() {
        return repository.getDefaultTeam().flatMapPublisher(team -> {
            updateDefaultTeam(team);
            return Flowable.fromCallable(defaultTeamRef::get).concatWith(teamChangeProcessor);
        }).observeOn(mainThread());

    }

    private Flowable<Team> getTeam(Team team) {
        return repository.get(team).doOnNext(this::onTeamChanged);
    }

    private Single<Team> createOrUpdate(Team team) {
        return repository.createOrUpdate(team).doOnSuccess(this::onTeamChanged);
    }

    public Single<Team> deleteTeam(Team team) {
        return repository.delete(team).doOnSuccess(deleted -> pushModelAlert(Alert.deletion(deleted)));
    }

    public Single<DiffUtil.DiffResult> nonDefaultTeams(List<Team> sink) {
        return FunctionalDiff.of(Flowable.fromIterable(teams)
                .filter(item -> item instanceof Team && !item.equals(defaultTeamRef))
                .cast(Team.class)
                .toList(), sink, ModelUtils::replaceList);
    }

    public void updateDefaultTeam(Team newDefault) {
        Team copy = Team.empty();
        copy.update(newDefault);

        defaultTeamRef.set(copy);
        repository.saveDefaultTeam(copy);
        teamChangeProcessor.onNext(copy);
    }

    public boolean isOnATeam() {
        return !teams.isEmpty() || !defaultTeamRef.get().isEmpty();
    }

    public Team getDefaultTeam() { return defaultTeamRef.get(); }

    private void onTeamChanged(Team updated) {
        Team currentDefault = defaultTeamRef.get();
        if (currentDefault.equals(updated) && !currentDefault.areContentsTheSame(updated))
            updateDefaultTeam(updated);
    }

    @Override
    void onModelAlert(Alert alert) {
        //noinspection unchecked
        Alert.matches(alert, Alert.of(Alert.Deletion.class, Team.class, team -> {
            teams.remove(team);
            repository.queueForLocalDeletion(team);
            if (team.equals(defaultTeamRef.get())) defaultTeamRef.set(Team.empty());
        }));
    }
}
