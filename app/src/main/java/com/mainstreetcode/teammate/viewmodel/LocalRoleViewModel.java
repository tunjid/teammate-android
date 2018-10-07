package com.mainstreetcode.teammate.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RoleRepository;

import io.reactivex.Flowable;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for checking a role in local contexts
 */

public class LocalRoleViewModel extends ViewModel {

    private Role role = Role.empty();

    private final RoleRepository repository;

    public LocalRoleViewModel() {
        repository = RoleRepository.getInstance();
    }

    public boolean hasPrivilegedRole() {
        return role.isPrivilegedRole();
    }

    public Flowable<Object> watchRoleChanges(User user, Team team) {
        return repository.getRoleInTeam(user.getId(), team.getId())
                .map(this::checkChanged)
                .filter(flag -> flag)
                .observeOn(mainThread())
                .cast(Object.class);
    }

    private boolean checkChanged(Role foundRole) {
        boolean changed = !role.getPosition().equals(foundRole.getPosition());
        role.update(foundRole);
        return changed;
    }
}
