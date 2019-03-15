package com.mainstreetcode.teammate.baseclasses;


import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar;
import com.mainstreetcode.teammate.model.UiState;
import com.tunjid.androidbootstrap.functions.Consumer;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public interface PersistentUiController {

    void update(UiState state);

    void toggleToolbar(boolean show);

    void toggleAltToolbar(boolean show);

    void toggleBottombar(boolean show);

    void toggleFab(boolean show);

    void toggleProgress(boolean show);

    void toggleSystemUI(boolean show);

    void setFabIcon(@DrawableRes int icon, @StringRes int textRes);

    void setFabExtended(boolean expanded);

    void showSnackBar(CharSequence message);

    void setToolbarTitle(CharSequence title);

    void setAltToolbarTitle(CharSequence title);

    void setAltToolbarMenu(@MenuRes int menu);

    void showSnackBar(Consumer<Snackbar> consumer);

    void showChoices(Consumer<ChoiceBar> consumer);

    void setFabClickListener(@Nullable View.OnClickListener clickListener);
}
