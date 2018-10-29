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
