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

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class ConfigRepository extends ModelRepository<Config> {

    private static final int REFRESH_THRESHOLD = 10;

    private int numRefreshes = 0;
    private final TeammateApi api;
    private final ConfigDao dao;

    private static ConfigRepository ourInstance;

    private ConfigRepository() {
        api = TeammateService.getApiInstance();
        dao = AppDatabase.getInstance().configDao();
    }

    public static ConfigRepository getInstance() {
        if (ourInstance == null) ourInstance = new ConfigRepository();
        return ourInstance;
    }

    public Config getCurrent() {
        Config current = dao.getCurrent();
        return current == null ? Config.empty() : current;
    }

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
        return config == null || config.isEmpty()
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
    private void refreshConfig() {
        if (++numRefreshes < REFRESH_THRESHOLD && numRefreshes != 0) return;
        numRefreshes = 0;
        api.getConfig().map(getSaveFunction()).map(Collections::singletonList).subscribe(dao::upsert, ErrorHandler.EMPTY);
    }
}
