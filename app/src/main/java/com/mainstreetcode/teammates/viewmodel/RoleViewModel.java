package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.repository.RoleRepository;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.ReplaySubject;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class RoleViewModel extends ViewModel {

    private final RoleRepository repository;

    private TeammateApi api = TeammateService.getApiInstance();
    private ReplaySubject<List<String>> roleSubject;

    public RoleViewModel() {
        repository = RoleRepository.getInstance();
    }

    public Observable<List<String>> getRoleValues() {
        // Use new subject if previous call errored out for whatever reason.
        if (roleSubject == null || roleSubject.hasThrowable()) {
            roleSubject = ReplaySubject.createWithSize(1);
            api.getRoleValues().subscribe(roleSubject);
        }
        return roleSubject;
    }

    public Observable<Role> updateRole(Role role) {
        return repository.createOrUpdate(role);
    }

    public Observable<Role> approveUser(JoinRequest request) {
        return repository.approveUser(request);
    }

    public Observable<Role> dropRole(Role role) {
        return repository.dropRole(role);
    }
}
