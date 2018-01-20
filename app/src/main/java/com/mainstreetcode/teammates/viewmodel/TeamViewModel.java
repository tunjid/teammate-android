package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


/**
 * ViewModel for team
 */

public class TeamViewModel extends MappedViewModel<Class<Team>, Team> {

    private final TeamRepository repository;
    private static final Team defaultTeam = Team.empty();
    static final List<Identifiable> teams = new ArrayList<>();

    public TeamViewModel() {
        repository = TeamRepository.getInstance();
        repository.getDefaultTeam().subscribe(defaultTeam::update, ErrorHandler.EMPTY);
    }

    @Override
    public List<Identifiable> getModelList(Class<Team> key) {
        return teams;
    }

    public Single<DiffUtil.DiffResult> createOrUpdate(Team team) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.createOrUpdate(team).toFlowable(), Team.class, team)
                .observeOn(mainThread()).cast(Team.class).map(teamListFunction);

        return Identifiable.diff(sourceFlowable, () -> teamListFunction.apply(team), (old, updated) -> updated).firstOrError();
    }

    public Flowable<DiffUtil.DiffResult> getTeam(Team team) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.get(team), Team.class, team)
                .observeOn(mainThread()).cast(Team.class).map(teamListFunction);

        return Identifiable.diff(sourceFlowable, () -> teamListFunction.apply(team), (old, updated) -> updated);
    }

    public Single<List<Team>> findTeams(String queryText) {
        return repository.findTeams(queryText).observeOn(mainThread());
    }

    public Flowable<DiffUtil.DiffResult> getMyTeams(String userId) {
        Flowable<List<Identifiable>> sourceFlowable = repository.getMyTeams(userId).map(toIdentifiable);
        return Identifiable.diff(sourceFlowable, () -> getModelList(Team.class), preserveList);
    }

    public Single<Team> deleteTeam(Team team) {
        return checkForInvalidObject(repository.delete(team).toFlowable(), Team.class, team).observeOn(mainThread())
                .firstOrError()
                .cast(Team.class)
                .map(TeamViewModel::onTeamDeleted)
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

    static Team onTeamDeleted(Team deleted) {
        teams.remove(deleted);
        if (defaultTeam.equals(deleted)) defaultTeam.update(Team.empty());
        Completable.fromRunnable(() -> AppDatabase.getInstance().teamDao()
                .delete(deleted)).subscribeOn(Schedulers.io()).subscribe(()->{}, ErrorHandler.EMPTY);
        return deleted;
    }

    private Function<Team, List<Identifiable>> teamListFunction = team -> {
        List<Identifiable> items = new ArrayList<>();
        for(int i = 0; i< team.size(); i++) items.add(team.get(i));
        return items;
    };
}
