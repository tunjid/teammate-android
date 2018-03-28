package com.mainstreetcode.teammate.persistence;

import android.arch.persistence.room.Dao;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.rest.TeammateService;

/**
 * DAO for {@link Config}
 */

@Dao
public class ConfigDao extends SharedPreferencesDao<Config> {

    @Override
    String preferenceName() {return "config";}

    @Override
    String to(Config device) {return TeammateService.getGson().toJson(device);}

    @Override
    Config from(String json) {return TeammateService.getGson().fromJson(json, Config.class);}
}
