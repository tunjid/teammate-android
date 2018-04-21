package com.mainstreetcode.teammate.viewmodel;

import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.repository.TeamMemberRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

import static com.mainstreetcode.teammate.util.ModelUtils.findLast;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
 */

public class TeamMemberViewModel extends TeamMappedViewModel<TeamMember> {

    private final TeamMemberRepository repository;

    public TeamMemberViewModel() {
        repository = TeamMemberRepository.getInstance();
    }

    @Override
    boolean sortsAscending() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    Flowable<List<TeamMember>> fetch(Team key, boolean fetchLatest) {
        Flowable<List<TeamMember>> flowable = repository.modelsBefore(key, getQueryDate(key, fetchLatest));
        flowable = flowable.doOnError(throwable -> checkForInvalidTeam(throwable, key));

        return flowable;
    }

    public Flowable<DiffUtil.DiffResult> processJoinRequest(JoinRequest request, boolean approved) {
        return am(request, (member, repository) -> {
            Single<TeamMember<JoinRequest>> sourceSingle = approved
                    ? repository.createOrUpdate(member)
                    : repository.delete(member);

            Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(sourceSingle
                    .toFlowable().cast(Model.class), request.getTeam(), member)
                    .cast(Identifiable.class)
                    .map(Collections::singletonList);

            final Callable<List<Identifiable>> listCallable = () -> getModelList(request.getTeam());

            return Identifiable.diff(sourceFlowable, listCallable, onRequestProcessed(request, approved));
        });
    }

    public Single<Role> deleteRole(Role role) {
        return am(role, (member, repository) -> {
            Single<TeamMember<Role>> deletionSingle = repository.delete(member);
            deletionSingle = deletionSingle.doOnSuccess(getModelList(role.getTeam())::remove);

            return checkForInvalidObject(deletionSingle.toFlowable(), role.getTeam(), member)
                    .firstOrError()
                    .map(deleted -> role)
                    .observeOn(mainThread());
        });
    }

    public Single<Role> updateRole(Role role) {
        return am(role, (member, repository) -> {
            Single<TeamMember<Role>> deletionSingle = repository.createOrUpdate(member);

            return checkForInvalidObject(deletionSingle.toFlowable(), role.getTeam(), member)
                    .firstOrError()
                    .map(updated -> role)
                    .observeOn(mainThread());
        });
    }

    private BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>> onRequestProcessed(Identifiable model, boolean approved) {
        if (approved) return (teamMembers, added) -> {
            teamMembers.remove(model);
            teamMembers.addAll(added);
            Collections.sort(teamMembers, Identifiable.COMPARATOR);
            return teamMembers;
        };
        else return (teamMembers, deleted) -> {
            teamMembers.removeAll(deleted);
            return teamMembers;
        };
    }

    @SuppressWarnings("unchecked")
    private <S extends Model<S>> TeamMemberRepository<S> repository() {
        return repository;
    }

    private <T extends Model<T>, S> S am(T model, BiFunction<TeamMember<T>, TeamMemberRepository<T>, S> function) {
        try {return function.apply(TeamMember.fromModel(model), repository());}
        catch (Exception e) {throw new RuntimeException(e);}
    }

    private Date getQueryDate(Team team, boolean fetchLatest) {
        if (fetchLatest) return null;

        TeamMember member = findLast(getModelList(team), TeamMember.class);
        if (member == null) return null;
        Model model = member.getWrappedModel();

        return model instanceof Role
                ? ((Role) model).getCreated()
                : model instanceof JoinRequest
                ? ((JoinRequest) model).getCreated()
                : null;
    }
}
