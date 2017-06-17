package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.TeamDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Observable;

import static io.reactivex.Observable.fromCallable;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;


/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class TeamViewModel extends ViewModel {

    private static final TeammateApi api = TeammateService.getApiInstance();
    private final TeamDao teamDao = AppDatabase.getInstance().teamDao();

    public Observable<Team> createTeam(Team team) {
        return api.createTeam(team).observeOn(mainThread());
    }

    public Observable<Team> getTeam(Team team) {
        return api.getTeam(team.getId()).observeOn(mainThread());
    }

    public Observable<JoinRequest> joinTeam(Team team, String role) {
        return api.joinTeam(team.getId(), role).observeOn(mainThread());
    }

    public Observable<List<Team>> findTeams(String queryText) {
        return api.findTeam(queryText).observeOn(mainThread());
    }

    public Observable<List<Team>> getMyTeams() {
        Observable<List<Team>> local = fromCallable(teamDao::getTeams).subscribeOn(io());
        Observable<List<Team>> remote = api.getMyTeams().flatMap(this::saveTeams);

        return Observable.concat(local, remote).observeOn(mainThread());
    }

    private Observable<List<Team>> saveTeams(List<Team> teams) {
        teamDao.insert(teams);
        return Observable.just(teams);
    }
}
