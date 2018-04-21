package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Model;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

abstract class QueryRepository<T extends Model<T>, S extends Model<S>> extends ModelRepository<T> {

    QueryRepository() {}

    public final Flowable<List<T>> modelsBefore(S key, @Nullable Date date) {
        return fetchThenGet(localModelsBefore(key, date), remoteModelsBefore(key, date));
    }

    abstract Maybe<List<T>> localModelsBefore(S key, @Nullable Date date);

    abstract Maybe<List<T>> remoteModelsBefore(S key, @Nullable Date date);
}
