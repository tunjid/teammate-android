package com.mainstreetcode.teammate.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public class Deferrer {

    private final long delay;
    private final Runnable toRun;
    private final AtomicReference<Disposable> ref;

    public Deferrer(long delay, Runnable toRun) {
        this.delay = delay;
        this.toRun = toRun;
        ref = new AtomicReference<>();
    }

    public final void advanceDeadline() {
        Disposable disposable = ref.get();
        if (disposable != null) disposable.dispose();
        ref.set(Completable.timer(delay, TimeUnit.MILLISECONDS).observeOn(mainThread()).subscribe(toRun::run));
    }

}
