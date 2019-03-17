package com.mainstreetcode.teammate.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.functions.Function;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class UiState implements Parcelable {

    @DrawableRes private final int fabIcon;
    @StringRes private final int fabText;
    @MenuRes private final int toolBarMenu;
    @MenuRes private final int altToolBarMenu;

    public final boolean showsFab;
    private final boolean showsToolbar;
    private final boolean showsAltToolbar;
    private final boolean showsBottomNav;
    private final boolean showsSystemUI;

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
                false,
                true,
                false,
                true,
                true,
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
                   boolean showsFab,
                   boolean showsToolbar,
                   boolean showsAltToolbar,
                   boolean showsBottomNav,
                   boolean showsSystemUI,
                   InsetFlags insetFlags,
                   CharSequence toolbarTitle,
                   CharSequence altToolbarTitle,
                   @Nullable View.OnClickListener fabClickListener) {
        this.fabIcon = fabIcon;
        this.fabText = fabText;
        this.toolBarMenu = toolBarMenu;
        this.altToolBarMenu = altToolBarMenu;
        this.showsFab = showsFab;
        this.showsToolbar = showsToolbar;
        this.showsAltToolbar = showsAltToolbar;
        this.showsBottomNav = showsBottomNav;
        this.showsSystemUI = showsSystemUI;
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
        showsFab = in.readByte() != 0x00;
        showsToolbar = in.readByte() != 0x00;
        showsAltToolbar = in.readByte() != 0x00;
        showsBottomNav = in.readByte() != 0x00;
        showsSystemUI = in.readByte() != 0x00;

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
        dest.writeByte((byte) (showsFab ? 0x01 : 0x00));
        dest.writeByte((byte) (showsToolbar ? 0x01 : 0x00));
        dest.writeByte((byte) (showsAltToolbar ? 0x01 : 0x00));
        dest.writeByte((byte) (showsBottomNav ? 0x01 : 0x00));
        dest.writeByte((byte) (showsSystemUI ? 0x01 : 0x00));
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
