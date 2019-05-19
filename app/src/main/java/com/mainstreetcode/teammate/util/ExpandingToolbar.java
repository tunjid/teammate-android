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

package com.mainstreetcode.teammate.util;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.core.widget.TextViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;

public class ExpandingToolbar {

    private final View optionsList;
    private final TextView searchButton;
    private final TextView searchTitle;
    private final ViewGroup container;

    private final Runnable onCollapsed;

    public static ExpandingToolbar create(ViewGroup container, Runnable onCollapsed) {
        return new ExpandingToolbar(container, onCollapsed);
    }

    private ExpandingToolbar(ViewGroup container, Runnable onCollapsed) {
        View.OnClickListener searchClickListener = clicked -> toggleVisibility();

        this.container = container;
        this.optionsList = container.findViewById(R.id.search_options);
        this.searchTitle = container.findViewById(R.id.search_title);
        this.searchButton = container.findViewById(R.id.search);

        this.onCollapsed = onCollapsed;
        container.setOnClickListener(searchClickListener);
        searchButton.setOnClickListener(searchClickListener);
    }

    public void setTitle(@StringRes int titleRes) {
        searchTitle.setText(searchTitle.getContext().getText(titleRes));
    }

    @SuppressLint("ResourceAsColor")
    public void setTitleIcon(boolean isDown) {
        int resVal = isDown ? R.drawable.anim_vect_down_to_right_arrow : R.drawable.anim_vect_right_to_down_arrow;

        Drawable icon = AnimatedVectorDrawableCompat.create(searchTitle.getContext(), resVal);
        if (icon == null) return;

        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(searchTitle, null, null, icon, null);
    }

    public void changeVisibility(boolean invisible) {
        TransitionManager.beginDelayedTransition(container, new AutoTransition()
                .addListener(new Transition.TransitionListener() {
                    public void onTransitionEnd(@NonNull Transition transition) {
                        if (invisible) onCollapsed.run();
                    }

                    public void onTransitionStart(@NonNull Transition transition) { }

                    public void onTransitionCancel(@NonNull Transition transition) { }

                    public void onTransitionPause(@NonNull Transition transition) { }

                    public void onTransitionResume(@NonNull Transition transition) { }
                }));

        setTitleIcon(invisible);

        AnimatedVectorDrawableCompat animatedDrawable = (AnimatedVectorDrawableCompat)
                TextViewCompat.getCompoundDrawablesRelative(searchTitle)[2];

        animatedDrawable.start();

        int visibility = invisible ? View.GONE : View.VISIBLE;
        searchButton.setVisibility(visibility);
        optionsList.setVisibility(visibility);
    }

    private void toggleVisibility() {
        boolean invisible = optionsList.getVisibility() == View.VISIBLE;
        changeVisibility(invisible);
    }
}
