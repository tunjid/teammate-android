package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.model.UserHost;
import com.mainstreetcode.teammate.repository.JoinRequestRepository;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.repository.TeamMemberRepository;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.mainstreetcode.teammate.viewmodel.gofers.JoinRequestGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.RoleGofer;

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
        removeBlockedUser(((Alert.UserBlocked) alert).getModel());
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
        return new JoinRequestGofer(joinRequest, onError(TeamMember.fromModel(joinRequest)), JoinRequestRepository.getInstance()::get, this::processRequest);
    }

    public RoleGofer gofer(Role role) {
        return new RoleGofer(role, onError(TeamMember.fromModel(role)), RoleRepository.getInstance()::get, this::deleteRole, this::updateRole);
    }

    private Single<Role> deleteRole(Role role) {
        return asTypedTeamMember(role, (member, repository) -> repository.delete(member).doOnSuccess(getModelList(role.getTeam())::remove).map(deleted -> role));
    }

    private Single<Role> updateRole(Role role) {
        return asTypedTeamMember(role, (member, repository) -> repository.createOrUpdate(member).map(updated -> role));
    }

    private Single<JoinRequest> processRequest(JoinRequest request, boolean approved) {
        return asTypedTeamMember(request, (member, repository) -> {
            Team team = request.getTeam();

            Single<TeamMember<JoinRequest>> sourceSingle = approved
                    ? repository.createOrUpdate(member)
                    : repository.delete(member);

            return sourceSingle
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

    private void removeBlockedUser(BlockedUser blockedUser) {
        Iterator<Identifiable> iterator = getModelList(blockedUser.getTeam()).iterator();

        while (iterator.hasNext()) {
            Identifiable identifiable = iterator.next();
            if (!(identifiable instanceof TeamMember)) continue;

            TeamMember member = ((TeamMember) identifiable);
            if (member.getUser().equals(blockedUser.getUser())) iterator.remove();
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
