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

import com.mainstreetcode.teammate.model.Device;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.persistence.AppDatabase;
import com.mainstreetcode.teammate.persistence.DeviceDao;
import com.mainstreetcode.teammate.persistence.EntityDao;
import com.mainstreetcode.teammate.rest.TeammateApi;
import com.mainstreetcode.teammate.rest.TeammateService;
import com.mainstreetcode.teammate.util.TeammateException;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static android.text.TextUtils.isEmpty;
import static com.mainstreetcode.teammate.model.Message.fromThrowable;

public class DeviceRepo extends ModelRepo<Device> {

    private final TeammateApi api;
    private final DeviceDao dao;

    DeviceRepo() {
        api = TeammateService.getApiInstance();
        dao = AppDatabase.getInstance().deviceDao();
    }

    @Override
    public EntityDao<? super Device> dao() {
        return dao;
    }

    @Override
    public Single<Device> createOrUpdate(Device model) {
        Device current = dao.getCurrent();
        Consumer<Device> saveFunction = device -> dao.upsert(Collections.singletonList(device));

        if (isEmpty(model.getFcmToken())) return Single.error(new TeammateException("No token"));
        else if (current.isEmpty()) return api.createDevice(model).doOnSuccess(saveFunction);
        else return api.updateDevice(current.setFcmToken(model.getFcmToken()).getId(), model)
                    .doOnSuccess(saveFunction)
                    .doOnError(throwable -> deleteInvalidModel(current, throwable))
                    .onErrorResumeNext(throwable -> {
                        Message message = fromThrowable(throwable);
                        return message != null && message.isInvalidObject()
                                ? api.createDevice(current).doOnSuccess(saveFunction)
                                : Single.error(throwable);
                    });
    }

    @Override
    public Flowable<Device> get(String id) {
        Device device = dao.getCurrent();
        return device.isEmpty()
                ? Flowable.error(new TeammateException("No stored device"))
                : Flowable.just(device);
    }

    @Override
    public Single<Device> delete(Device model) {
        dao.deleteCurrent();
        return Single.just(model);
    }

    @Override
    Function<List<Device>, List<Device>> provideSaveManyFunction() {
        return (List<Device> devices) -> {
            dao.upsert(devices);
            return devices;
        };
    }
}
