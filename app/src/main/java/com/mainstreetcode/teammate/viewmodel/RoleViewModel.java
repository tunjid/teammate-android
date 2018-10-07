package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
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
        getMore(Role.class).subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

    @Override
    boolean sortsAscending() {
        return true;
    }

    @Override
    Class<Role> valueClass() { return Role.class; }

    @Override
    Flowable<List<Role>> fetch(Class<Role> roleClass, boolean fetchLatest) {
        return roleRepository.getMyRoles();
    }

    @Override
    public List<Identifiable> getModelList(Class<Role> roleClass) {
        return roles;
    }

    public boolean privilegedInGame(Game game) {
        Competitive home = game.getHome().getEntity();
        Competitive away = game.getAway().getEntity();

        for (Identifiable identifiable : roles) {
            if (!(identifiable instanceof Role)) continue;
            Role role = (Role) identifiable;
            if (!game.betweenUsers() && !role.isPrivilegedRole()) continue;
            if (matches(role, home)) return true;
            if (matches(role, away)) return true;
        }
        return false;
    }

    private boolean matches(Role role, Competitive entity) {
        if (entity instanceof User) return role.getUser().equals(entity);
        else if (entity instanceof Team) return role.getTeam().equals(entity);
        return false;
    }

}
