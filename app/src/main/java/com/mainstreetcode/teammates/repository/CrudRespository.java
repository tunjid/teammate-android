package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.Model;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

import static io.reactivex.Observable.just;

/**
 * Repository that manages model CRUD operations
 */

public abstract class CrudRespository<T extends Model> {

    public abstract Observable<T> createOrUpdate(T model);

    public abstract Observable<T> get(String id);

    public abstract Observable<T> delete(T model);

    abstract Observable<List<T>> saveList(List<T> model);

    Observable<T> save(T model) {
        return saveList(Collections.singletonList(model)).flatMap(list -> just(list.get(0)));
    }

    Observable<T> updateLocal(T source, T updated) {
        source.update(updated);
        return save(updated);
    }
}
