package com.mainstreetcode.teammate.viewmodel;


import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.notifications.Notifier;

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

    Flowable<Identifiable> checkForInvalidObject(Flowable<? extends Identifiable> sourceFlowable, K key, V value) {
        return sourceFlowable.cast(Identifiable.class).doOnError(throwable -> checkForInvalidObject(throwable, value, key));
    }

    void onErrorMessage(Message message, K key, Identifiable invalid) {
        if (message.isInvalidObject()) getModelList(key).remove(invalid);
    }

    private void checkForInvalidObject(Throwable throwable, Identifiable model, K key) {
        Message message = fromThrowable(throwable);
        if (message != null) onErrorMessage(message, key, model);
    }

    abstract Flowable<List<V>> fetch(K key, boolean fetchLatest);

    abstract <T extends Model<T>> List<Class<T>> notifiedClasses();

    @SuppressWarnings("unchecked")
    public void clearNotifications(V v) {
        if (!(v instanceof Model)) return;
        Model model = (Model) v;

        Notifier notifier = factory.forClass(model.getClass());
        if (notifier != null) notifier.clearNotifications(model);
    }

    @SuppressWarnings("unchecked")
    public <T extends Model<T>> void clearNotifications() {
        for (Class notifiedClass : notifiedClasses()) {
            Class<T> casted = (Class<T>) notifiedClass;
            Notifier notifier = factory.forClass(casted);
            if (notifier != null) notifier.clearNotifications();
        }
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
}
