package com.mainstreetcode.teammate.util;

import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class UrlDiff {

    private final AtomicReference<PublishSubject<String>> ref = new AtomicReference<>();
    private final ModelUtils.Consumer<String> consumer;
    private final CompositeDisposable disposable = new CompositeDisposable();

    public UrlDiff(ModelUtils.Consumer<String> consumer) {
        this.consumer = consumer;
        watch();
    }

    public void push(String url) { ref.get().onNext(url); }

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
