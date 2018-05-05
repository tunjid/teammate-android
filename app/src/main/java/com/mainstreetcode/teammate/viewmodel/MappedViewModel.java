package com.mainstreetcode.teammate.viewmodel;


import android.annotation.SuppressLint;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.Notifier;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;

import static com.mainstreetcode.teammate.model.Message.fromThrowable;

public abstract class MappedViewModel<K, V extends Identifiable> extends BaseViewModel {

    private AtomicInteger pullToRefreshCount = new AtomicInteger(0);
    private Notifier.NotifierFactory factory = new Notifier.NotifierFactory();

    MappedViewModel() {}

    public abstract List<Identifiable> getModelList(K key);

    abstract Flowable<List<V>> fetch(K key, boolean fetchLatest);

    Flowable<Identifiable> checkForInvalidObject(Flowable<? extends Identifiable> source, K key, V value) {
        return source.cast(Identifiable.class).doOnError(throwable -> checkForInvalidObject(throwable, value, key));
    }

    public Flowable<DiffUtil.DiffResult> getMany(K key, boolean fetchLatest) {
        return fetchLatest ? getLatest(key) : getMore(key);
    }

    public Flowable<DiffUtil.DiffResult> getMore(K key) {
        return Identifiable.diff(fetch(key, false).map(this::toIdentifiable), () -> getModelList(key), this::preserveList);
    }

    public Flowable<DiffUtil.DiffResult> refresh(K key) {
        return Identifiable.diff(fetch(key, true).map(this::toIdentifiable), () -> getModelList(key), this::pullToRefresh);
    }

    private Flowable<DiffUtil.DiffResult> getLatest(K key) {
        return Identifiable.diff(fetch(key, true).map(this::toIdentifiable), () -> getModelList(key), this::preserveList)
                .doOnTerminate(() -> pullToRefreshCount.set(0));
    }

    public void clearNotifications(V value) {
        clearNotification(notificationCancelMap(value));
    }

    @SuppressLint("CheckResult")
    public void clearNotifications(K key) {
        Flowable.fromIterable(getModelList(key))
                .map(this::notificationCancelMap)
                .subscribe(this::clearNotification, ErrorHandler.EMPTY);
    }

    private List<Identifiable> pullToRefresh(List<Identifiable> source, List<Identifiable> additions) {
        if (pullToRefreshCount.getAndIncrement() == 0) source.clear();

        preserveList(source, additions);
        afterPullToRefreshDiff(source);
        return source;
    }

    void afterPullToRefreshDiff(List<Identifiable> source) {}

    void onErrorMessage(Message message, K key, Identifiable invalid) {
        if (message.isInvalidObject()) getModelList(key).remove(invalid);
    }

    Pair<Model, Class> notificationCancelMap(Identifiable identifiable) {
        if (!(identifiable instanceof Model)) return new Pair<>(null, null);
        Model model = (Model) identifiable;
        return new Pair<>(model, model.getClass());
    }

    @SuppressWarnings("unchecked")
    private void clearNotification(Pair<Model, Class> pair) {
        if (pair.first == null || pair.second == null) return;
        Notifier notifier = factory.forClass(pair.second);
        if (notifier != null) notifier.clearNotifications(pair.first);
    }

    void checkForInvalidObject(Throwable throwable, V model, K key) {
        Message message = fromThrowable(throwable);
        if (message != null) onErrorMessage(message, key, model);
    }

    final List<Identifiable> toIdentifiable(List<V> source) {
        return new ArrayList<>(source);
    }
}
