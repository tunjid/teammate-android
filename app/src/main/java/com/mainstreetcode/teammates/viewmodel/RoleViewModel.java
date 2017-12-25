package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.repository.JoinRequestRepository;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for roles in a team
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

    public Single<Role> updateRole(Role role) {
        return repository.createOrUpdate(role).observeOn(mainThread());
    }

    public Single<Role> approveUser(JoinRequest request) {
        return repository.approveUser(request).observeOn(mainThread());
    }

    public Single<JoinRequest> joinTeam(JoinRequest joinRequest) {
        return joinRequestRepository.createOrUpdate(joinRequest).observeOn(mainThread());
    }

    public Single<JoinRequest> declineUser(JoinRequest request) {
        return joinRequestRepository.dropJoinRequest(request).observeOn(mainThread());
    }

    public Single<Role> dropRole(Role role) {
        return repository.dropRole(role).observeOn(mainThread());
    }
}
