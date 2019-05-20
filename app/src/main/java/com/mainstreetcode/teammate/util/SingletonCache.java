/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
