package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


/**
 * ViewModel for team
 */

public class TeamViewModel extends MappedViewModel<Class<Team>, Team> {

    private final TeamRepository repository;
    private final Team defaultTeam = Team.empty();
    private final List<Team> teams = new ArrayList<>();

    public TeamViewModel() {
        repository = TeamRepository.getInstance();
        repository.getDefaultTeam().subscribe(defaultTeam::update, ErrorHandler.EMPTY);
    }

    @Override
    public List<Team> getModelList(Class<Team> key) {
        return teams;
    }

    public Single<Team> createOrUpdate(Team team) {
        return checkForInvalidObject(repository.createOrUpdate(team).toFlowable(), team, Team.class).observeOn(mainThread()).firstOrError();
    }

    public Flowable<DiffUtil.DiffResult> getTeam(Team team, List<Model> teamModels) {
        Flowable<List<Model>> sourceFlowable = checkForInvalidObject(repository.get(team), team, Team.class)
                .map(teamListFunction);

        return Identifiable.diff(sourceFlowable, () -> teamModels, (sourceTeamList, newTeamList) -> {
            Collections.sort(newTeamList, Model.COMPARATOR);
            return newTeamList;
        });
    }

    public Single<List<Team>> findTeams(String queryText) {
        return repository.findTeams(queryText).observeOn(mainThread());
    }

    public Flowable<DiffUtil.DiffResult> getMyTeams(String userId) {
        return Identifiable.diff(repository.getMyTeams(userId), () -> getModelList(Team.class), ModelUtils::preserveList);
    }

    public Single<Team> deleteTeam(Team team) {
        return checkForInvalidObject(repository.delete(team).toFlowable(), team, Team.class).observeOn(mainThread())
                .firstOrError()
                .doOnSuccess(getModelList(Team.class)::remove)
                .observeOn(mainThread());
    }

    public Team getDefaultTeam() {
        return defaultTeam;
    }

    public void updateDefaultTeam(Team newDefault) {
        defaultTeam.update(newDefault);
        repository.saveDefaultTeam(defaultTeam);
    }

    private Function<Team, List<Model>> teamListFunction = team -> {
        List<Model> teamModels = new ArrayList<>();
        teamModels.addAll(team.getRoles());
        teamModels.addAll(team.getJoinRequests());

        return teamModels;
    };
}
