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

import android.view.View;

import com.google.android.material.appbar.AppBarLayout;
import com.tunjid.androidbootstrap.functions.Consumer;

public class AppBarListener implements
        View.OnAttachStateChangeListener,
        AppBarLayout.OnOffsetChangedListener {

    private int lastOffset;
    private int appBarHeight;
    private final AppBarLayout appBarLayout;
    private final Consumer<OffsetProps> offsetDiffListener;

    private AppBarListener(AppBarLayout appBarLayout, Consumer<OffsetProps> offsetDiffListener) {
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
        private Consumer<OffsetProps> offsetDiffListener;

        public Builder appBarLayout(AppBarLayout appBarLayout) {
            this.appBarLayout = appBarLayout;
            return this;
        }

        public Builder offsetDiffListener(Consumer<OffsetProps> offsetDiffListener) {
            this.offsetDiffListener = offsetDiffListener;
            return this;
        }

        public AppBarListener create() {
            return new AppBarListener(appBarLayout, offsetDiffListener);
        }
    }
}
