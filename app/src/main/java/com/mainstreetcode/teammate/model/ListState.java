package com.mainstreetcode.teammate.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class ListState {

    @StringRes
    public final int textRes;
    @DrawableRes
    public final int imageRes;

    private ListState(int textRes, int imageRes) {
        this.textRes = textRes;
        this.imageRes = imageRes;
    }

    public static ListState of(@DrawableRes int iconRes, @StringRes int stringRes) {
        return new ListState(iconRes, stringRes);
    }
}
