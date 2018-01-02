package com.mainstreetcode.teammates.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.repository.TeamRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class TeamMemberViewModel extends TeamMappedViewModel<Model> {

    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final JoinRequestRepository joinRequestRepository;

    public TeamMemberViewModel() {
        teamRepository = TeamRepository.getInstance();
        roleRepository = RoleRepository.getInstance();
        joinRequestRepository = JoinRequestRepository.getInstance();
    }

    public Flowable<DiffUtil.DiffResult> getTeamMembers(Team team) {
        Flowable<List<Model>> sourceFlowable = checkForInvalidObject(teamRepository.get(team)
                .cast(Model.class), team, team)
                .cast(Team.class)
                .map(teamListFunction);

        return Identifiable.diff(sourceFlowable, () -> getModelList(team), (sourceTeamList, newTeamList) -> {
            Collections.sort(newTeamList, Model.COMPARATOR);
            return newTeamList;
        });
    }

    public Flowable<DiffUtil.DiffResult> processJoinRequest(JoinRequest request, boolean approved) {
        Flowable<List<Model>> sourceFlowable = (approved
                ? roleRepository.approveUser(request)
                : joinRequestRepository.dropJoinRequest(request))
                .cast(Model.class)
                .map(Collections::singletonList)
                .toFlowable();

        final Callable<List<Model>> listCallable = () -> getModelList(request.getTeam());

        return Identifiable.diff(sourceFlowable, listCallable, onRequestProcessed(request, approved));
    }

    public Single<Role> deleteRole(Role role) {
        return checkForInvalidObject(roleRepository.delete(role)
                .doOnSuccess(getModelList(role.getTeam())::remove)
                .toFlowable().cast(Model.class), role, role.getTeam())
                .firstOrError()
                .cast(Role.class)
                .observeOn(mainThread());
    }

    public Single<Role> updateRole(Role role) {
        return checkForInvalidObject(roleRepository.createOrUpdate(role)
                .toFlowable().cast(Model.class), role, role.getTeam())
                .firstOrError()
                .cast(Role.class)
                .observeOn(mainThread());
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

    private Function<Team, List<Model>> teamListFunction = team -> {
        List<Model> teamModels = new ArrayList<>();
        teamModels.addAll(team.getRoles());
        teamModels.addAll(team.getJoinRequests());

        return teamModels;
    };
}
