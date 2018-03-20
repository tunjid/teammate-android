package com.mainstreetcode.teammate.viewmodel;


import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.Notifier;
import com.mainstreetcode.teammate.util.ErrorHandler;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static com.mainstreetcode.teammate.util.ModelUtils.fromThrowable;

public abstract class MappedViewModel<K, V extends Identifiable> extends BaseViewModel {

    private Notifier.NotifierFactory factory = new Notifier.NotifierFactory();

    MappedViewModel() {}

    Function<List<V>, List<Identifiable>> toIdentifiable = ArrayList<Identifiable>::new;

    private BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>> pullToRefreshFunction =
            new BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>>() {
                int count = 0;

                @Override
                public List<Identifiable> apply(List<Identifiable> stale, List<Identifiable> fetched) throws Exception {
                    if (++count == 1) stale.clear();
                    if (count >= 2) count = 0;
                    preserveList.apply(stale, fetched);
                    return stale;
                }
            };

    public abstract List<Identifiable> getModelList(K key);

    abstract Flowable<List<V>> fetch(K key, boolean fetchLatest);

    Flowable<Identifiable> checkForInvalidObject(Flowable<? extends Identifiable> sourceFlowable, K key, V value) {
        return sourceFlowable.cast(Identifiable.class).doOnError(throwable -> checkForInvalidObject(throwable, value, key));
    }

    public Flowable<DiffUtil.DiffResult> getMore(K key) {
        return Identifiable.diff(fetch(key, false).map(toIdentifiable), () -> getModelList(key), preserveList);
    }

    public Flowable<DiffUtil.DiffResult> getLatest(K key) {
        return Identifiable.diff(fetch(key, true).map(toIdentifiable), () -> getModelList(key), preserveList);
    }

    public Flowable<DiffUtil.DiffResult> refresh(K key) {
        return Identifiable.diff(fetch(key, true).map(toIdentifiable), () -> getModelList(key), pullToRefreshFunction);
    }

    public void clearNotifications(V value) {
        clearNotification(notificationCancelMap(value));
    }

    public void clearNotifications(K key) {
        Flowable.fromIterable(getModelList(key))
                .map(this::notificationCancelMap)
                .subscribe(this::clearNotification, ErrorHandler.EMPTY);
    }

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

    private void checkForInvalidObject(Throwable throwable, Identifiable model, K key) {
        Message message = fromThrowable(throwable);
        if (message != null) onErrorMessage(message, key, model);
    }
}
