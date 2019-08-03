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

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.repository.CompetitorRepo;
import com.mainstreetcode.teammate.repository.RepoProvider;
import com.mainstreetcode.teammate.util.FunctionalDiff;
import com.mainstreetcode.teammate.viewmodel.events.Alert;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * View model for User and Auth
 */

public class CompetitorViewModel extends MappedViewModel<Class<User>, Competitor> {

    private final CompetitorRepo repository;
    private final List<Differentiable> declined = new ArrayList<>();

    public CompetitorViewModel() { repository = RepoProvider.Companion.forRepo(CompetitorRepo.class); }

    @Override
    Class<Competitor> valueClass() { return Competitor.class; }

    @Override
    public List<Differentiable> getModelList(Class<User> key) { return declined; }

    public Completable updateCompetitor(Competitor competitor) {
        if (competitor.isEmpty()) return Completable.complete();
        return repository.get(competitor).ignoreElements().observeOn(mainThread());
    }

    public Single<DiffUtil.DiffResult> respond(final Competitor competitor, boolean accept) {
        if (accept) competitor.accept();
        else competitor.decline();
        Single<List<Differentiable>> single = repository.createOrUpdate(competitor).map(Collections::singletonList);
        return FunctionalDiff.of(single, declined, (sourceCopy, fetched) -> {
            if (accept) sourceCopy.removeAll(fetched);
            else sourceCopy.addAll(fetched);

            if (accept) return sourceCopy;

            pushModelAlert(competitor.inOneOffGame()
                    ? Alert.deletion(competitor.getGame())
                    : Alert.deletion(competitor.getTournament()));

            return sourceCopy;
        });
    }

    @Override
    Flowable<List<Competitor>> fetch(Class<User> key, boolean fetchLatest) {
        return repository.getDeclined(getQueryDate(fetchLatest, key, Competitor::getCreated)).toFlowable();
    }
}
