package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.annotation.NonNull;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.rest.TeammateService;

/**
 * DAO for {@link Config}
 */

@Dao
public class ConfigDao extends SharedPreferencesDao<Config> {

    @NonNull
    @Override
    Config getEmpty() { return Config.empty(); }

    @Override
    String preferenceName() {return "config";}

    @Override
    String to(Config device) {return TeammateService.getGson().toJson(device);}

    @Override
    Config from(String json) {return TeammateService.getGson().fromJson(json, Config.class);}
}
