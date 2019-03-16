package com.mainstreetcode.teammate.model;

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

public final class UiState {

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
                true,
                false,
                false,
                false,
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

    public UiState diff(UiState newState,
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

        either(newState, state -> state.fabIcon, state -> state.fabText, fabStateConsumer);
        either(newState, state -> state.toolBarMenu, state -> state.toolbarTitle, toolbarStateConsumer);
        either(newState, state -> state.altToolBarMenu, state -> state.altToolbarTitle, altToolbarStateConsumer);

        if (!Objects.equals(showsFab, newState.showsFab))
            showsFabConsumer.accept(newState.showsFab);

        if (!Objects.equals(showsToolbar, newState.showsToolbar))
            showsToolbarConsumer.accept(newState.showsToolbar);

        if (!Objects.equals(showsAltToolbar, newState.showsAltToolbar))
            showsAltToolbarConsumer.accept(newState.showsAltToolbar);

        if (!Objects.equals(showsBottomNav, newState.showsBottomNav))
            showsBottomNavConsumer.accept(newState.showsBottomNav);

        if (!Objects.equals(showsSystemUI, newState.showsSystemUI))
            showsSystemUIConsumer.accept(newState.showsSystemUI);

        if (!Objects.equals(insetFlags, newState.insetFlags))
            insetFlagsConsumer.accept(newState.insetFlags);

        fabClickListenerConsumer.accept(newState.fabClickListener);

        return newState;
    }

    private <S, T> void either(UiState that,
                               Function<UiState, S> first,
                               Function<UiState, T> second,
                               BiConsumer<S, T> biConsumer) {
        S thisFirst = first.apply(this);
        S thatFirst = first.apply(that);
        T thisSecond = second.apply(this);
        T thatSecond = second.apply(that);

        if (!Objects.equals(thisFirst, thatFirst) || !Objects.equals(thisSecond, thatSecond))
            biConsumer.accept(thatFirst, thatSecond);
    }
}
