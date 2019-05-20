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

import android.graphics.drawable.Drawable;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

public class FabInteractor extends FabExtensionAnimator {

    private MaterialButton button;

    public FabInteractor(MaterialButton button) {
        super(button);
        this.button = button;
    }

    public void update(@DrawableRes int icon, @StringRes int text) {
        updateGlyphs(new State(button, icon, text));
    }

    public void setOnClickListener(@Nullable View.OnClickListener clickListener) {
        if (clickListener == null) {
            button.setOnClickListener(null);
            return;
        }
        AtomicBoolean flag = new AtomicBoolean(true);
        button.setOnClickListener(view -> {
            if (!flag.getAndSet(false)) return;
            clickListener.onClick(view);
            button.postDelayed(() -> flag.set(true), 2000);
        });
    }

    private class State extends GlyphState {

        @DrawableRes
        private final int icon;
        @StringRes
        private final int text;

        private final CharSequence charSequence;
        private final Drawable drawable;

        State(MaterialButton button, @DrawableRes int icon, @StringRes int text) {
            this.icon = icon;
            this.text = text;
            this.charSequence = button.getResources().getText(text);
            drawable = ContextCompat.getDrawable(button.getContext(), icon);
        }

        @Override
        public Drawable getIcon() { return drawable; }

        @Override
        public CharSequence getText() { return charSequence; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return icon == state.icon &&
                    text == state.text;
        }

        @Override
        public int hashCode() {
            return Objects.hash(icon, text);
        }
    }
}
