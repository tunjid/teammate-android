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

package com.mainstreetcode.teammate.viewmodel.gofers;

import com.mainstreetcode.teammate.model.ListableModel;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.TeamHost;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.RoleRepo;
import com.mainstreetcode.teammate.repository.UserRepo;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * Interface for liaisons between a ViewModel and a single instance of it's Model
 */
public abstract class TeamHostingGofer<T extends Model<T> & ListableModel<T> & TeamHost> extends Gofer<T> {

    private Role currentRole;
    private final UserRepo userRepository;
    private final RoleRepo roleRepository;

    TeamHostingGofer(T model, Consumer<Throwable> onError) {
        super(model, onError);
        currentRole = Role.empty();
        userRepository = RepoProvider.forRepo(UserRepo.class);
        roleRepository = RepoProvider.forRepo(RoleRepo.class);

        startPrep();
    }

    @Override
    Flowable<Boolean> changeEmitter() {
        return roleRepository.getRoleInTeam(userRepository.getCurrentUser().getId(), model.getTeam().getId())
                .map(this::onRoleFound).observeOn(mainThread());
    }

    public boolean hasRole() {return !currentRole.isEmpty();}

    public boolean hasPrivilegedRole() {
        return currentRole.isPrivilegedRole();
    }

    User getSignedInUser() {
        return userRepository.getCurrentUser();
    }

    private boolean onRoleFound(Role foundRole) {
        boolean changed = !currentRole.getPosition().equals(foundRole.getPosition());
        currentRole.update(foundRole);
        return changed;
    }
}
