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

import android.annotation.SuppressLint;
import androidx.arch.core.util.Function;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class TournamentGofer extends TeamHostingGofer<Tournament> {

    private static final int CREATING = 0;
    private static final int EDITING = 1;

    private int state;
    private final Function<Tournament, Flowable<Tournament>> getFunction;
    private final Function<Tournament, Single<Tournament>> deleteFunction;
    private final Function<Tournament, Single<Tournament>> updateFunction;
    private final Function<Tournament, Flowable<List<Competitor>>> competitorsFunction;

    @SuppressLint("CheckResult")
    public TournamentGofer(Tournament model,
                           Consumer<Throwable> onError,
                           Function<Tournament, Flowable<Tournament>> getFunction,
                           Function<Tournament, Single<Tournament>> upsertFunction,
                           Function<Tournament, Single<Tournament>> deleteFunction,
                           Function<Tournament, Flowable<List<Competitor>>> competitorsFunction) {
        super(model, onError);
        this.getFunction = getFunction;
        this.updateFunction = upsertFunction;
        this.deleteFunction = deleteFunction;
        this.competitorsFunction = competitorsFunction;

        items.addAll(model.asItems());
        state = model.isEmpty() ? CREATING : EDITING;
    }

    public boolean canEditBeforeCreation() {
        return canEditAfterCreation() && model.isEmpty();
    }

    public boolean canEditAfterCreation() {
        return state == CREATING || hasPrivilegedRole();
    }

    public String getToolbarTitle(Fragment fragment) {
        return model.isEmpty()
                ? fragment.getString(R.string.create_tournament)
                : fragment.getString(R.string.edit_tournament, model.getName());
    }

    @Nullable
    @Override
    public String getImageClickMessage(Fragment fragment) {
        if (state == CREATING) return fragment.getString(R.string.create_tournament_first);
        else if (!hasPrivilegedRole()) return fragment.getString(R.string.no_permission);
        return null;
    }

    @Override
    Flowable<DiffUtil.DiffResult> fetch() {
        Flowable<List<Differentiable>> eventFlowable = getFunction.apply(model).map(Tournament::asDifferentiables);
        Flowable<List<Differentiable>> competitorsFlowable = competitorsFunction.apply(model).map(ModelUtils::asDifferentiables);
        Flowable<List<Differentiable>> sourceFlowable = Flowable.mergeDelayError(eventFlowable, competitorsFlowable);
        return FunctionalDiff.of(sourceFlowable, getItems(), this::preserveItems);
    }

    Single<DiffUtil.DiffResult> upsert() {
        Single<List<Differentiable>> source = updateFunction.apply(model).map(Tournament::asDifferentiables);
        return FunctionalDiff.of(source, getItems(), this::preserveItems);
    }

    Completable delete() {
        return deleteFunction.apply(model).toCompletable();
    }
}
