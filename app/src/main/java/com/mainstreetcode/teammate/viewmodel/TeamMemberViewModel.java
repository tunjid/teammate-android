package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.UserHost;
import com.mainstreetcode.teammate.repository.TeamMemberRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
        if (!(alert instanceof Alert.UserBlocked)) return;
        User user = ((Alert.UserBlocked) alert).getModel();

        Flowable.fromIterable(modelListMap.values())
                .map(List::iterator)
                .subscribe(iterator -> removeBlockedUser(user, iterator), ErrorHandler.EMPTY);
    }

    @Override
    void afterPullToRefreshDiff(List<Identifiable> source) {
        super.afterPullToRefreshDiff(source);
        filterJoinedMembers(source);
    }

    @Override
    void afterPreserveListDiff(List<Identifiable> source) {
        super.afterPreserveListDiff(source);
        filterJoinedMembers(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    Flowable<List<TeamMember>> fetch(Team key, boolean fetchLatest) {
        Flowable<List<TeamMember>> flowable = repository.modelsBefore(key, getQueryDate(key, fetchLatest));
        flowable = flowable.doOnError(throwable -> checkForInvalidTeam(throwable, key));

        return flowable;
    }

    public JoinRequestGofer gofer(JoinRequest joinRequest) {
        return new JoinRequestGofer(joinRequest, this::processRequest);
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

    private Single<JoinRequest> processRequest(JoinRequest request, boolean approved) {
        return asTypedTeamMember(request, (member, repository) -> {
            Team team = request.getTeam();

            Single<TeamMember<JoinRequest>> sourceSingle = approved
                    ? repository.createOrUpdate(member)
                    : repository.delete(member);

            return checkForInvalidObject(sourceSingle.toFlowable(), team, member)
                    .firstOrError()
                    .doOnSuccess(processedMember -> onRequestProcessed(request, approved, team, processedMember))
                    .map(processedMember -> request);
        });
    }

    private void onRequestProcessed(JoinRequest request, boolean approved, Team team, Identifiable processedMember) {
        pushModelAlert(Alert.requestProcessed(request));
        List<Identifiable> list = getModelList(team);
        list.remove(TeamMember.fromModel(request));
        if (approved) list.add(processedMember);
    }

    @SuppressWarnings("unchecked")
    private <S extends Model<S> & UserHost & TeamHost> TeamMemberRepository<S> repository() {
        return repository;
    }

    private <T extends Model<T> & TeamHost & UserHost, S> S asTypedTeamMember(T model, BiFunction<TeamMember<T>, TeamMemberRepository<T>, S> function) {
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

            TeamMember member = ((TeamMember) identifiable);
            if (member.getUser().equals(user)) iterator.remove();
        }
    }

    private void filterJoinedMembers(List<Identifiable> source) {
        Set<String> userIds = new HashSet<>();
        ListIterator<Identifiable> iterator = source.listIterator(source.size());

        while (iterator.hasPrevious()) {
            Identifiable item = iterator.previous();
            if (!(item instanceof TeamMember)) continue;

            TeamMember member = ((TeamMember) item);
            item = member.getWrappedModel();

            if (item instanceof Role) userIds.add(member.getUser().getId());
            else if (item instanceof JoinRequest) {
                User user = member.getUser();
                if (userIds.contains(user.getId())) iterator.remove();
            }
        }
    }
}
