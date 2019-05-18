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
