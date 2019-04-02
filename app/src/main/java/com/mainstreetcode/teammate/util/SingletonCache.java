package com.mainstreetcode.teammate.util;

import android.util.Pair;

import com.tunjid.androidbootstrap.functions.Function;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

public class SingletonCache<O, T> {

    private final Map<Class<? extends T>, T> instanceMap = new HashMap<>();
    private final Function<Class<? extends O>, Class<? extends T>> function;
    private final Function<Class<? extends T>, T> defaultFunction;

    @SafeVarargs
    public SingletonCache(Function<Class<? extends O>, Class<? extends T>> function,
                          Function<Class<? extends T>, T> defaultFunction,
                          Pair<Class<? extends T>, T>... pairs) {
        this.function = function;
        this.defaultFunction = defaultFunction;
        for (Pair<Class<? extends T>, T> pair : pairs) instanceMap.put(pair.first, pair.second);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public T forInstance(Class<? extends T> itemClass) {
        T result = instanceMap.get(itemClass);
        return result != null ? result : defaultFunction.apply(itemClass);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public T forModel(Class<? extends O> itemClass) {
        Class<? extends T> aClass = function.apply(itemClass);
        T result = instanceMap.get(aClass);
        return result != null ? result : forInstance(aClass);
    }

}
