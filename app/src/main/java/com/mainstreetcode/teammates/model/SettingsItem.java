package com.mainstreetcode.teammates.model;

import android.support.annotation.StringRes;

/**
 * Settings item
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class SettingsItem {

    @StringRes
    private final int stringResorce;

    public SettingsItem(@StringRes int stringResorce) {
        this.stringResorce = stringResorce;
    }

    @StringRes
    public int getStringResorce() {
        return stringResorce;
    }
}
