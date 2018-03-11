package com.mainstreetcode.teammates.persistence;

import android.arch.persistence.room.Dao;

import com.mainstreetcode.teammates.model.Config;
import com.mainstreetcode.teammates.rest.TeammateService;

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
