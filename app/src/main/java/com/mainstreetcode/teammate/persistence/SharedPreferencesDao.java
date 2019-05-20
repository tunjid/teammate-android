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

package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.mainstreetcode.teammate.App;
import com.mainstreetcode.teammate.model.Device;

import java.util.List;

import io.reactivex.Single;

/**
 * DAO for {@link Device}
 */

@Dao
public abstract class SharedPreferencesDao<T> extends EntityDao<T> {

    private static final String KEY = "SharedPreferencesDao";

    @Override
    protected String getTableName() {
        return preferenceName();
    }

    @NonNull
    abstract T getEmpty();

    @NonNull
    public T getCurrent() {
        SharedPreferences preferences = getPreferences();
        String deserialized = preferences.getString(KEY, "");

        if (TextUtils.isEmpty(deserialized)) return getEmpty();
        return from(deserialized);
    }

    @Override
    public void delete(T model) {
        deleteCurrent();
    }

    public void deleteCurrent() {
        getPreferences().edit().remove(KEY).apply();
    }

    @Override
    public void insert(List<T> models) {
        if (models.isEmpty() || models.size() > 1) return;

        T device = models.get(0);
        getPreferences().edit().putString(KEY, to(device)).apply();
    }

    @Override
    protected void update(List<T> models) {
        insert(models);
    }

    @Override
    public void delete(List<T> models) {
        deleteCurrent();
    }

    @Override
    Single<Integer> deleteAll() {
        return Single.fromCallable(() -> {
            deleteCurrent();
            return 0;
        });
    }

    private SharedPreferences getPreferences() {
        return App.getInstance().getSharedPreferences(getTableName(), Context.MODE_PRIVATE);
    }

    abstract String preferenceName();

    abstract String to(T t);

    abstract T from(String deserialized);
}
