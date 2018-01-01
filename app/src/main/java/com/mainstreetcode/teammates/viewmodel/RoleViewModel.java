package com.mainstreetcode.teammates.viewmodel;

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
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends TeamMappedViewModel<Model> {

    private int count;
    private final RoleRepository roleRepository;
    private final JoinRequestRepository joinRequestRepository;

    private final TeammateApi api = TeammateService.getApiInstance();
    private final List<String> roleNames = new ArrayList<>();

    public RoleViewModel() {
        roleRepository = RoleRepository.getInstance();
        joinRequestRepository = JoinRequestRepository.getInstance();
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void fetchRoleValues() {
        Maybe<List<String>> listMaybe = !roleNames.isEmpty() && count++ % 5 != 0
                ? Maybe.empty()
                : api.getRoleValues().toMaybe();

        listMaybe.observeOn(mainThread()).subscribe(names -> {
            roleNames.clear();
            roleNames.addAll(names);
            count++;
        }, ErrorHandler.EMPTY);
    }

    public Single<Role> updateRole(Role role) {
        return checkForInvalidObject(roleRepository.createOrUpdate(role).toFlowable().cast(Model.class), role, role.getTeam())
                .firstOrError()
                .cast(Role.class)
                .observeOn(mainThread());
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest).observeOn(mainThread());
    }

    public Flowable<DiffUtil.DiffResult> processJoinRequest(JoinRequest request, Team team, boolean approved) {
        Flowable<List<Model>> sourceFlowable = (approved
                ? roleRepository.approveUser(request)
                : joinRequestRepository.dropJoinRequest(request))
                .cast(Model.class)
                .map(Collections::singletonList)
                .toFlowable();

        final Callable<List<Model>> listCallable = () -> getModelList(team);

        return Identifiable.diff(sourceFlowable, listCallable, onRequestProcessed(request, approved))
                .onErrorResumeNext(checkForInvalidModel(listCallable, request));
    }

    public Single<Role> deleteRole(Role role) {
        return checkForInvalidObject(roleRepository.delete(role)
                .doOnSuccess(getModelList(role.getTeam())::remove)
                .toFlowable().cast(Model.class), role, role.getTeam())
                .firstOrError()
                .cast(Role.class)
                .observeOn(mainThread());
    }

    private Function<Throwable, Flowable<DiffUtil.DiffResult>> checkForInvalidModel(Callable<List<Model>> callable, Model invalid) {
        return throwable -> {
            Flowable<List<Model>> sourceFlowable = Flowable.just(Collections.singletonList(invalid));

            return ModelUtils.isInvalidObject(throwable)
                    ? Identifiable.diff(sourceFlowable, callable, onRequestProcessed(invalid, false))
                    : Flowable.error(throwable);
        };
    }

    private BiFunction<List<Model>, List<Model>, List<Model>> onRequestProcessed(Model model, boolean approved) {
        if (approved) return (teamMembers, added) -> {
            teamMembers.remove(model);
            teamMembers.addAll(added);
            Collections.sort(teamMembers, Model.COMPARATOR);
            return teamMembers;
        };
        else return (teamMembers, deleted) -> {
            teamMembers.removeAll(deleted);
            return teamMembers;
        };
    }
}
