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
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class TeamMemberRepository<T extends Model<T>> extends TeamQueryRepository<TeamMember<T>> {

    private final TeammateApi api;
    private final RoleRepository roleRepository;
    private final JoinRequestRepository joinRequestRepository;

    private static TeamMemberRepository ourInstance;

    private TeamMemberRepository() {
        api = TeammateService.getApiInstance();
        roleRepository = RoleRepository.getInstance();
        joinRequestRepository = JoinRequestRepository.getInstance();
    }

    public static TeamMemberRepository getInstance() {
        if (ourInstance == null) ourInstance = new TeamMemberRepository();
        return ourInstance;
    }

    @Override
    public EntityDao<? super TeamMember> dao() {
        return null;
    }

    @Override
    public Single<TeamMember<T>> createOrUpdate(TeamMember<T> model) {
        Model<?> wrapped = model.getWrappedModel();
        return wrapped instanceof Role
                ? roleRepository.createOrUpdate((Role) wrapped).map(TeamMember::fromModel).map(getLocalUpdateFunction(model))
                : wrapped instanceof JoinRequest
                ? joinRequestRepository.createOrUpdate((JoinRequest) wrapped).map(TeamMember::fromModel).map(getLocalUpdateFunction(model))
                : Single.error(new TeammateException("Unimplemented"));
    }

    @Override
    public Flowable<TeamMember<T>> get(String id) {
        return null;
    }

    @Override
    public Single<TeamMember<T>> delete(TeamMember<T> model) {
        Model<?> wrapped = model.getWrappedModel();
        return wrapped instanceof Role
                ? roleRepository.delete((Role) wrapped).map(TeamMember::fromModel)
                : wrapped instanceof JoinRequest
                ? joinRequestRepository.delete((JoinRequest) wrapped).map(TeamMember::fromModel)
                : Single.error(new TeammateException("Unimplemented"));
    }


    @Override
    @SuppressLint("CheckResult")
    @SuppressWarnings("unchecked")
    Maybe<List<TeamMember<T>>> localModelsBefore(Team key, @Nullable Date date) {
        AppDatabase database = AppDatabase.getInstance();
        String teamId = key.getId();

        Maybe<List<Role>> rolesMaybe = database.roleDao().getRoles(key.getId(), date).defaultIfEmpty(new ArrayList<>());
        Maybe<List<JoinRequest>> requestsMaybe = database.joinRequestDao().getRequests(teamId, date).defaultIfEmpty(new ArrayList<>());

        return Maybe.zip(rolesMaybe, requestsMaybe, (roles, requests) -> {
            List<TeamMember> result = new ArrayList<>(roles.size() + requests.size());

            for (Role role : roles) result.add(TeamMember.fromModel(role));
            for (JoinRequest request : requests) result.add(TeamMember.fromModel(request));

            return result;
        }).cast(List.class);

    }

    @Override
    @SuppressWarnings("unchecked")
    Maybe<List<TeamMember<T>>> remoteModelsBefore(Team key, @Nullable Date date) {
        return api.getTeamMembers(key.getId(), date).toMaybe().cast(List.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    Function<List<TeamMember<T>>, List<TeamMember<T>>> provideSaveManyFunction() {
        return models -> {
            Map<Class, List> classListMap = new HashMap<>();

            Flowable.fromIterable(models).subscribe(model -> {
                Model wrapped = model.getWrappedModel();
                Class modelClass = wrapped.getClass();
                List items = classListMap.get(modelClass);

                if (items == null) classListMap.put(modelClass, items = new ArrayList<>());
                items.add(wrapped);
            });

            List<Role> roles = classListMap.get(Role.class);
            List<JoinRequest> joinRequests = classListMap.get(JoinRequest.class);

            roleRepository.getSaveManyFunction().apply(roles);
            joinRequestRepository.getSaveManyFunction().apply(joinRequests);

            return models;
        };
    }
}
