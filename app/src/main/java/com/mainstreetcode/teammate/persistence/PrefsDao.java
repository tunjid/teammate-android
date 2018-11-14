package com.mainstreetcode.teammate.persistence;

import androidx.room.Dao;
import androidx.annotation.NonNull;

import com.mainstreetcode.teammate.model.Config;
import com.mainstreetcode.teammate.model.Prefs;
import com.mainstreetcode.teammate.rest.TeammateService;

/**
 * DAO for {@link Config}
 */

@Dao
public class PrefsDao extends SharedPreferencesDao<Prefs> {

    @Override
    String preferenceName() {return "device-prefs";}

    @NonNull
    @Override
    Prefs getEmpty() { return Prefs.empty(); }

    @Override
    String to(Prefs device) {return TeammateService.getGson().toJson(device);}

    @Override
    Prefs from(String json) {return TeammateService.getGson().fromJson(json, Prefs.class);}
}
