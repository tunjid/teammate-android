package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.TeamRepository;

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

    public TeamViewModel() {
        repository = TeamRepository.getInstance();
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

    public Flowable<List<Team>> getMyTeams() {
        return repository.getMyTeams();
    }

    public Single<Team> deleteTeam(Team team) {
        return repository.delete(team);
    }
}
