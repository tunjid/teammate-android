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
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class UserGofer extends Gofer<User> {

    private final Function<User, Boolean> authUserFunction;
    private final Function<User, Flowable<User>> getFunction;
    private final Function<User, Single<User>> updateFunction;

    public UserGofer(User model,
                     Function<User, Boolean> authUserFunction,
                     Function<User, Flowable<User>> getFunction,
                     Function<User, Single<User>> updateFunction) {
        super(model, ErrorHandler.EMPTY);
        this.authUserFunction = authUserFunction;
        this.getFunction = getFunction;
        this.updateFunction = updateFunction;
        items.addAll(filter(new ArrayList<>(model.asItems())));
    }

    @Override
    Flowable<Boolean> changeEmitter() {
        return Flowable.empty();
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        return null;
    }

    @Override
    public Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Differentiable>> listFlowable = getFunction.apply(model).map(User::asDifferentiables);
        return FunctionalDiff.of(listFlowable, getItems(), (stale, updated) -> filter(updated));
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Differentiable>> source = updateFunction.apply(model).map(User::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), (itemsCopy, updated) -> updated);
    }

    public Completable delete() {
        return Completable.error(new TeammateException("Cannot delete"));
    }

    private List<Differentiable> filter(List<Differentiable> list) {
        boolean isAuthUser = authUserFunction.apply(model);
        if (isAuthUser) return list;

        Iterator<Differentiable> it = list.iterator();

        while (it.hasNext()) {
            Differentiable next = it.next();
            if (!(next instanceof Item)) continue;
            if (((Item) next).getStringRes() == R.string.email) it.remove();
        }

        return list;
    }
}
