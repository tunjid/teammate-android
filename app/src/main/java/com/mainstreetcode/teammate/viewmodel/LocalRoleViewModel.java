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
