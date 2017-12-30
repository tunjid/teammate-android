package com.mainstreetcode.teammates.viewmodel;


import android.arch.lifecycle.ViewModel;

import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.util.ModelUtils;

import java.util.List;

import io.reactivex.Flowable;

public abstract class MappedViewModel<K, V extends Model> extends ViewModel {


    public abstract List<V> getModelList(K key);

    Flowable<V> checkForInvalidObject(Flowable<V> sourceFlowable, V value, K key) {
        return sourceFlowable.doOnError(throwable -> ModelUtils.checkForInvalidObject(throwable, value, getModelList(key)));
    }
}
