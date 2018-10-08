package com.mainstreetcode.teammate.adapters.viewholders;


import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;

import static com.mainstreetcode.teammate.adapters.viewholders.SnackBarUtils.findSuitableParent;

public class LoadingBar extends BaseTransientBottomBar<LoadingBar> {

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent   The parent for this transient bottom bar.
     * @param content  The content view for this transient bottom bar.
     * @param callback The content view callback for this transient bottom bar.
     */
    private LoadingBar(ViewGroup parent, View content, android.support.design.snackbar.ContentViewCallback callback) {
        super(parent, content, callback);

        // Remove the default insets applied that account for the keyboard showing up
        // since it's handled by us
        View wrapper = (View) content.getParent();
        ViewCompat.setOnApplyWindowInsetsListener(wrapper, (view, insets) -> insets);
    }

    public static LoadingBar make(@NonNull View view, @Duration int duration) {
        final ViewGroup parent = findSuitableParent(view);
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        final View content = inflater.inflate(R.layout.snackbar_loading, parent, false);
        final android.support.design.snackbar.ContentViewCallback viewCallback = new SnackBarUtils.SnackbarAnimationCallback(content);
        final LoadingBar loadingBar = new LoadingBar(parent, content, viewCallback);

        loadingBar.setDuration(duration);
        return loadingBar;
    }
}
