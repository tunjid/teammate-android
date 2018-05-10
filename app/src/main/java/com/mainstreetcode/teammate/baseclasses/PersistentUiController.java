package com.mainstreetcode.teammate.baseclasses;


import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

public interface PersistentUiController {
    void toggleToolbar(boolean show);

    void toggleAltToolbar(boolean show);

    void toggleBottombar(boolean show);

    void toggleFab(boolean show);

    void toggleProgress(boolean show);

    void toggleSystemUI(boolean show);

    void setFabIcon(@DrawableRes int icon);

    void showSnackBar(CharSequence message);

    void setToolbarTitle(CharSequence title);

    void setAltToolbarTitle(CharSequence title);

    void setAltToolbarMenu(@MenuRes int menu);

    void showSnackBar(CharSequence message, @StringRes int stringRes, View.OnClickListener clickListener);

    void setFabClickListener(@Nullable View.OnClickListener clickListener);
}
