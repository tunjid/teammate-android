package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.TeamMemberRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.viewmodel.events.Alert;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

import static com.mainstreetcode.teammate.util.ModelUtils.findLast;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

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
    @SuppressLint("CheckResult")
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);
        if (alert instanceof Alert.UserBlocked) {
            User user = ((Alert.UserBlocked) alert).getModel();

            Flowable.fromIterable(modelListMap.values())
                    .map(List::iterator)
                    .subscribe(iterator -> removeBlockedUser(user, iterator), ErrorHandler.EMPTY);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    Flowable<List<TeamMember>> fetch(Team key, boolean fetchLatest) {
        Flowable<List<TeamMember>> flowable = repository.modelsBefore(key, getQueryDate(key, fetchLatest));
        flowable = flowable.doOnError(throwable -> checkForInvalidTeam(throwable, key));

        return flowable;
    }

    public Flowable<DiffUtil.DiffResult> processJoinRequest(JoinRequest request, boolean approved) {
        return asTypedTeamMember(request, (member, repository) -> {
            Single<TeamMember<JoinRequest>> sourceSingle = approved
                    ? repository.createOrUpdate(member)
                    : repository.delete(member);

            sourceSingle = sourceSingle.doOnSuccess(processed -> pushModelAlert(Alert.requestProcessed(request)));

            Flowable<List<Identifiable>> sourceFlowable = checkForInvalidObject(sourceSingle
                    .toFlowable().cast(Model.class), request.getTeam(), member)
                    .cast(Identifiable.class)
                    .map(Collections::singletonList);

            final Callable<List<Identifiable>> listCallable = () -> getModelList(request.getTeam());

            return Identifiable.diff(sourceFlowable, listCallable, onRequestProcessed(request, approved));
        });
    }

    public Single<Role> deleteRole(Role role) {
        return asTypedTeamMember(role, (member, repository) -> {
            Single<TeamMember<Role>> deletionSingle = repository.delete(member);
            deletionSingle = deletionSingle.doOnSuccess(getModelList(role.getTeam())::remove);

            return checkForInvalidObject(deletionSingle.toFlowable(), role.getTeam(), member)
                    .firstOrError()
                    .map(deleted -> role)
                    .observeOn(mainThread());
        });
    }

    public Single<Role> updateRole(Role role) {
        return asTypedTeamMember(role, (member, repository) -> {
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

    private <T extends Model<T>, S> S asTypedTeamMember(T model, BiFunction<TeamMember<T>, TeamMemberRepository<T>, S> function) {
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

    private void removeBlockedUser(User user, Iterator<Identifiable> iterator) {
        while (iterator.hasNext()) {
            Identifiable identifiable = iterator.next();
            if (!(identifiable instanceof TeamMember)) continue;

            Model model = ((TeamMember) identifiable).getWrappedModel();
            if (model instanceof Role && ((Role) model).getUser().equals(user)) {
                iterator.remove();
            }
            if (model instanceof JoinRequest && ((JoinRequest) model).getUser().equals(user)) {
                iterator.remove();
            }
        }
    }
}
