package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.RoleRepository;

import io.reactivex.Completable;

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

    public Role getCurrentRole() {
        return role;
    }

    public Completable getRoleInTeam(User user, Team team) {
        return !role.isEmpty() ? Completable.complete() : repository.getRoleInTeam(user.getId(), team.getId())
                .map(this::onRoleFound).observeOn(mainThread()).flatMapCompletable(role1 -> Completable.complete());
    }

    private Role onRoleFound(Role foundRole) {
        role.update(foundRole);
        return role;
    }
}
