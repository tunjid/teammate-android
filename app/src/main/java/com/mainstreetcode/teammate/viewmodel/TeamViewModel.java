package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.BlockUserRequest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.repository.TeamRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.TransformingSequentialList;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;


/**
 * ViewModel for team
 */

public class TeamViewModel extends MappedViewModel<Class<Team>, Team> {

    private static final int SEARCH_DEBOUNCE = 300;
    private static final Team defaultTeam = Team.empty();
    static final List<Identifiable> teams = new TransformingSequentialList<>(RoleViewModel.roles, role -> role instanceof Role ? ((Role) role).getTeam() : role);

    private final TeamRepository repository;
    private AtomicReference<PublishProcessor<String>> searchRef;

    @SuppressLint("CheckResult")
    public TeamViewModel() {
        searchRef = new AtomicReference<>();
        repository = TeamRepository.getInstance();
        repository.getDefaultTeam().subscribe(defaultTeam::update, ErrorHandler.EMPTY);
    }

    @Override
    public List<Identifiable> getModelList(Class<Team> key) {
        return teams;
    }

    @Override
    Flowable<List<Team>> fetch(Class<Team> key, boolean fetchLatest) {
        return Flowable.empty();
    }

    public Single<DiffUtil.DiffResult> createOrUpdate(Team team) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.createOrUpdate(team).toFlowable(), Team.class, team)
                .observeOn(mainThread()).cast(Team.class).map(Team::asIdentifiables);

        return Identifiable.diff(sourceFlowable, team::asIdentifiables, (old, updated) -> updated).firstOrError();
    }

    public Flowable<DiffUtil.DiffResult> getTeam(Team team) {
        Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(repository.get(team), Team.class, team)
                .observeOn(mainThread()).cast(Team.class).map(Team::asIdentifiables);

        return Identifiable.diff(sourceFlowable, team::asIdentifiables, (old, updated) -> updated);
    }

    public Flowable<List<Team>> findTeams() {
        if (searchRef.get() == null) searchRef.set(PublishProcessor.create());
        return searchRef.get()
                .debounce(SEARCH_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap(query -> repository.findTeams(query).toFlowable())
                .doFinally(() -> searchRef.set(null))
                .observeOn(mainThread());
    }

    public Single<Team> deleteTeam(Team team) {
        return checkForInvalidObject(repository.delete(team).toFlowable(), Team.class, team).observeOn(mainThread())
                .firstOrError()
                .cast(Team.class)
                .doOnSuccess(deleted -> pushModelAlert(Alert.teamDeletion(deleted)))
                .observeOn(mainThread());
    }

    public Single<User> blockUser(BlockUserRequest blockUserRequest) {
        return repository.blockUser(blockUserRequest)
                .doOnSuccess(blocked -> pushModelAlert(Alert.userBlocked(blockUserRequest.getUser())));
    }

    public boolean postSearch(String queryText) {
        if (searchRef.get() == null) return false;
        searchRef.get().onNext(queryText);
        return true;
    }

    public void updateDefaultTeam(Team newDefault) {
        defaultTeam.update(newDefault);
        repository.saveDefaultTeam(defaultTeam);
    }

    public boolean isOnATeam() {
        return !teams.isEmpty() || !defaultTeam.isEmpty();
    }

    public Team getDefaultTeam() {return defaultTeam;}

    @Override
    @SuppressLint("CheckResult")
    void onModelAlert(Alert alert) {
        if (!(alert instanceof Alert.TeamDeletion)) return;
        Team deleted = ((Alert.TeamDeletion) alert).getModel();

        teams.remove(deleted);
        if (defaultTeam.equals(deleted)) defaultTeam.update(Team.empty());

        Completable.fromRunnable(() -> AppDatabase.getInstance().teamDao()
                .delete(deleted))
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, ErrorHandler.EMPTY);
    }
}
