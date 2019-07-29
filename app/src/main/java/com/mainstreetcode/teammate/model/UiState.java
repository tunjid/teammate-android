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

package com.mainstreetcode.teammate.model;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class UiState implements Parcelable {

    @DrawableRes private final int fabIcon;
    @StringRes private final int fabText;
    @MenuRes private final int toolBarMenu;
    @MenuRes private final int altToolBarMenu;
    @ColorInt private final int navBarColor;

    private final boolean showsFab;
    private final boolean showsToolbar;
    private final boolean showsAltToolbar;
    private final boolean showsBottomNav;
    private final boolean showsSystemUI;
    private final boolean hasLightNavBar;

    private final InsetFlags insetFlags;
    private final CharSequence toolbarTitle;
    private final CharSequence altToolbarTitle;
    @Nullable private final View.OnClickListener fabClickListener;

    public static UiState freshState() {
        return new UiState(
                0,
                0,
                0,
                0,
                Color.BLACK,
                true,
                true,
                false,
                true,
                true,
                false,
                InsetFlags.ALL,
                "",
                "",
                null
        );
    }

    public UiState(int fabIcon,
                   int fabText,
                   int toolBarMenu,
                   int altToolBarMenu,
                   int navBarColor,
                   boolean showsFab,
                   boolean showsToolbar,
                   boolean showsAltToolbar,
                   boolean showsBottomNav,
                   boolean showsSystemUI,
                   boolean hasLightNavBar,
                   InsetFlags insetFlags,
                   CharSequence toolbarTitle,
                   CharSequence altToolbarTitle,
                   @Nullable View.OnClickListener fabClickListener) {
        this.fabIcon = fabIcon;
        this.fabText = fabText;
        this.toolBarMenu = toolBarMenu;
        this.altToolBarMenu = altToolBarMenu;
        this.navBarColor = navBarColor;
        this.showsFab = showsFab;
        this.showsToolbar = showsToolbar;
        this.showsAltToolbar = showsAltToolbar;
        this.showsBottomNav = showsBottomNav;
        this.showsSystemUI = showsSystemUI;
        this.hasLightNavBar = hasLightNavBar;
        this.insetFlags = insetFlags;
        this.toolbarTitle = toolbarTitle;
        this.altToolbarTitle = altToolbarTitle;
        this.fabClickListener = fabClickListener;
    }

    public UiState diff(boolean force, UiState newState,
                        Consumer<Boolean> showsFabConsumer,
                        Consumer<Boolean> showsToolbarConsumer,
                        Consumer<Boolean> showsAltToolbarConsumer,
                        Consumer<Boolean> showsBottomNavConsumer,
                        Consumer<Boolean> showsSystemUIConsumer,
                        Consumer<Boolean> hasLightNavBarConsumer,
                        Consumer<Integer> navBarColorConsumer,
                        Consumer<InsetFlags> insetFlagsConsumer,
                        BiConsumer<Integer, Integer> fabStateConsumer,
                        BiConsumer<Integer, CharSequence> toolbarStateConsumer,
                        BiConsumer<Integer, CharSequence> altToolbarStateConsumer,
                        Consumer<View.OnClickListener> fabClickListenerConsumer
    ) {
        only(force, newState, state -> state.showsFab, showsFabConsumer);
        only(force, newState, state -> state.showsToolbar, showsToolbarConsumer);
        only(force, newState, state -> state.showsAltToolbar, showsAltToolbarConsumer);
        only(force, newState, state -> state.showsBottomNav, showsBottomNavConsumer);
        only(force, newState, state -> state.showsSystemUI, showsSystemUIConsumer);
        only(force, newState, state -> state.hasLightNavBar, hasLightNavBarConsumer);
        only(force, newState, state -> state.navBarColor, navBarColorConsumer);
        only(force, newState, state -> state.insetFlags, insetFlagsConsumer);

        either(force, newState, state -> state.fabIcon, state -> state.fabText, fabStateConsumer);
        either(force, newState, state -> state.toolBarMenu, state -> state.toolbarTitle, toolbarStateConsumer);
        either(force, newState, state -> state.altToolBarMenu, state -> state.altToolbarTitle, altToolbarStateConsumer);

        fabClickListenerConsumer.accept(newState.fabClickListener);

        return newState;
    }

    private <T> void only(boolean force, UiState that, Function<UiState, T> first, Consumer<T> consumer) {
        T thisFirst = first.apply(this);
        T thatFirst = first.apply(that);

        if (force || !Objects.equals(thisFirst, thatFirst)) consumer.accept(thatFirst);

    }

    private <S, T> void either(boolean force,
                               UiState that,
                               Function<UiState, S> first,
                               Function<UiState, T> second,
                               BiConsumer<S, T> biConsumer) {
        S thisFirst = first.apply(this);
        S thatFirst = first.apply(that);
        T thisSecond = second.apply(this);
        T thatSecond = second.apply(that);

        if (force || !Objects.equals(thisFirst, thatFirst) || !Objects.equals(thisSecond, thatSecond))
            biConsumer.accept(thatFirst, thatSecond);
    }

    private UiState(Parcel in) {
        fabIcon = in.readInt();
        fabText = in.readInt();
        toolBarMenu = in.readInt();
        altToolBarMenu = in.readInt();
        navBarColor = in.readInt();
        showsFab = in.readByte() != 0x00;
        showsToolbar = in.readByte() != 0x00;
        showsAltToolbar = in.readByte() != 0x00;
        showsBottomNav = in.readByte() != 0x00;
        showsSystemUI = in.readByte() != 0x00;
        hasLightNavBar = in.readByte() != 0x00;

        boolean hasLeftInset = in.readByte() != 0x00;
        boolean hasTopInset = in.readByte() != 0x00;
        boolean hasRightInset = in.readByte() != 0x00;
        boolean hasBottomInset = in.readByte() != 0x00;
        insetFlags = InsetFlags.create(hasLeftInset, hasTopInset, hasRightInset, hasBottomInset);

        toolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        altToolbarTitle = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);

        fabClickListener = null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(fabIcon);
        dest.writeInt(fabText);
        dest.writeInt(toolBarMenu);
        dest.writeInt(altToolBarMenu);
        dest.writeInt(navBarColor);
        dest.writeByte((byte) (showsFab ? 0x01 : 0x00));
        dest.writeByte((byte) (showsToolbar ? 0x01 : 0x00));
        dest.writeByte((byte) (showsAltToolbar ? 0x01 : 0x00));
        dest.writeByte((byte) (showsBottomNav ? 0x01 : 0x00));
        dest.writeByte((byte) (showsSystemUI ? 0x01 : 0x00));
        dest.writeByte((byte) (hasLightNavBar ? 0x01 : 0x00));
        dest.writeByte((byte) (insetFlags.hasLeftInset() ? 0x01 : 0x00));
        dest.writeByte((byte) (insetFlags.hasTopInset() ? 0x01 : 0x00));
        dest.writeByte((byte) (insetFlags.hasRightInset() ? 0x01 : 0x00));
        dest.writeByte((byte) (insetFlags.hasBottomInset() ? 0x01 : 0x00));

        TextUtils.writeToParcel(toolbarTitle, dest, 0);
        TextUtils.writeToParcel(altToolbarTitle, dest, 0);
    }

    public static final Parcelable.Creator<UiState> CREATOR = new Parcelable.Creator<UiState>() {
        @Override
        public UiState createFromParcel(Parcel in) {
            return new UiState(in);
        }

        @Override
        public UiState[] newArray(int size) {
            return new UiState[size];
        }
    };
}
