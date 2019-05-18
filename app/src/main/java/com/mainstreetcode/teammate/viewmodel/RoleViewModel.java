/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.viewmodel;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RoleRepo;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * ViewModel for roles in a team
 */

public class RoleViewModel extends MappedViewModel<Class<Role>, Role> {

    private final RoleRepo roleRepository;

    static final List<Differentiable> roles = new ArrayList<>();

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public RoleViewModel() {
        roleRepository = RepoProvider.forRepo(RoleRepo.class);
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
