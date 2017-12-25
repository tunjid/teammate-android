package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


/**
 * ViewModel for team
 */

public class TeamViewModel extends ViewModel {

    private final TeamRepository repository;
    private final Team defaultTeam = Team.empty();

    public TeamViewModel() {
        repository = TeamRepository.getInstance();
        repository.getDefaultTeam().subscribe(defaultTeam::update, ErrorHandler.EMPTY);
    }

    public Single<Team> createOrUpdate(Team team) {
        return repository.createOrUpdate(team).observeOn(mainThread());
    }

    public Flowable<DiffUtil.DiffResult> getTeam(Team team, List<Model> teamModels) {
        Flowable<List<Model>> sourceFlowable = repository.get(team).map(teamListFunction);
        return Identifiable.diff(sourceFlowable, () -> teamModels, (sourceTeamList, newTeamList) -> newTeamList);
    }

    public Single<List<Team>> findTeams(String queryText) {
        return repository.findTeams(queryText).observeOn(mainThread());
    }

    public Flowable<DiffUtil.DiffResult> getMyTeams(String userId, List<Team> teams) {
        return Identifiable.diff(repository.getMyTeams(userId), () -> teams, ModelUtils::preserveList);
    }

    public Single<Team> deleteTeam(Team team) {
        return repository.delete(team).observeOn(mainThread());
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
