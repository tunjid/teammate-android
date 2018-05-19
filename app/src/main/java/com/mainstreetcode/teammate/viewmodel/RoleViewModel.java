package com.mainstreetcode.teammate.viewmodel;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends MappedViewModel<Class<Role>, Role> {

    private final RoleRepository roleRepository;

    static final List<Identifiable> roles = new ArrayList<>();

    public RoleViewModel() {
        roleRepository = RoleRepository.getInstance();
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
