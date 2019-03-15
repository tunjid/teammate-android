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

    PersistentUiController DUMMY = new PersistentUiController() {

        @Override
        public void update(UiState state) {}

        @Override
        public void toggleToolbar(boolean show) {}

        @Override
        public void toggleAltToolbar(boolean show) {}

        @Override
        public void toggleBottombar(boolean show) {}

        @Override
        public void toggleFab(boolean show) {}

        @Override
        public void toggleProgress(boolean show) {}

        @Override
        public void toggleSystemUI(boolean show) {}

        @Override
        public void setFabIcon(int icon, int textRes) {}

        @Override
        public void setFabExtended(boolean expanded) {}

        @Override
        public void showSnackBar(CharSequence message) {}

        @Override
        public void showSnackBar(Consumer<Snackbar> consumer) {}

        @Override
        public void showChoices(Consumer<ChoiceBar> consumer) {}

        @Override
        public void setToolbarTitle(CharSequence title) {}

        @Override
        public void setAltToolbarTitle(CharSequence title) {}

        @Override
        public void setAltToolbarMenu(int menu) {}

        @Override
        public void setFabClickListener(View.OnClickListener clickListener) {}
    };
}
