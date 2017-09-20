package com.mainstreetcode.teammates.util;

import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

public class FabIconAnimator extends FloatingActionButton.OnVisibilityChangedListener {

    @DrawableRes private int currentIcon;
    private FloatingActionButton fab;

    public FabIconAnimator(FloatingActionButton fab) {
        this.fab = fab;
    }

    public void setCurrentIcon(@DrawableRes int resource) {
        if (currentIcon == resource) return;

        currentIcon = resource;

        if (fab.getVisibility() == View.GONE) fab.setImageResource(resource);
        else fab.hide(this);
    }

    @Override
    public void onHidden(FloatingActionButton fab) {
        super.onHidden(fab);
        fab.setImageResource(currentIcon);
        fab.show();
    }

    @Override
    public void onShown(FloatingActionButton fab) {
        super.onShown(fab);
    }
}
