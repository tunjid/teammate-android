package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamSearchRequest;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.TeamRepo;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.util.InstantSearch;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.TeamGofer;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


/**
 * ViewModel for team
 */

public class TeamViewModel extends MappedViewModel<Class<Team>, Team> {

    private final Team defaultTeam = Team.empty();
    static final List<Differentiable> teams = Lists.transform(RoleViewModel.roles, role -> role instanceof Role ? ((Role) role).getTeam() : role);

    private final TeamRepo repository;
    private final PublishProcessor<Team> teamChangeProcessor;

    public TeamViewModel() {
        teamChangeProcessor = PublishProcessor.create();
        repository = RepoProvider.forRepo(TeamRepo.class);
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
        return repository.getDefaultTeam().flatMapPublisher(team -> {
            updateDefaultTeam(team);
            return Flowable.fromCallable(() -> defaultTeam).concatWith(teamChangeProcessor);
        }).observeOn(mainThread());

    }

    private Flowable<Team> getTeam(Team team) {
        return repository.get(team);
    }

    private Single<Team> createOrUpdate(Team team) {
        return repository.createOrUpdate(team);
    }

    public Single<Team> deleteTeam(Team team) {
        return repository.delete(team).doOnSuccess(deleted -> pushModelAlert(Alert.deletion(deleted)));
    }

    public Single<DiffUtil.DiffResult> nonDefaultTeams(List<Team> sink) {
        return FunctionalDiff.of(Flowable.fromIterable(teams)
                        .filter(item -> item instanceof Team && !item.equals(defaultTeam))
                        .cast(Team.class)
                        .toList(), sink, ModelUtils::replaceList);
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
        //noinspection unchecked
        Alert.matches(alert, Alert.of(Alert.Deletion.class, Team.class, team -> {
            teams.remove(team);
            repository.queueForLocalDeletion(team);
            if (defaultTeam.equals(team)) defaultTeam.update(Team.empty());
        }));
    }
}
