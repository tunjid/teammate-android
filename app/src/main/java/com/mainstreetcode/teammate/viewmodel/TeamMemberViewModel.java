package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.BlockedUser;
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
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.BiFunction;

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
    Class<TeamMember> valueClass() { return TeamMember.class; }

    @Override
    @SuppressLint("CheckResult")
    void onModelAlert(Alert alert) {
        super.onModelAlert(alert);

        //noinspection unchecked
        Alert.matches(alert, Alert.of(Alert.Creation.class, BlockedUser.class, this::removeBlockedUser));
    }

    @Override
    void afterPullToRefreshDiff(List<Differentiable> source) {
        super.afterPullToRefreshDiff(source);
        filterJoinedMembers(source);
    }

    @Override
    void afterPreserveListDiff(List<Differentiable> source) {
        super.afterPreserveListDiff(source);
        filterJoinedMembers(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    Flowable<List<TeamMember>> fetch(Team key, boolean fetchLatest) {
        return repository.modelsBefore(key, getQueryDate(fetchLatest, key, TeamMember::getCreated));
    }

    public JoinRequestGofer gofer(JoinRequest joinRequest) {
        return new JoinRequestGofer(joinRequest, onError(TeamMember.fromModel(joinRequest)), JoinRequestRepository.getInstance()::get, this::processRequest);
    }

    public RoleGofer gofer(Role role) {
        return new RoleGofer(role, onError(TeamMember.fromModel(role)), RoleRepository.getInstance()::get, this::deleteRole, this::updateRole);
    }

    public Flowable<User> getAllUsers() {
        return getAllModels().filter(model -> model instanceof UserHost)
                .cast(UserHost.class).map(UserHost::getUser).distinct();
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

    private void onRequestProcessed(JoinRequest request, boolean approved, Team team, Differentiable processedMember) {
        pushModelAlert(Alert.requestProcessed(request));
        List<Differentiable> list = getModelList(team);
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

    private void removeBlockedUser(BlockedUser blockedUser) {
        Iterator<Differentiable> iterator = getModelList(blockedUser.getTeam()).iterator();

        while (iterator.hasNext()) {
            Differentiable identifiable = iterator.next();
            if (!(identifiable instanceof TeamMember)) continue;

            TeamMember member = ((TeamMember) identifiable);
            if (member.getUser().equals(blockedUser.getUser())) iterator.remove();
        }
    }

    private void filterJoinedMembers(List<Differentiable> source) {
        Set<String> userIds = new HashSet<>();
        ListIterator<Differentiable> iterator = source.listIterator(source.size());

        while (iterator.hasPrevious()) {
            Differentiable item = iterator.previous();
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
