package com.mainstreetcode.teammates.repository;


import com.mainstreetcode.teammates.model.BaseModel;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static io.reactivex.Maybe.concat;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;

/**
 * Repository that manages model CRUD operations
 */

public abstract class CrudRespository<T extends BaseModel<T>> {

    private final Function<List<T>, List<T>> saveListFunction = provideSaveManyFunction();
    private final Function<T, T> saveFunction = model -> saveListFunction.apply(Collections.singletonList(model)).get(0);

    public abstract Single<T> createOrUpdate(T model);

    public abstract Flowable<T> get(String id);

    public abstract Single<T> delete(T model);

    abstract Function<List<T>, List<T>> provideSaveManyFunction();

    public final Flowable<T> get(T model) {
        if (model.isEmpty()) {
            return Flowable.error(new IllegalArgumentException("Model does not exist"));
        }
        return get(model.getId()).map(localMapper(model));
    }

    final Function<List<T>, List<T>> getSaveManyFunction() {
        return saveListFunction;
    }

    final Function<T, T> getSaveFunction() {
        return saveFunction;
    }

    final Function<T, T> localMapper(T original) {
        return emitted -> {
            original.update(emitted);
            return original;
        };
    }

    static <R> Flowable<R> cacheThenRemote(Maybe<R> local, Maybe<R> remote) {
        return concat(local, remote).observeOn(mainThread(), true);
    }
}
