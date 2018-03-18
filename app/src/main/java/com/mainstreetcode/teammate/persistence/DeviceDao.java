package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;

import com.mainstreetcode.teammate.model.Device;

/**
 * DAO for {@link com.mainstreetcode.teammate.model.Device}
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
