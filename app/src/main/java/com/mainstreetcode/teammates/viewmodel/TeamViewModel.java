package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
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

    public Observable<Team> createOrUpdate(Team team) {
        return repository.createOrUpdate(team);
    }

    public Observable<Team> getTeam(Team team) {
        return repository.get(team.getId());
    }

    public Observable<List<Team>> findTeams(String queryText) {
        return repository.findTeams(queryText);
    }

    public Observable<List<Team>> getMyTeams() {
        return repository.getMyTeams();
    }

    public Observable<User> updateTeamUser(Role role) {
        return repository.updateTeamUser(role);
    }

    public Observable<JoinRequest> joinTeam(Team team, String role) {
        return repository.joinTeam(team, role);
    }

    public Observable<Role> approveUser(JoinRequest request) {
        return repository.approveUser(request);
    }

    public Observable<JoinRequest> declineUser(JoinRequest request) {
        return repository.declineUser(request);
    }

    public Observable<User> dropUser(Role role) {
        return repository.dropUser(role);
    }

    public Observable<Team> deleteTeam(Team team) {
        return repository.delete(team);
    }
}
