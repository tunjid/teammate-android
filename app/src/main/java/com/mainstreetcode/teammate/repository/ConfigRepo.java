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

package com.mainstreetcode.teammate.repository;

import android.annotation.SuppressLint;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.ConfigDao;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class ConfigRepo extends ModelRepo<Config> {

    private static final int REFRESH_THRESHOLD = 10;

    private int numRefreshes = 0;
    private int retryPeriod = 3;

    private final TeammateApi api;
    private final ConfigDao dao;

    ConfigRepo() {
        api = TeammateService.getApiInstance();
        dao = AppDatabase.getInstance().configDao();
    }

    public Config getCurrent() { return dao.getCurrent(); }

    @Override
    public EntityDao<? super Config> dao() {
        return dao;
    }

    @Override
    public Single<Config> createOrUpdate(Config model) {
        return Single.error(new TeammateException("Can't create config locally"));
    }

    @Override
    public Flowable<Config> get(String ignored) {
        Config config = dao.getCurrent();
        return config.isEmpty()
                ? api.getConfig().map(getSaveFunction()).toFlowable()
                : Flowable.just(config).doFinally(this::refreshConfig);
    }

    @Override
    public Single<Config> delete(Config model) {
        dao.deleteCurrent();
        return Single.just(model);
    }

    @Override
    Function<List<Config>, List<Config>> provideSaveManyFunction() {
        return (List<Config> devices) -> {
            dao.upsert(devices);
            return devices;
        };
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void refreshConfig() {
        if (numRefreshes++ % REFRESH_THRESHOLD != 0) return;
        api.getConfig().map(getSaveFunction())
                .onErrorResumeNext(retryConfig()::apply)
                .subscribe(ignored -> {}, ErrorHandler.EMPTY);
    }

    private Function<Throwable, Single<Config>> retryConfig() {
        return throwable -> {
            numRefreshes = 0;
            retryPeriod *= retryPeriod;
            retryPeriod = Math.min(retryPeriod, 60);
            return Completable.timer(retryPeriod, TimeUnit.SECONDS)
                    .andThen(api.getConfig().map(getSaveFunction()).onErrorResumeNext(retryConfig()::apply));
        };
    }
}
