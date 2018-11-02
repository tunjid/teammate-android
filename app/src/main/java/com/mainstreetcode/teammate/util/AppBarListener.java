package com.mainstreetcode.teammate.util;

import android.view.View;

import com.google.android.material.appbar.AppBarLayout;

public class AppBarListener implements
        View.OnAttachStateChangeListener,
        AppBarLayout.OnOffsetChangedListener {

    private int lastOffset;
    private int appBarHeight;
    private final AppBarLayout appBarLayout;
    private final ModelUtils.Consumer<OffsetProps> offsetDiffListener;

    private AppBarListener(AppBarLayout appBarLayout, ModelUtils.Consumer<OffsetProps> offsetDiffListener) {
        this.appBarLayout = appBarLayout;
        this.offsetDiffListener = offsetDiffListener;
        appBarLayout.addOnOffsetChangedListener(this);
        appBarLayout.addOnAttachStateChangeListener(this);
        ViewHolderUtil.listenForLayout(appBarLayout, () -> this.appBarHeight = appBarLayout.getHeight());
    }

    public static Builder with() { return new Builder(); }

    @Override
    public void onViewAttachedToWindow(View v) { }

    @Override
    public void onViewDetachedFromWindow(View v) {
        appBarLayout.removeOnOffsetChangedListener(this);
        appBarLayout.removeOnAttachStateChangeListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int newOffset) {
        offsetDiffListener.accept(new OffsetProps(lastOffset - newOffset, newOffset, appBarHeight));
        lastOffset = newOffset;
    }

    public static class OffsetProps {
        private final int dy;
        private final int offset;
        private final int appBarHeight;

        OffsetProps(int dy, int offset, int appBarHeight) {
            this.dy = dy;
            this.offset = offset;
            this.appBarHeight = appBarHeight;
        }

        public boolean appBarUnmeasured() { return appBarHeight == 0; }

        public int getDy() { return dy; }

        public int getOffset() { return -offset; }

        public float getFraction() { return ((float) -offset) / appBarHeight; }
    }

    public static class Builder {
        private AppBarLayout appBarLayout;
        private ModelUtils.Consumer<OffsetProps> offsetDiffListener;

        public Builder appBarLayout(AppBarLayout appBarLayout) {
            this.appBarLayout = appBarLayout;
            return this;
        }

        public Builder offsetDiffListener(ModelUtils.Consumer<OffsetProps> offsetDiffListener) {
            this.offsetDiffListener = offsetDiffListener;
            return this;
        }

        public AppBarListener create() {
            return new AppBarListener(appBarLayout, offsetDiffListener);
        }
    }
}
