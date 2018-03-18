package com.mainstreetcode.teammate.adapters.viewholders;


import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;

import com.mainstreetcode.teammate.R;

import java.lang.reflect.Field;

public class LoadingBar extends BaseTransientBottomBar<LoadingBar> {

    /**
     * Constructor for the transient bottom bar.
     *
     * @param parent   The parent for this transient bottom bar.
     * @param content  The content view for this transient bottom bar.
     * @param callback The content view callback for this transient bottom bar.
     */
    private LoadingBar(ViewGroup parent, View content, ContentViewCallback callback) {
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
        final ContentViewCallback viewCallback = new ContentViewCallback(content);
        final LoadingBar loadingBar = new LoadingBar(parent, content, viewCallback);
        loadingBar.setDuration(duration);
        forceAnimation(loadingBar);
        return loadingBar;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return (ViewGroup) view;
            }
            else if (view instanceof FrameLayout) {
                // If we've hit the decor content view, then we didn't find a CoL in the
                // hierarchy, so use it.
                if (view.getId() == android.R.id.content) return (ViewGroup) view;
                // It's not the content view but we'll use it as our fallback
                else fallback = (ViewGroup) view;
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback;
    }

    private static void forceAnimation(BaseTransientBottomBar bottomBar) {
        try {
            Field mAccessibilityManagerField = BaseTransientBottomBar.class.getDeclaredField("mAccessibilityManager");
            mAccessibilityManagerField.setAccessible(true);
            AccessibilityManager accessibilityManager = (AccessibilityManager) mAccessibilityManagerField.get(bottomBar);
            Field mIsEnabledField = AccessibilityManager.class.getDeclaredField("mIsEnabled");
            mIsEnabledField.setAccessible(true);
            mIsEnabledField.setBoolean(accessibilityManager, false);
            mAccessibilityManagerField.set(bottomBar, accessibilityManager);
        } catch (Exception e) {
            Log.d("Snackbar", "Reflection error: " + e.toString());
        }
    }

    private static class ContentViewCallback implements BaseTransientBottomBar.ContentViewCallback {

        private View content;

        ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            content.setAlpha(0f);
            ViewCompat.animate(content).alpha(1f).setDuration(duration)
                    .setStartDelay(delay).start();
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            content.setAlpha(1f);
            ViewCompat.animate(content).alpha(0f).setDuration(duration)
                    .setStartDelay(delay).start();
        }
    }
}
