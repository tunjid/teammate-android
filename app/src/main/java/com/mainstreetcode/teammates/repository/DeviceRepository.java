package com.mainstreetcode.teammates.repository;

import com.mainstreetcode.teammates.model.Device;
import com.mainstreetcode.teammates.persistence.DeviceDao;
import com.mainstreetcode.teammates.rest.TeammateApi;
import com.mainstreetcode.teammates.rest.TeammateService;
import com.mainstreetcode.teammates.util.TeammateException;

import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class DeviceRepository extends ModelRepository<Device> {

    private final TeammateApi api;
    private final DeviceDao dao;

    private static DeviceRepository ourInstance;

    private DeviceRepository() {
        api = TeammateService.getApiInstance();
        dao = new DeviceDao();
    }

    public static DeviceRepository getInstance() {
        if (ourInstance == null) ourInstance = new DeviceRepository();
        return ourInstance;
    }


    @Override
    public Single<Device> createOrUpdate(Device model) {
        Device current = dao.getCurrentDevice();

        if (current == null) return api.createDevice(model).map(device -> {
            dao.upsert(Collections.singletonList(device));
            return device;
        });
        else {
            current.setFcmToken(model.getFcmToken());
            return api.updateDevice(current.getId(), model).map(device -> {
                dao.upsert(Collections.singletonList(device));
                return device;
            }).doOnError(throwable -> dao.deleteCurrentDevice());
        }
    }

    @Override
    public Flowable<Device> get(String id) {
        Device device = dao.getCurrentDevice();
        return device == null
                ? Flowable.error(new TeammateException("No stored device"))
                : Flowable.just(device);
    }

    @Override
    public Single<Device> delete(Device model) {
        dao.deleteCurrentDevice();
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
