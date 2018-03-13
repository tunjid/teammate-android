package com.mainstreetcode.teammates.baseclasses;


import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;

public interface PersistentUiController {
    void toggleToolbar(boolean show);

    void toggleBottombar(boolean show);

    void toggleFab(boolean show);

    void toggleProgress(boolean show);

    void toggleSystemUI(boolean show);

    void setFabIcon(@DrawableRes int icon);

    void showSnackBar(CharSequence message);

    void setToolbarTitle(CharSequence title);

    void showSnackBar(CharSequence message, @StringRes int stringRes, View.OnClickListener clickListener);

    void setFabClickListener(@Nullable View.OnClickListener clickListener);
}
