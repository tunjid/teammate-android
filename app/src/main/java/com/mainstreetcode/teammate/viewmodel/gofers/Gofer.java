package com.mainstreetcode.teammate.viewmodel.gofers;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.ListableModel;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public abstract class Gofer<T extends Model<T> & ListableModel<T>> {

    protected final T model;
    private final Consumer<Throwable> onError;

    Gofer(T model, Consumer<Throwable> onError) {
        this.model = model;
        this.onError = onError;
    }

    public static String tag(String seed, Model model) {
        String uuid = model.isEmpty() ? UUID.randomUUID().toString() : model.getId();
        return seed + "-" + uuid;
    }

    abstract Single<DiffUtil.DiffResult> upsert();

    abstract Flowable<DiffUtil.DiffResult> fetch();

    abstract Completable delete();

    public final Single<DiffUtil.DiffResult> save() { return upsert().doOnSuccess(ignored -> startPrep()).doOnError(onError); }

    public final Flowable<DiffUtil.DiffResult> get() { return model.isEmpty() ? Flowable.empty() : fetch().doOnNext(ignored -> startPrep()).doOnError(onError); }

    public final Completable remove() { return delete().doOnError(onError).observeOn(mainThread()); }

    public abstract Completable prepare();

    @Nullable
    public abstract String getImageClickMessage(Fragment fragment);

    @SuppressLint("CheckResult")
    void startPrep() { prepare().subscribe(() -> {}, ErrorHandler.EMPTY); }
}
