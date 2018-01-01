package com.mainstreetcode.teammates.baseclasses;


import android.support.annotation.DrawableRes;

public interface PersistentUiController {
    void toggleToolbar(boolean show);

    void toggleBottombar(boolean show);

    void toggleFab(boolean show);

    void setFabIcon(@DrawableRes int icon);

    void setToolbarTitle(CharSequence charSequence);
}
