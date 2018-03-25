package com.mainstreetcode.teammate.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

abstract class QueryRepository<T extends Model<T>> extends ModelRepository<T> {

    QueryRepository() {}

    public final Flowable<List<T>> modelsBefore(Team team, @Nullable Date date) {
        return fetchThenGet(localModelsBefore(team, date), remoteModelsBefore(team, date));
    }

    abstract Maybe<List<T>> localModelsBefore(Team team, @Nullable Date date);

    abstract Maybe<List<T>> remoteModelsBefore(Team team, @Nullable Date date);
}
