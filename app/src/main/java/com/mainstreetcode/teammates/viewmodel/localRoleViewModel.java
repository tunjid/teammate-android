package com.mainstreetcode.teammates.viewmodel;

import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.User;
import com.mainstreetcode.teammates.repository.RoleRepository;

import io.reactivex.Maybe;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * ViewModel for signed in team
 * <p>
 * Created by Shemanigans on 6/4/17.
 */

public class localRoleViewModel extends ViewModel {

    private Role role = Role.empty();

    private final RoleRepository repository;

    public localRoleViewModel() {
        repository = RoleRepository.getInstance();
    }

    public Maybe<Role> getRoleInTeam(User user, Team team) {
        return !role.isEmpty() ? Maybe.just(role) : repository.getRoleInTeam(user.getId(), team.getId())
                .map(this::onRoleFound).observeOn(mainThread());
    }

    private Role onRoleFound(Role foundRole) {
        role.update(foundRole);
        return role;
    }
}
