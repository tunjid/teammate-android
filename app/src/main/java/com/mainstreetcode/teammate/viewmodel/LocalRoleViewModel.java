package com.mainstreetcode.teammate.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

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

    public Single<Boolean> hasRoles() {
        List<Role> roles = new ArrayList<>();
        return Flowable.fromIterable(RoleViewModel.roles)
                .filter(identifiable -> identifiable instanceof Role)
                .cast(Role.class)
                .collectInto(roles, List::add)
                .map(list -> !list.isEmpty());
    }
    private Role onRoleFound(Role foundRole) {
        role.update(foundRole);
        return role;
    }
}
