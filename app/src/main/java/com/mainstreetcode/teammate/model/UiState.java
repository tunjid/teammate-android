package com.mainstreetcode.teammate.model;

import android.view.View;

import com.tunjid.androidbootstrap.functions.BiConsumer;
import com.tunjid.androidbootstrap.functions.Consumer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.Objects;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
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
    private final View.OnClickListener fabClickListener;

    public static UiState freshState() {
        return new UiState(
                0,
                0,
                0,
                0,
                false,
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
                   boolean showsAltToolbar, boolean showsBottomNav,
                   boolean showsSystemUI,
                   InsetFlags insetFlags,
                   CharSequence toolbarTitle,
                   CharSequence altToolbarTitle,
                   View.OnClickListener fabClickListener) {
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

    public void diff(UiState newState,
                     BiConsumer<Integer, Integer> fabStateConsumer,
                     Consumer<Integer> toolBarMenuConsumer,
                     Consumer<Integer> altToolBarMenuConsumer,
                     Consumer<Boolean> showsFabConsumer,
                     Consumer<Boolean> showsToolbarConsumer,
                     Consumer<Boolean> showsAltToolbarConsumer,
                     Consumer<Boolean> showsBottomNavConsumer,
                     Consumer<Boolean> showsSystemUIConsumer,
                     Consumer<InsetFlags> insetFlagsConsumer,
                     Consumer<CharSequence> toolbarTitleConsumer,
                     Consumer<CharSequence> altToolbarTitleConsumer,
                     Consumer<View.OnClickListener> fabClickListenerConsumer
    ) {
        if (!Objects.equals(fabIcon, newState.fabIcon)
                || !Objects.equals(fabText, newState.fabText))
            fabStateConsumer.accept(newState.fabIcon, newState.fabText);

        if (!Objects.equals(toolBarMenu, newState.toolBarMenu))
            toolBarMenuConsumer.accept(newState.toolBarMenu);

        if (!Objects.equals(altToolBarMenu, newState.altToolBarMenu))
            altToolBarMenuConsumer.accept(newState.altToolBarMenu);

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

        if (!Objects.equals(toolbarTitle, newState.toolbarTitle))
            toolbarTitleConsumer.accept(newState.toolbarTitle);

        if (!Objects.equals(altToolbarTitle, newState.altToolbarTitle))
            altToolbarTitleConsumer.accept(newState.altToolbarTitle);

        if (!Objects.equals(fabClickListener, newState.fabClickListener))
            fabClickListenerConsumer.accept(newState.fabClickListener);
    }
}
