package com.mainstreetcode.teammate.util;

import android.support.design.widget.AppBarLayout;
import android.view.View;
import android.view.ViewTreeObserver;

public class AppBarListener implements
        View.OnAttachStateChangeListener,
        AppBarLayout.OnOffsetChangedListener,
        ViewTreeObserver.OnGlobalLayoutListener{

    private int lastOffset;
    private int appBarHeight;
    private final AppBarLayout appBarLayout;
    private final ModelUtils.Consumer<OffsetProps> offsetDiffListener;

    private AppBarListener(AppBarLayout appBarLayout, ModelUtils.Consumer<OffsetProps> offsetDiffListener) {
        this.appBarLayout = appBarLayout;
        this.offsetDiffListener = offsetDiffListener;
        appBarLayout.addOnAttachStateChangeListener(this);
        appBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(this);
        appBarLayout.addOnOffsetChangedListener(this);
    }

    public static Builder builder() { return new Builder(); }

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

    @Override
    public void onGlobalLayout() {
        this.appBarHeight = appBarLayout.getHeight();
        appBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
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

        public int getDy() { return dy; }
    }

    public static class Builder {
        private AppBarLayout appBarLayout;
        private ModelUtils.Consumer<OffsetProps> offsetDiffListener;

        public Builder setAppBarLayout(AppBarLayout appBarLayout) {
            this.appBarLayout = appBarLayout;
            return this;
        }

        public Builder setOffsetDiffListener(ModelUtils.Consumer<OffsetProps> offsetDiffListener) {
            this.offsetDiffListener = offsetDiffListener;
            return this;
        }

        public AppBarListener create() {
            return new AppBarListener(appBarLayout, offsetDiffListener);
        }
    }
}
