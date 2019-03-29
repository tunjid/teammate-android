package com.mainstreetcode.teammate.viewmodel;

import androidx.lifecycle.ViewModel;

import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.RoleRepo;

import java.util.Arrays;

import io.reactivex.Flowable;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for checking a role in local contexts
 */

public class LocalRoleViewModel extends ViewModel {

    private Role role = Role.empty();

    private final RoleRepo repository;

    public LocalRoleViewModel() {
        repository = RepoProvider.forRepo(RoleRepo.class);
    }

    public boolean hasPrivilegedRole() {
        return role.isPrivilegedRole();
    }

    public Flowable<Object> watchRoleChanges(User user, Team team) {
        return matchInUserRoles(user, team)
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

    private Flowable<Role> matchInUserRoles(User user, Team team) {
        Flowable<Role> inMemory = Flowable.fromIterable(RoleViewModel.roles)
                .filter(role -> role instanceof Role)
                .cast(Role.class)
                .filter(role -> user.equals(role.getUser()) && team.equals(role.getTeam()));

        Flowable<Role> fromIo = repository.getRoleInTeam(user.getId(), team.getId());

        return Flowable.concatDelayError(Arrays.asList(inMemory, fromIo));
    }
}
