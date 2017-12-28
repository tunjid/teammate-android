package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends ViewModel {

    private final RoleRepository repository;
    private final JoinRequestRepository joinRequestRepository;

    private TeammateApi api = TeammateService.getApiInstance();
    private ReplaySubject<List<String>> roleSubject;

    public RoleViewModel() {
        repository = RoleRepository.getInstance();
        joinRequestRepository = JoinRequestRepository.getInstance();
    }

    public Single<List<String>> getRoleValues() {
        // Use new subject if previous call errored out for whatever reason.
        if (roleSubject == null || roleSubject.hasThrowable()) {
            roleSubject = ReplaySubject.createWithSize(1);
            api.getRoleValues().toObservable().subscribe(roleSubject);
        }
        return roleSubject.singleOrError();
    }

    public Single<Role> updateRole(Role role) {
        return repository.createOrUpdate(role).observeOn(mainThread());
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest).observeOn(mainThread());
    }

    @SuppressWarnings("unchecked")

    public Flowable<DiffUtil.DiffResult> approveUser(JoinRequest request, Team team, List<Model> teamList) {
        Flowable<List<Model>> sourceFlowable = repository.approveUser(request)
                .doOnSuccess(role -> {
                    team.getRoles().add(role);
                    team.getJoinRequests().remove(request);
                })
                .cast(Model.class)
                .map(Collections::singletonList)
                .toFlowable();

        return Identifiable.diff(sourceFlowable, () -> teamList, (teamListCopy, addedRole) -> {
            teamListCopy.remove(request);
            teamListCopy.addAll(addedRole);
            Collections.sort(teamListCopy, (modelA, modelB) -> {
                if (modelA.getClass().equals(modelB.getClass())) return modelA.compareTo(modelB);
                if (modelA instanceof Role) return 1;
                else return -1;
            });
            return teamListCopy;
        });
    }

    public Flowable<DiffUtil.DiffResult> declineUser(JoinRequest request, Team team, List<Model> teamList) {
        Flowable<List<Model>> sourceFlowable = joinRequestRepository.dropJoinRequest(request)
                .doOnSuccess(team.getJoinRequests()::remove)
                .cast(Model.class)
                .map(Collections::singletonList)
                .toFlowable();

        return Identifiable.diff(sourceFlowable, () -> teamList, (teamListCopy, deleted) -> {
            teamListCopy.removeAll(deleted);
            return teamListCopy;
        });
    }

    public Single<Role> dropRole(Role role) {
        return repository.dropRole(role).observeOn(mainThread());
    }
}
