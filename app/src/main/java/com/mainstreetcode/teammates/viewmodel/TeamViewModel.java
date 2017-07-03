package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.TeamRepository;

import java.util.List;

import io.reactivex.Observable;


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

    public Observable<Team> createTeam(Team team) {
        return repository.createTeam(team);
    }

    public Observable<Team> getTeam(Team team) {
        return repository.getTeam(team);
    }

    public Observable<Team> updateTeam(Team team) {
        return repository.updateTeam(team);
    }

    public Observable<List<Team>> findTeams(String queryText) {
        return repository.findTeams(queryText);
    }

    public Observable<List<Team>> getMyTeams() {
        return repository.getMyTeams();
    }

    public Observable<User> updateTeamUser(Team team, User user) {
        return repository.updateTeamUser(team, user);
    }

    public Observable<JoinRequest> joinTeam(Team team, String role) {
        return repository.joinTeam(team, role);
    }

    public Observable<JoinRequest> approveUser(Team team, User user, boolean approve) {
        return repository.approveUser(team, user, approve);
    }

    public Observable<User> dropUser(Team team, User user) {
        return repository.dropUser(team, user);
    }

    public Observable<Team> deleteTeam(Team team) {
        return repository.deleteTeam(team);
    }
}
