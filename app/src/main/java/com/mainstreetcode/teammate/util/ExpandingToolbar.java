package com.mainstreetcode.teammate.util;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.widget.TextViewCompat;
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
        searchTitle.setOnClickListener(searchClickListener);
        searchButton.setOnClickListener(searchClickListener);
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
