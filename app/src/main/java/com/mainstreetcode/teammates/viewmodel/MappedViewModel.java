package com.mainstreetcode.teammates.viewmodel;


import android.support.v7.util.DiffUtil;

import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Message;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

import static com.mainstreetcode.teammates.util.ModelUtils.fromThrowable;

public abstract class MappedViewModel<K, V extends Identifiable> extends BaseViewModel {

    Function<List<V>, List<Identifiable>> toIdentifiable = ArrayList<Identifiable>::new;

    private BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>> pullToRefreshFunction =
            new BiFunction<List<Identifiable>, List<Identifiable>, List<Identifiable>>() {
                int count = 0;

                @Override
                public List<Identifiable> apply(List<Identifiable> stale, List<Identifiable> fetched) throws Exception {
                    if (++count == 1) stale.clear();
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
