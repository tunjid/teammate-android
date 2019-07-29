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

import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.repository.UserRepo;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class RoleGofer extends TeamHostingGofer<Role> {

    private final Function<Role, Flowable<Role>> getFunction;
    private final Function<Role, Single<Role>> deleteFunction;
    private final Function<Role, Single<Role>> updateFunction;

    public RoleGofer(Role model, Consumer<Throwable> onError, Function<Role, Flowable<Role>> getFunction, Function<Role, Single<Role>> deleteFunction, Function<Role, Single<Role>> updateFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.deleteFunction = deleteFunction;
        this.updateFunction = updateFunction;
        this.items.addAll(model.asItems());
    }

    public boolean canChangeRoleFields() {
        return hasPrivilegedRole() || getSignedInUser().equals(model.getUser());
    }

    public String getDropRolePrompt(Fragment fragment) {
        User roleUser = model.getUser();
        return RepoProvider.forRepo(UserRepo.class).getCurrentUser().equals(roleUser)
                ? fragment.getString(R.string.confirm_user_leave)
                : fragment.getString(R.string.confirm_user_drop, roleUser.getFirstName());
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        if (hasPrivilegedRole() || getSignedInUser().equals(model.getUser())) return null;
        return fragment.getString(R.string.no_permission);
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Differentiable>> source = getFunction.apply(model).map(Role::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Differentiable>> source = updateFunction.apply(model).map(Role::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        return deleteFunction.apply(model).ignoreElement();
    }
}
