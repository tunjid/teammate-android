package com.mainstreetcode.teammate.viewmodel.gofers;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.ListableModel;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ModelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

public abstract class Gofer<T extends Model<T> & ListableModel<T>> {

    protected final T model;
    private final Consumer<Throwable> onError;

    final List<Identifiable> items;

    Gofer(T model, Consumer<Throwable> onError) {
        this.model = model;
        this.onError = onError;
        items = new ArrayList<>();
    }

    public static String tag(String seed, Model model) {
        String uuid = model.isEmpty() ? UUID.randomUUID().toString() : model.getId();
        return seed + "-" + uuid;
    }

    @Nullable
    public abstract String getImageClickMessage(Fragment fragment);

    abstract Completable delete();

    abstract Flowable<Boolean> changeEmitter();

    abstract Single<DiffUtil.DiffResult> upsert();

    abstract Flowable<DiffUtil.DiffResult> fetch();

    public final Completable remove() {
        return delete().doOnError(onError).observeOn(mainThread());
    }

    private int count = 0;

    public Flowable<Object> watchForChange() {
        return changeEmitter().filter(changed -> {
            Logger.log("TEST", "CHANGED: " + changed + " Count: " + ++count);
            return changed;
        }).cast(Object.class);
    }

    public final Single<DiffUtil.DiffResult> save() {
        return upsert().doOnError(onError);
    }

    public final Flowable<DiffUtil.DiffResult> get() {
        return model.isEmpty() ? Flowable.empty() : fetch().doOnError(onError);
    }

    public final List<Identifiable> getItems() { return items; }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    void startPrep() { watchForChange().subscribe(ignored -> {}, ErrorHandler.EMPTY); }

    List<Identifiable> preserveItems(List<Identifiable> old, List<Identifiable> fetched) {
        ModelUtils.preserveAscending(old, fetched);
        return old;
    }
}
