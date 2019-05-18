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

package com.mainstreetcode.teammate.baseclasses;


import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar;
import com.mainstreetcode.teammate.model.UiState;
import com.tunjid.androidbootstrap.functions.Consumer;

import androidx.annotation.ColorInt;
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

    void toggleLightNavBar(boolean isLight);

    void setNavBarColor(@ColorInt int color);

    void setFabIcon(@DrawableRes int icon, @StringRes int textRes);

    void updateMainToolBar(@MenuRes int menu, CharSequence title);

    void updateAltToolbar(@MenuRes int menu, CharSequence title);

    void setFabExtended(boolean expanded);

    void showSnackBar(CharSequence message);

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
        public void toggleLightNavBar(boolean isLight) {}

        @Override
        public void setNavBarColor(int color) {}

        @Override
        public void setFabIcon(int icon, int textRes) {}

        @Override
        public void setFabExtended(boolean expanded) {}

        @Override
        public void updateMainToolBar(int menu, CharSequence title) {}

        @Override
        public void updateAltToolbar(int menu, CharSequence title) {}

        @Override
        public void showSnackBar(CharSequence message) {}

        @Override
        public void showSnackBar(Consumer<Snackbar> consumer) {}

        @Override
        public void showChoices(Consumer<ChoiceBar> consumer) {}

        @Override
        public void setFabClickListener(View.OnClickListener clickListener) {}
    };
}
