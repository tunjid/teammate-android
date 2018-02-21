package com.mainstreetcode.teammates.repository;


import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Team;

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
