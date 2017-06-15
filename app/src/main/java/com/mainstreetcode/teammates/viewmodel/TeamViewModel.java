package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class TeamViewModel extends ViewModel {

    private static final int TIMEOUT = 4;

    private static final TeammateApi api = TeammateService.getApiInstance();

    public Observable<List<Team>> findTeams(String queryText) {
        return api.findTeam(queryText).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<JoinRequest> joinTeam(Team team, String role) {
        return api.joinTeam(team.getId(), role).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Team> createTeam(Team team) {
        return api.createTeam(team).observeOn(AndroidSchedulers.mainThread());
    }
}
