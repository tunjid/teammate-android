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

import com.mainstreetcode.teammate.model.Prefs;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.persistence.PrefsDao;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class PrefsRepo extends ModelRepo<Prefs> {

    private final PrefsDao dao;

    PrefsRepo() { dao = AppDatabase.getInstance().prefsDao(); }

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
