package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.TeamRepository;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.util.ModelDiffCallback;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.computation;


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
        return repository.createOrUpdate(team);
    }

    public Flowable<DiffUtil.DiffResult> getTeam(Team team) {
        final List<Model> original = new ArrayList<>(team.getRoles());
        original.addAll(team.getJoinRequests());

        return repository.get(team)
                .concatMapDelayError(fetched -> Flowable.fromCallable(() -> {
                    List<Model> stale = new ArrayList<>(original);
                    List<Model> updated = new ArrayList<>(fetched.getRoles());
                    updated.addAll(fetched.getJoinRequests());

                    original.clear();
                    original.addAll(updated);

                    return calculateDiff(new ModelDiffCallback(updated, stale));
                })
                        .subscribeOn(computation())
                        .observeOn(mainThread()));
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
