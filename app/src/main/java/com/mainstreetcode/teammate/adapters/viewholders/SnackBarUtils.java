package com.mainstreetcode.teammate.adapters.viewholders;


import com.google.android.material.snackbar.ContentViewCallback;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

 class SnackBarUtils {

    static ViewGroup findSuitableParent(View view) {
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

    static class SnackbarAnimationCallback implements ContentViewCallback {

        private View content;

        SnackbarAnimationCallback(View content) {
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
