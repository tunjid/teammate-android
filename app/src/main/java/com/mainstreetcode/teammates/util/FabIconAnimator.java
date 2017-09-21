package com.mainstreetcode.teammates.util;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

public class FabIconAnimator {

    private static final String SCALE_X_PROPERTY = "scaleX";
    private static final String SCALE_Y_PROPERTY = "scaleY";
    private static final String ROTATION_Y_PROPERTY = "rotationY";
    private static final float FULL_SCALE = 1F;
    private static final float FIFTH_SCALE = 0.2F;
    private static final float TWITCH_END = 60F;
    private static final float TWITCH_START = 0F;
    private static final int DURATION = 200;

    @DrawableRes
    private int currentIcon;
    private FloatingActionButton fab;
    private final ScaleListener scaleListener = new ScaleListener();

    public FabIconAnimator(FloatingActionButton fab) {
        this.fab = fab;
    }

    public void setCurrentIcon(@DrawableRes int resource) {
        boolean sameIcon = currentIcon == resource;

        currentIcon = resource;

        if (sameIcon) twitch();
        else if (fab.getVisibility() == View.GONE) fab.setImageResource(resource);
        else scale();
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

    private void twitch() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator twitchA = animateProperty(ROTATION_Y_PROPERTY, TWITCH_START, TWITCH_END);
        ObjectAnimator twitchB = animateProperty(ROTATION_Y_PROPERTY, TWITCH_END, TWITCH_START);

        set.play(twitchB).after(twitchA);
        set.start();
    }

    @NonNull
    private ObjectAnimator animateProperty(String property, float start, float end) {
        return ObjectAnimator.ofFloat(fab, property, start, end).setDuration(DURATION);
    }

    private final class ScaleListener implements Animator.AnimatorListener {
        @Override
        public void onAnimationEnd(Animator animator) {
            fab.setImageResource(currentIcon);
        }

        @Override
        public void onAnimationStart(Animator animator) {}

        @Override
        public void onAnimationCancel(Animator animator) {}

        @Override
        public void onAnimationRepeat(Animator animator) {}
    }
}
