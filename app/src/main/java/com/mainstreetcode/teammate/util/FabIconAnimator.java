package com.mainstreetcode.teammate.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.button.MaterialButton;
import android.transition.TransitionManager;
import android.view.View;

import com.mainstreetcode.teammate.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FabIconAnimator {

    private static final String SCALE_X_PROPERTY = "scaleX";
    private static final String SCALE_Y_PROPERTY = "scaleY";
    private static final String ROTATION_Y_PROPERTY = "rotationY";
    private static final float FULL_SCALE = 1F;
    private static final float FIFTH_SCALE = 0.2F;
    private static final float TWITCH_END = 60F;
    private static final float TWITCH_START = 0F;
    private static final int DURATION = 200;

    @DrawableRes private int currentIcon;
    @StringRes private int currentText;

    private final MaterialButton button;
    private final ConstraintLayout container;
    private final ScaleListener scaleListener = new ScaleListener();

    public FabIconAnimator(ConstraintLayout container) {
        this.container = container;
        this.button = container.findViewById(R.id.fab);
    }

    public void update(@DrawableRes int icon, @StringRes int text) {
        boolean isSame = currentIcon == icon && currentText == text;

        currentIcon = icon;
        currentText = text;
        animateChange(icon, text);
//        if (isSame) twitch();
//        else animateChange(icon, text);
//        else if (fab.getVisibility() == View.GONE) fab.setImageResource(icon, text);
//        else scale();
    }

    public void setExtended(boolean extended) {
        //if (extended && isExtended()) return;

        ConstraintSet from = new ConstraintSet();
        ConstraintSet to = new ConstraintSet();
        from.clone(container);
        to.clone(container.getContext(), extended ? R.layout.fab_extended : R.layout.fab_collapsed);

        TransitionManager.beginDelayedTransition(container);

        if (extended) button.setText(currentText);
        else button.setText("");

        to.applyTo(container);
    }

    public void setOnClickListener(@Nullable View.OnClickListener clickListener) {
        button.setOnClickListener(clickListener);
    }

    public int getVisibility() {
        return button.getVisibility() == VISIBLE || button.getVisibility() == VISIBLE ? VISIBLE : GONE;
    }

    private boolean isExtended() {
        return button.getLayoutParams().height != button.getResources().getDimensionPixelSize(R.dimen.triple_and_half_margin);
    }

    private void animateChange(@DrawableRes int icon, @StringRes int text) {
        button.setText(text);
        button.setIconResource(icon);
        boolean extended = isExtended();
        setExtended(extended);
        if (!extended) twitch();
    }

    private void twitch() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator twitchA = animateProperty(ROTATION_Y_PROPERTY, TWITCH_START, TWITCH_END);
        ObjectAnimator twitchB = animateProperty(ROTATION_Y_PROPERTY, TWITCH_END, TWITCH_START);

        set.play(twitchB).after(twitchA);
        set.start();
    }

    @NonNull
    private ObjectAnimator animateProperty(String property, float start, float end) {
        return ObjectAnimator.ofFloat(container, property, start, end).setDuration(DURATION);
    }

    private void scale() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleDownX = animateProperty(SCALE_X_PROPERTY, FULL_SCALE, FIFTH_SCALE);
        ObjectAnimator scaleDownY = animateProperty(SCALE_Y_PROPERTY, FULL_SCALE, FIFTH_SCALE);

        ObjectAnimator scaleUpX = animateProperty(SCALE_X_PROPERTY, FIFTH_SCALE, FULL_SCALE);
        ObjectAnimator scaleUpY = animateProperty(SCALE_Y_PROPERTY, FIFTH_SCALE, FULL_SCALE);

        scaleDownX.addListener(scaleListener);

        set.play(scaleUpY).after(scaleDownY);
        set.play(scaleUpX).after(scaleDownX);
        set.playTogether(scaleDownX, scaleDownY);

        set.start();
    }

    private final class ScaleListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationEnd(Animator animator) {
            //  fab.setImageResource(currentIcon, currentText);
        }

        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }
}
