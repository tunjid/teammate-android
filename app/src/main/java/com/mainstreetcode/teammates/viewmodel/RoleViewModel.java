package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;
import android.util.Log;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.subjects.ReplaySubject;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends TeamMappedViewModel<Model> {

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
        return checkForInvalidObject(repository.createOrUpdate(role).toFlowable().cast(Model.class), role, role.getTeam())
                .firstOrError()
                .cast(Role.class)
                .observeOn(mainThread());
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest).observeOn(mainThread());
    }

    @SuppressWarnings("unchecked")
    public Flowable<DiffUtil.DiffResult> approveUser(JoinRequest request, Team team) {
        Flowable<List<Model>> sourceFlowable = repository.approveUser(request)
                .cast(Model.class)
                .map(Collections::singletonList)
                .toFlowable();

        final Callable<List<Model>> listCallable = () -> getModelList(team);

        return Identifiable.diff(sourceFlowable, listCallable, (teamListCopy, addedRole) -> {
            teamListCopy.remove(request);
            teamListCopy.addAll(addedRole);
            Collections.sort(teamListCopy, (modelA, modelB) -> {
                if (modelA.getClass().equals(modelB.getClass())) return modelA.compareTo(modelB);
                if (modelA instanceof Role) return 1;
                else return -1;
            });
            return teamListCopy;
        }).onErrorResumeNext(checkForInvalidModel(listCallable, request));
    }

    public Flowable<DiffUtil.DiffResult> declineUser(JoinRequest request, Team team) {
        Flowable<List<Model>> sourceFlowable = joinRequestRepository.dropJoinRequest(request)
                .doOnSuccess(team.getJoinRequests()::remove)
                .cast(Model.class)
                .map(Collections::singletonList)
                .toFlowable();

        final Callable<List<Model>> listCallable = () -> getModelList(team);

        return Identifiable.diff(sourceFlowable, listCallable, (teamListCopy, deleted) -> {
            teamListCopy.removeAll(deleted);
            return teamListCopy;
        }).onErrorResumeNext(checkForInvalidModel(listCallable, request));
    }

    public Single<Role> deleteRole(Role role) {
        return checkForInvalidObject(repository.delete(role)
                .doOnSuccess(deleted -> {
                    getModelList(role.getTeam()).remove(deleted);
                    role.getTeam().getRoles().remove(deleted);
                    List<Model> models = getModelList(role.getTeam());
                    Log.i("Test", "Deleted role!" + models);
                })
                .toFlowable().cast(Model.class), role, role.getTeam())
                .firstOrError()
                .cast(Role.class)
                .observeOn(mainThread());
    }

    private Function<Throwable, Flowable<DiffUtil.DiffResult>> checkForInvalidModel(Callable<List<Model>> callable, Model invalid) {
        return throwable -> {
            Flowable<List<Model>> sourceFlowable = Flowable.just(Collections.singletonList(invalid));

            if (ModelUtils.isInvalidObject(throwable))
                return Identifiable.diff(sourceFlowable, callable, (teamListCopy, invalidList) -> {
                    teamListCopy.removeAll(invalidList);
                    return teamListCopy;
                });
            else return Flowable.error(throwable);
        };
    }
}
