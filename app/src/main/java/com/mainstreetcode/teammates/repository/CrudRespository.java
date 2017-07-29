package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Model;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

import static io.reactivex.Single.just;

/**
 * Repository that manages model CRUD operations
 */

public abstract class CrudRespository<T extends Model> {

    public abstract Single<T> createOrUpdate(T model);

    public abstract Flowable<T> get(String id);

    public abstract Single<T> delete(T model);

    abstract Single<List<T>> saveList(List<T> models);

    public Flowable<T> get(T model) {
        return get(model.getId()).map(emiitedModel -> {
            model.update(emiitedModel);
            return model;
        });
    }

    Single<T> save(T model) {
        return saveList(Collections.singletonList(model)).flatMap(list -> just(list.get(0)));
    }

    Single<T> updateLocal(T source, T updated) {
        source.update(updated);
        return save(updated);
    }
}
