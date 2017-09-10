package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.core.view.ViewHider;

/**
 * Base Activity for the app
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public abstract class TeammatesBaseActivity extends BaseActivity
        implements OnApplyWindowInsetsListener {

    private boolean insetsApplied;
    private int insetHeight;
    private View insetView;

    private Toolbar toolbar;
    private FloatingActionButton fab;

    @Nullable
    private ViewHider fabHider;
    @Nullable
    private ViewHider toolbarHider;
    @Nullable
    private ViewHider bottombarHider;

    final FragmentManager.FragmentLifecycleCallbacks lifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            boolean isFullscreenFragment = isFullscreenFragment(f);

//            TransitionManager.beginDelayedTransition((ViewGroup)toolbar.getParent(), new AutoTransition());
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = isFullscreenFragment ? insetHeight : 0;
            insetView.setVisibility(isFullscreenFragment ? View.GONE : View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = isFullscreenFragment ? R.color.transparent : R.color.colorPrimaryDark;
                getWindow().setStatusBarColor(ContextCompat.getColor(TeammatesBaseActivity.this, color));
            }

            ViewCompat.setOnApplyWindowInsetsListener(v, TeammatesBaseActivity.this::consumeFragmentInsets);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(lifecycleCallbacks, false);
    }

    @Override
    @SuppressLint("WrongViewCast")
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        insetView = findViewById(R.id.inset_view);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);

        View bottomBar = findViewById(R.id.bottom_navigation);

        if (toolbar != null) toolbarHider = new ViewHider(toolbar, ViewHider.TOP);
        if (fab != null) fabHider = new ViewHider(fab, ViewHider.BOTTOM);
        if (bottomBar != null) bottombarHider = new ViewHider(bottomBar, ViewHider.BOTTOM);

        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_view), this);
        }
    }

    public void toggleToolbar(boolean show) {
        if (toolbarHider == null) return;
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }

    public void toggleBottombar(boolean show) {
        if (bottombarHider == null) return;
        if (show) bottombarHider.show();
        else bottombarHider.hide();
    }

    public void toggleFab(boolean show) {
        if (fabHider == null) return;
        if (show) fabHider.show();
        else fabHider.hide();
    }

    public void setToolbarTitle(CharSequence charSequence) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(charSequence);
    }

    public FloatingActionButton getFab() {
        return fab;
    }

    @Override
    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
        if (insetsApplied) return insets;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) insetView.getLayoutParams();
        insetHeight = insets.getSystemWindowInsetTop();
        params.height = insetHeight;

        insetsApplied = true;
        return insets;
    }

    protected boolean isFullscreenFragment(Fragment fragment) {
        return fragment instanceof TeammatesBaseFragment
                && ((TeammatesBaseFragment) fragment).drawsBehindStatusBar();
    }

    private WindowInsetsCompat consumeFragmentInsets(View view, WindowInsetsCompat insets) {
        Fragment fragment = getCurrentFragment();

        int padding = insets.getSystemWindowInsetBottom();

        if (fragment instanceof MainActivityFragment && padding != 0)
            padding -= getResources().getDimensionPixelSize(R.dimen.action_bar_height);

        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), padding);

        return insets;
    }
}
