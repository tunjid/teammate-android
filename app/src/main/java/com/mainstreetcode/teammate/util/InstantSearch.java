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

package com.mainstreetcode.teammate.util;

import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Diff;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.arch.core.util.Function;
import androidx.recyclerview.widget.DiffUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class InstantSearch<T, R> {

    private static final int SEARCH_DEBOUNCE = 300;

    private final PublishProcessor<T> searchProcessor;
    private final List<R> currentItems = new ArrayList<>();
    private final Flowable<DiffUtil.DiffResult> searchFlowable;

    public InstantSearch(Function<T, Single<? extends List<? extends R>>> searcher, Function<R, Differentiable> diffFunction) {
        searchProcessor = PublishProcessor.create();
        searchFlowable = searchProcessor
                .debounce(SEARCH_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMapSingle(searcher::apply)
                .map(nextItems -> Diff.calculate(
                        currentItems,
                        cast(nextItems),
                        (__, next) -> next,
                        diffFunction::apply
                ))
                .observeOn(mainThread())
                .doOnNext(diff -> Lists.replace(currentItems, diff.items))
                .map(diff -> diff.result);
    }

    public void postSearch(T query) { searchProcessor.onNext(query); }

    public List<R> getCurrentItems() { return currentItems; }

    public Flowable<DiffUtil.DiffResult> subscribe() { return searchFlowable; }

    private List<R> cast(List<? extends R> source) { return Lists.transform(source, item -> (R) item); }
}
