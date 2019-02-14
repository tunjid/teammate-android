package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamSearchRequest;
import com.mainstreetcode.teammate.repository.TeamRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.TeamGofer;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;


/**
 * ViewModel for team
 */

public class TeamViewModel extends MappedViewModel<Class<Team>, Team> {

    private static final Team defaultTeam = Team.empty();
    static final List<Differentiable> teams = Lists.transform(RoleViewModel.roles, role -> role instanceof Role ? ((Role) role).getTeam() : role);

    private final TeamRepository repository;
    private final PublishProcessor<Team> teamChangeProcessor;

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public TeamViewModel() {
        repository = TeamRepository.getInstance();
        teamChangeProcessor = PublishProcessor.create();
        repository.getDefaultTeam().subscribe(this::updateDefaultTeam, ErrorHandler.EMPTY);
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

    public InstantSearch<TeamSearchRequest, Team> instantSearch() {
        return new InstantSearch<>(repository::findTeams);
    }

    public Flowable<Team> getTeamChangeFlowable() {
        return Flowable.just(defaultTeam).concatWith(teamChangeProcessor);
    }

    private Flowable<Team> getTeam(Team team) {
        return repository.get(team);
    }

    private Single<Team> createOrUpdate(Team team) {
        return repository.createOrUpdate(team);
    }

    public Single<Team> deleteTeam(Team team) {
        return repository.delete(team).doOnSuccess(deleted -> pushModelAlert(Alert.teamDeletion(deleted)));
    }

    public void updateDefaultTeam(Team newDefault) {
        defaultTeam.update(newDefault);
        repository.saveDefaultTeam(defaultTeam);
        teamChangeProcessor.onNext(defaultTeam);
    }

    public boolean isOnATeam() {
        return !teams.isEmpty() || !defaultTeam.isEmpty();
    }

    public Team getDefaultTeam() {return defaultTeam;}

    @Override
    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void onModelAlert(Alert alert) {
        if (!(alert instanceof Alert.TeamDeletion)) return;
        Team deleted = ((Alert.TeamDeletion) alert).getModel();

        teams.remove(deleted);
        if (defaultTeam.equals(deleted)) defaultTeam.update(Team.empty());

        repository.queueForLocalDeletion(deleted);
    }
}
