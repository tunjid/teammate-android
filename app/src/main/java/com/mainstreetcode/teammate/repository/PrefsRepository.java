package com.mainstreetcode.teammate.repository;

import com.mainstreetcode.teammate.model.Prefs;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.PrefsDao;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class PrefsRepository extends ModelRepository<Prefs> {

    private final PrefsDao dao;

    private static PrefsRepository ourInstance;

    private PrefsRepository() { dao = AppDatabase.getInstance().prefsDao(); }

    public static PrefsRepository getInstance() {
        if (ourInstance == null) ourInstance = new PrefsRepository();
        return ourInstance;
    }

    public Prefs getCurrent() { return dao.getCurrent(); }

    @Override
    public EntityDao<? super Prefs> dao() {
        return dao;
    }

    @Override
    public Single<Prefs> createOrUpdate(Prefs model) {
        dao.insert(Collections.singletonList(model));
        return Single.just(model);
    }

    @Override
    public Flowable<Prefs> get(String ignored) {
        Prefs config = dao.getCurrent();
        return Flowable.just(config);
    }

    @Override
    public Single<Prefs> delete(Prefs model) {
        dao.deleteCurrent();
        return Single.just(model);
    }

    @Override
    Function<List<Prefs>, List<Prefs>> provideSaveManyFunction() {
        return (List<Prefs> devices) -> {
            dao.upsert(devices);
            return devices;
        };
    }
}
