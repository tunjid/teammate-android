package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;


/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class TeamViewModel extends ViewModel {

    private final TeamRepository repository;
    private final Team defaultTeam = Team.empty();

    public TeamViewModel() {
        repository = TeamRepository.getInstance();
        repository.getDefaultTeam().subscribe(defaultTeam::update, ErrorHandler.EMPTY);
    }

    public Single<Team> createOrUpdate(Team team) {
        return repository.createOrUpdate(team);
    }

    public Flowable<Team> getTeam(Team team) {
        return repository.get(team.getId());
    }

    public Single<List<Team>> findTeams(String queryText) {
        return repository.findTeams(queryText);
    }

    public Flowable<List<Team>> getMyTeams(String userId) {
        return repository.getMyTeams(userId);
    }

    public Single<Team> deleteTeam(Team team) {
        return repository.delete(team);
    }

    public Team getDefaultTeam() {
        return defaultTeam;
    }

    public void updateDefaultTeam(Team newDefault) {
        defaultTeam.update(newDefault);
        repository.saveDefaultTeam(defaultTeam);
    }
}
