package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.LoadingBar;
import com.mainstreetcode.teammates.util.FabIconAnimator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.core.view.ViewHider;

import static android.support.v4.view.ViewCompat.setOnApplyWindowInsetsListener;
import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.VISIBLE;
import static com.tunjid.androidbootstrap.core.view.ViewHider.BOTTOM;
import static com.tunjid.androidbootstrap.core.view.ViewHider.TOP;

/**
 * Base Activity for the app
 */

public abstract class TeammatesBaseActivity extends BaseActivity
        implements PersistentUiController {

    public static int insetHeight;

    private boolean insetsApplied;
    private View keyboardPadding;
    private View insetView;

    private Toolbar toolbar;
    private CoordinatorLayout root;
    private FloatingActionButton fab;
    private LoadingBar loadingBar;

    @Nullable
    private ViewHider fabHider;
    @Nullable
    private ViewHider toolbarHider;
    @Nullable
    private ViewHider bottombarHider;
    @Nullable
    private FabIconAnimator fabIconAnimator;


    final FragmentManager.FragmentLifecycleCallbacks fragmentViewCreatedCallback = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            boolean isFullscreenFragment = isFullscreenFragment(f);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = isFullscreenFragment ? insetHeight : 0;
            insetView.setVisibility(isFullscreenFragment ? GONE : VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = isFullscreenFragment ? R.color.transparent : R.color.colorPrimaryDark;
                getWindow().setStatusBarColor(ContextCompat.getColor(TeammatesBaseActivity.this, color));
            }

            setOnApplyWindowInsetsListener(v, (view, insets) -> consumeFragmentInsets(insets));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentViewCreatedCallback, false);
    }

    @Override
    @SuppressLint("WrongViewCast")
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        keyboardPadding = findViewById(R.id.keyboard_padding);
        insetView = findViewById(R.id.inset_view);
        toolbar = findViewById(R.id.toolbar);
        root = findViewById(R.id.coordinator);
        fab = findViewById(R.id.fab);

        View bottomBar = findViewById(R.id.bottom_navigation);

        if (toolbar != null) toolbarHider = ViewHider.of(toolbar).setDirection(TOP).build();

        if (bottomBar != null) {
            bottombarHider = ViewHider.of(bottomBar).setDirection(BOTTOM)
                    .addEndRunnable(this::initTransition).build();
        }
        if (fab != null) {
            fabHider = ViewHider.of(fab).setDirection(BOTTOM).build();
            fabIconAnimator = new FabIconAnimator(fab);
        }

        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            setOnApplyWindowInsetsListener(findViewById(R.id.content_view), (view, insets) -> consumeToolbarInsets(insets));
        }
    }

    @Override
    public void toggleToolbar(boolean show) {
        if (toolbarHider == null) return;
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }

    @Override
    public void toggleBottombar(boolean show) {
        if (bottombarHider == null) return;
        if (show) bottombarHider.show();
        else bottombarHider.hide();
    }

    @Override
    public void toggleFab(boolean show) {
        if (fabHider == null) return;
        if (show) fabHider.show();
        else fabHider.hide();
    }

    @Override
    @SuppressLint("Range")
    public void toggleProgress(boolean show) {
        if (show && loadingBar != null && loadingBar.isShown()) return;
        if (show) (loadingBar = LoadingBar.make(root, Snackbar.LENGTH_INDEFINITE)).show();
        else if (loadingBar != null && loadingBar.isShownOrQueued()) loadingBar.dismiss();
    }

    @Override
    public void setFabIcon(@DrawableRes int icon) {
        if (fabIconAnimator == null) return;
        fabIconAnimator.setCurrentIcon(icon);
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    @Override
    public void showSnackBar(CharSequence message) {
        toggleProgress(false);
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.getView(), (view, insets) -> insets);
        snackbar.show();
    }

    @Override
    public void setFabClickListener(@Nullable View.OnClickListener clickListener) {
        fab.setOnClickListener(clickListener);
    }

    protected boolean isFullscreenFragment(Fragment fragment) {
        return fragment instanceof TeammatesBaseFragment && ((TeammatesBaseFragment) fragment).drawsBehindStatusBar();
    }

    private WindowInsetsCompat consumeToolbarInsets(WindowInsetsCompat insets) {
        if (insetsApplied) return insets;

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) insetView.getLayoutParams();
        insetHeight = insets.getSystemWindowInsetTop();
        params.height = insetHeight;

        insetsApplied = true;
        return insets;
    }

    private WindowInsetsCompat consumeFragmentInsets(WindowInsetsCompat insets) {
        initTransition();
        Fragment fragment = getCurrentFragment();

        int padding = insets.getSystemWindowInsetBottom();

        if (fragment instanceof MainActivityFragment && padding != 0)
            padding -= getResources().getDimensionPixelSize(R.dimen.action_bar_height);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) keyboardPadding.getLayoutParams();
        params.height = padding;

        return insets;
    }

    private void initTransition() {
        Transition transition = new AutoTransition();
        transition.setDuration(200);

        TeammatesBaseFragment view = (TeammatesBaseFragment) getCurrentFragment();
        if (view != null) for (int id : view.staticViews()) transition.excludeTarget(id, true);

        TransitionManager.beginDelayedTransition((ViewGroup) toolbar.getParent(), transition);
    }
}
