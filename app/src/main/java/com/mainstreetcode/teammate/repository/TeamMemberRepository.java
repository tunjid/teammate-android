package com.mainstreetcode.teammate.repository;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.TeamMemberDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.schedulers.Schedulers.io;

public class TeamMemberRepository<T extends Model<T>> extends TeamQueryRepository<TeamMember<T>> {

    private final TeammateApi api;
    private final TeamMemberDao dao;
    private final RoleRepository roleRepository;
    private final JoinRequestRepository requestRepository;

    private static TeamMemberRepository ourInstance;

    private TeamMemberRepository() {
        api = TeammateService.getApiInstance();
        dao = AppDatabase.getInstance().teamMemberDao();
        roleRepository = RoleRepository.getInstance();
        requestRepository = JoinRequestRepository.getInstance();
    }

    @SuppressWarnings("unchecked")
    public static <S extends Model<S>> TeamMemberRepository<S> getInstance() {
        if (ourInstance == null) ourInstance = new TeamMemberRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super TeamMember> dao() {
        return dao;
    }

    @Override
    public Single<TeamMember<T>> createOrUpdate(TeamMember<T> model) {
        Model<?> wrapped = model.getWrappedModel();
        Single<TeamMember<T>> single;

        if (wrapped instanceof Role) {
            single = unsafeCast(roleRepository.createOrUpdate((Role) wrapped));
        }
        else if (wrapped instanceof JoinRequest) {
            JoinRequest request = (JoinRequest) wrapped;
            single = request.isUserApproved() ? approveUser(request) : acceptInvite(request);
        }
        else single = Single.error(new TeammateException("Unimplemented"));

        single = single.map(getLocalUpdateFunction(model));

        return single;
    }

    @Override
    public Flowable<TeamMember<T>> get(String id) {
        return Flowable.error(new IllegalArgumentException("Unimplementable"));
    }

    @Override
    public Single<TeamMember<T>> delete(TeamMember<T> model) {
        Model<?> wrapped = model.getWrappedModel();
        Single<TeamMember<T>> single;

        if (wrapped instanceof Role) {
            single = unsafeCast(roleRepository.delete((Role) wrapped));
        }
        else if (wrapped instanceof JoinRequest) {
            single = unsafeCast(requestRepository.delete((JoinRequest) wrapped));
        }
        else single = Single.error(new TeammateException("Unimplemented"));

        return single;
    }

    @Override
    @SuppressLint("CheckResult")
    Maybe<List<TeamMember<T>>> localModelsBefore(Team key, @Nullable Date date) {
        if (date == null) date = new Date();

        AppDatabase database = AppDatabase.getInstance();
        String teamId = key.getId();

        Maybe<List<Role>> rolesMaybe = database.roleDao().getRoles(key.getId(), date).defaultIfEmpty(new ArrayList<>());
        Maybe<List<JoinRequest>> requestsMaybe = database.joinRequestDao().getRequests(teamId, date).defaultIfEmpty(new ArrayList<>());

        Maybe<List<TeamMember>> listMaybe = Maybe.zip(rolesMaybe, requestsMaybe, (roles, requests) -> {
            List<TeamMember> result = new ArrayList<>(roles.size() + requests.size());

            for (Role role : roles) result.add(TeamMember.fromModel(role));
            for (JoinRequest request : requests) result.add(TeamMember.fromModel(request));

            return result;
        }).subscribeOn(io());

        return unsafeCastList(listMaybe);
    }

    private Single<TeamMember<T>> acceptInvite(JoinRequest request) {
        return apply(request, api.acceptInvite(request.getId()));
    }

    private Single<TeamMember<T>> approveUser(JoinRequest request) {
        return apply(request, api.approveUser(request.getId()));
    }

    private Single<TeamMember<T>> apply(JoinRequest request, Single<Role> apiSingle) {
        TeamMember<T> member = TeamMember.unsafeCast(TeamMember.fromModel(request));
        return apiSingle
                .map(roleRepository.getSaveFunction())
                .doOnSuccess(role -> AppDatabase.getInstance().joinRequestDao().delete(request))
                .map(role -> member)
                .doOnError(throwable -> deleteInvalidModel(member, throwable));
    }

    @Override
    Maybe<List<TeamMember<T>>> remoteModelsBefore(Team key, @Nullable Date date) {
        Maybe<List<TeamMember<T>>> maybe = TeamMemberRepository.unsafeCastList(api.getTeamMembers(key.getId(), date).toMaybe());
        return maybe.map(getSaveManyFunction());
    }

    @Override
    Function<List<TeamMember<T>>, List<TeamMember<T>>> provideSaveManyFunction() {
        return models -> {
            TeamMember.split(Collections.unmodifiableList(models), (roles, requests) -> {
                deleteStaleJoinRequests(roles);

                if (!requests.isEmpty()) requestRepository.saveAsNested().apply(requests);
                if (!roles.isEmpty()) roleRepository.saveAsNested().apply(roles);
            });
            return models;
        };
    }

    private void deleteStaleJoinRequests(@Nullable List<Role> roles) {
        if (roles == null) return;

        String teamId = roles.get(0).getTeam().getId();
        String[] userIds = new String[roles.size()];

        for (int i = 0; i < roles.size(); i++) userIds[i] = roles.get(i).getUser().getId();

        AppDatabase.getInstance().joinRequestDao().deleteRequestsFromTeam(teamId, userIds);
    }

    private static <S extends Model<S>, R extends Model<R>> Single<TeamMember<S>> unsafeCast(final Single<R> single) {
        return single.map(TeamMember::fromModel).map(TeamMember::unsafeCast);
    }

    private static <S extends Model<S>> Maybe<List<TeamMember<S>>> unsafeCastList(final Maybe<List<TeamMember>> single) {
        return single.map(TeamMemberRepository::unsafeCastList);
    }

    @SuppressWarnings("unchecked")
    private static <S extends Model<S>> List<S> unsafeCastList(List<TeamMember> source){
        return new ArrayList(source);
    }
}
