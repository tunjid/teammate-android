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

import androidx.arch.core.util.Function;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.PublishProcessor;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class InstantSearch<T, R> {

    private static final int SEARCH_DEBOUNCE = 300;
    private final Function<T, Single<List<R>>> searcher;
    private AtomicReference<PublishProcessor<T>> searchRef;

    public InstantSearch(Function<T, Single<List<R>>> searcher) {
        this.searcher = searcher;
        searchRef = new AtomicReference<>();
    }

    public Flowable<List<R>> subscribe() {
        if (searchRef.get() == null) searchRef.set(PublishProcessor.create());
        return searchRef.get()
                .debounce(SEARCH_DEBOUNCE, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .switchMap(debounced -> searcher.apply(debounced).toFlowable())
                .doFinally(() -> searchRef.set(null))
                .observeOn(mainThread());
    }

    public boolean postSearch(T query) {
        if (searchRef.get() == null) return false;
        searchRef.get().onNext(query);
        return true;
    }
}
