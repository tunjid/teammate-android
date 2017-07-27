package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class RoleViewModel extends ViewModel {

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

    public Maybe<Role> getRoleInTeam(String userId, String teamId) {
        return repository.getRoleInTeam(userId, teamId);
    }

    public Single<Role> updateRole(Role role) {
        return repository.createOrUpdate(role);
    }

    public Single<Role> approveUser(JoinRequest request) {
        return repository.approveUser(request);
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest);
    }

    public Single<JoinRequest> declineUser(JoinRequest request) {
        return joinRequestRepository.dropJoinRequest(request);
    }

    public Single<Role> dropRole(Role role) {
        return repository.dropRole(role);
    }
}
