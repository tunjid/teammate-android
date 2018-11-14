package com.mainstreetcode.teammate.repository;


import androidx.annotation.Nullable;

import com.mainstreetcode.teammate.model.Model;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

abstract class QueryRepository<T extends Model<T>, S extends Model<S>, R> extends ModelRepository<T> {

    QueryRepository() {}

    public final Flowable<List<T>> modelsBefore(S key, @Nullable R pagination) {
        return fetchThenGet(localModelsBefore(key, pagination), remoteModelsBefore(key, pagination));
    }

    abstract Maybe<List<T>> localModelsBefore(S key, @Nullable R pagination);

    abstract Maybe<List<T>> remoteModelsBefore(S key, @Nullable R pagination);

    Date getFutureDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, 100);
        return calendar.getTime();
    }
}
