package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RoleRepository;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends MappedViewModel<Class<Role>, Role> {

    private final RoleRepository roleRepository;

    static final List<Differentiable> roles = new ArrayList<>();

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
    public List<Differentiable> getModelList(Class<Role> roleClass) {
        return roles;
    }

    public Maybe<Competitor> hasPendingCompetitor(Game game) {
        Competitor competitor;
        if (game.getHome().hasNotResponded()) competitor = game.getHome();
        else if (game.getAway().hasNotResponded()) competitor = game.getAway();
        else competitor = null;

        if (competitor == null || competitor.isEmpty() || competitor.isAccepted())
            return Maybe.empty();

        for (Differentiable identifiable : roles) {
            if (!(identifiable instanceof Role)) continue;
            Role role = (Role) identifiable;
            Competitive entity = competitor.getEntity();
            if (matches(role, entity)) return Maybe.just(competitor);
        }
        return Maybe.empty();
    }

    private boolean matches(Role role, Competitive entity) {
        if (entity instanceof User) return role.getUser().equals(entity);
        else if (!(entity instanceof Team)) return false;
        Team team = (Team) entity;
        return role.getTeam().equals(team) && role.isPrivilegedRole();
    }

}
