package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.mainstreetcode.teammates.Application;
import com.mainstreetcode.teammates.model.Device;

import java.util.List;

import io.reactivex.Single;

/**
 * DAO for {@link com.mainstreetcode.teammates.model.Device}
 */

@Dao
public class DeviceDao extends EntityDao<Device> {

    private static final String KEY = "current_device_key";
    private static final String PREFERENCE_KEY = "user_devices";

    @Override
    protected String getTableName() {
        return PREFERENCE_KEY;
    }

    @Nullable
    public Device getCurrentDevice() {
        SharedPreferences preferences = getPreferences();
        String id = preferences.getString(KEY, "");

        if (TextUtils.isEmpty(id)) return null;
        return new Device(id);
    }

    @Override
    public void delete(Device model) {
        deleteCurrentDevice();
    }

    public void deleteCurrentDevice() {
        getPreferences().edit().remove(KEY).apply();
    }

    @Override
    protected void insert(List<Device> models) {
        if (models.isEmpty() || models.size() > 1) return;

        Device device = models.get(0);
        getPreferences().edit().putString(KEY, device.getId()).apply();
    }

    @Override
    protected void update(List<Device> models) {
        insert(models);
    }

    @Override
    Single<Integer> deleteAll() {
        return Single.fromCallable(() -> {
            deleteCurrentDevice();
            return 0;
        });
    }

    private SharedPreferences getPreferences() {
        return Application.getInstance().getSharedPreferences(getTableName(), Context.MODE_PRIVATE);
    }
}
