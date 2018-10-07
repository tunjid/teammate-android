package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
