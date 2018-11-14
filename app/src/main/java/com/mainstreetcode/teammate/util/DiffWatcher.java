package com.mainstreetcode.teammate.util;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class DiffWatcher<T> {

    private final AtomicReference<PublishSubject<T>> ref = new AtomicReference<>();
    private final ModelUtils.Consumer<T> consumer;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public DiffWatcher(ModelUtils.Consumer<T> consumer) {
        this.consumer = consumer;
        watch();
    }

    public void push(T item) { ref.get().onNext(item); }

    public void stop() { disposable.clear(); }

    public void restart() {
        stop();
        watch();
    }

    private void watch() {
        ref.set(PublishSubject.create());
        disposable.add(ref.get().distinct().subscribe(consumer::accept, ErrorHandler.EMPTY));
    }
}
