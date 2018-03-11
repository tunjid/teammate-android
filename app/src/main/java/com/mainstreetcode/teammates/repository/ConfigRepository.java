package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.Config;
import com.mainstreetcode.teammates.persistence.AppDatabase;
import com.mainstreetcode.teammates.persistence.ConfigDao;
import com.mainstreetcode.teammates.persistence.EntityDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class ConfigRepository extends ModelRepository<Config> {

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
        Config device = dao.getCurrent();
        return device == null
                ? api.getConfig().map(getSaveFunction()).toFlowable()
                : Flowable.just(device);
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
}
