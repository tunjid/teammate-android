package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends MappedViewModel<Class<Role>, Role> {

    private final RoleRepository roleRepository;

    static final List<Identifiable> roles = new ArrayList<>();

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public RoleViewModel() {
        roleRepository = RoleRepository.getInstance();
        fetch(Role.class, false).subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

    @Override
    boolean sortsAscending() {
        return true;
    }

    @Override
    Flowable<List<Role>> fetch(Class<Role> roleClass, boolean fetchLatest) {
        return roleRepository.getMyRoles();
    }

    @Override
    public List<Identifiable> getModelList(Class<Role> roleClass) {
        return roles;
    }
}
