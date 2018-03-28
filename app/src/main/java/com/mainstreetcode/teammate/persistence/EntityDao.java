package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Delete;

import java.util.List;

import io.reactivex.Single;


public abstract class EntityDao<T> {

    protected abstract String getTableName();

    protected abstract void insert(List<T> models);

    protected abstract void update(List<T> models);

    @Delete
    public abstract void delete(T model);

    @Delete
    public abstract void delete(List<T> models);

    public void upsert(List<T> models) {
        insert(models);
        update(models);
    }

    Single<Integer> deleteAll() {
        final String sql = "DELETE FROM " + getTableName();
        return Single.fromCallable(() -> AppDatabase.getInstance().compileStatement(sql).executeUpdateDelete());
    }
}
