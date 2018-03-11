package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;

import com.mainstreetcode.teammates.model.Device;

/**
 * DAO for {@link com.mainstreetcode.teammates.model.Device}
 */

@Dao
public class DeviceDao extends SharedPreferencesDao<Device> {

    @Override
    String preferenceName() {return "user_devices";}

    @Override
    String to(Device device) {return device.getId();}

    @Override
    Device from(String id) {return new Device(id);}
}
