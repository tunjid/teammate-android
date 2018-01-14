package com.mainstreetcode.teammates.viewmodel;


import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Message;

import java.util.LinkedList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import static com.mainstreetcode.teammates.util.ModelUtils.fromThrowable;

public abstract class MappedViewModel<K, V extends Identifiable> extends BaseViewModel {

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

    Function<List<V>, List<Identifiable>> toIdentifiable = LinkedList<Identifiable>::new;
}
