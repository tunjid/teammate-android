package com.mainstreetcode.teammates.viewmodel;


import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Model;

import java.util.List;

import io.reactivex.Flowable;
import retrofit2.HttpException;

public abstract class MappedViewModel<K, V extends Model> extends ViewModel {

    public abstract List<V> getModelList(K key);

    Flowable<V> checkForInvalidObject(Flowable<V> sourceFlowable, V value, K key) {
        return sourceFlowable.doOnError(throwable -> checkForInvalidObject(throwable, value, key));
    }

    void onErrorMessage(Message message, K key, V invalid) {
        if (message.isInvalidObject()) getModelList(key).remove(invalid);
    }

    private void checkForInvalidObject(Throwable throwable, V model, K key) {
        Message message = fromThrowable(throwable);
        if (message != null) onErrorMessage(message, key, model);
    }

    @Nullable
    private Message fromThrowable(Throwable throwable) {
        if (!(throwable instanceof HttpException)) return null;
        return new Message((HttpException) throwable);
    }
}
