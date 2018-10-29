package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.annotation.NonNull;

import com.mainstreetcode.teammate.model.Device;

/**
 * DAO for {@link com.mainstreetcode.teammate.model.Device}
 */

@Dao
public class DeviceDao extends SharedPreferencesDao<Device> {

    @NonNull
    @Override
    Device getEmpty() { return Device.empty(); }

    @Override
    String preferenceName() {return "user_devices";}

    @Override
    String to(Device device) {return device.getId();}

    @Override
    Device from(String id) {return new Device(id);}
}
