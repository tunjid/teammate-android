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
import android.support.v7.widget.RecyclerView;
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
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
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

    private View bottomInsetView;
    private View keyboardPadding;
    private View topInsetView;

    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fab;
    private LoadingBar loadingBar;
    private Toolbar toolbar;

    @Nullable
    private ViewHider fabHider;
    @Nullable
    private ViewHider toolbarHider;
    @Nullable
    private FabIconAnimator fabIconAnimator;


    final FragmentManager.FragmentLifecycleCallbacks fragmentViewCreatedCallback = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            if (!isInMainFragmentContainer(v)) return;

            boolean isFullscreenFragment = isFullscreenFragment(f);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
            params.topMargin = isFullscreenFragment ? insetHeight : 0;
            topInsetView.setVisibility(isFullscreenFragment ? GONE : VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int color = isFullscreenFragment ? R.color.transparent : R.color.colorPrimary;
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

        View keyboardPaddingWrapper = findViewById(R.id.keyboard_padding_wrapper);
        keyboardPadding = findViewById(R.id.keyboard_padding);
        coordinatorLayout = findViewById(R.id.coordinator);
        bottomInsetView = findViewById(R.id.bottom_inset);
        topInsetView = findViewById(R.id.top_inset);
        toolbar = findViewById(R.id.toolbar);
        fab = findViewById(R.id.fab);

        if (toolbar != null) toolbarHider = ViewHider.of(toolbar).setDirection(TOP).build();

        if (fab != null) {
            fabHider = ViewHider.of(fab).setDirection(BOTTOM).build();
            fabIconAnimator = new FabIconAnimator(fab);
        }

        //noinspection AndroidLintClickableViewAccessibility
        keyboardPaddingWrapper.setOnTouchListener((view, event) -> {
            setKeyboardPadding(0);
            return true;
        });

        setSupportActionBar(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            setOnApplyWindowInsetsListener(findViewById(R.id.content_view), (view, insets) -> consumeSystemInsets(insets));
        }
    }

    @Override
    public void toggleToolbar(boolean show) {
        if (toolbarHider == null) return;
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }

    @Override
    public void toggleBottombar(boolean show) {}

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
        if (show) (loadingBar = LoadingBar.make(coordinatorLayout, Snackbar.LENGTH_INDEFINITE)).show();
        else if (loadingBar != null && loadingBar.isShownOrQueued()) loadingBar.dismiss();
    }

    @Override
    public void toggleStatusBar(boolean show) {
        View decorView = getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();

        if (show) uiOptions = uiOptions & ~View.SYSTEM_UI_FLAG_FULLSCREEN;
        else uiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
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
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.getView(), (view, insets) -> insets);
        snackbar.show();
    }

    @Override
    public void showSnackBar(CharSequence message, int stringRes, View.OnClickListener clickListener) {
        toggleProgress(false);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE)
                .setAction(stringRes, clickListener);

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.getView(), (view, insets) -> insets);
        snackbar.show();
    }

    @Override
    public void setFabClickListener(@Nullable View.OnClickListener clickListener) {
        fab.setOnClickListener(clickListener);
    }

    public void onDialogDismissed() {
        TeammatesBaseFragment fragment = (TeammatesBaseFragment) getCurrentFragment();
        boolean showFab = fragment != null && fragment.showsFab();

        if (showFab) toggleFab(true);
    }

    protected boolean isFullscreenFragment(Fragment fragment) {
        return fragment instanceof TeammatesBaseFragment && ((TeammatesBaseFragment) fragment).drawsBehindStatusBar();
    }

    protected boolean isInMainFragmentContainer(View view) {
        View parent = (View) view.getParent();
        return parent.getId() == R.id.main_fragment_container;
    }

    protected void initTransition() {
        Transition transition = new AutoTransition();
        transition.setDuration(200);

        TeammatesBaseFragment view = (TeammatesBaseFragment) getCurrentFragment();
        if (view != null) for (int id : view.staticViews()) transition.excludeTarget(id, true);
        transition.excludeTarget(RecyclerView.class, true);

        TransitionManager.beginDelayedTransition((ViewGroup) toolbar.getParent(), transition);
    }

    private WindowInsetsCompat consumeSystemInsets(WindowInsetsCompat insets) {
        if (insetsApplied) return insets;

        ViewGroup.MarginLayoutParams topParams = fromView(topInsetView);
        insetHeight = insets.getSystemWindowInsetTop();
        topParams.height = insetHeight;
        fromView(bottomInsetView).height = insets.getSystemWindowInsetBottom();

        insetsApplied = true;
        return insets;
    }

    private WindowInsetsCompat consumeFragmentInsets(WindowInsetsCompat insets) {
        int bottomInset = insets.getSystemWindowInsetBottom();
        setKeyboardPadding(bottomInset);

        TeammatesBaseFragment view = (TeammatesBaseFragment) getCurrentFragment();
        if (view != null) view.onKeyBoardChanged(bottomInset != 0);
        return insets;
    }

    private void setKeyboardPadding(int padding) {
        initTransition();
        Fragment fragment = getCurrentFragment();

        if (fragment instanceof MainActivityFragment && padding != 0)
            padding -= getResources().getDimensionPixelSize(R.dimen.action_bar_height);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) keyboardPadding.getLayoutParams();
        params.height = padding;
    }

    private ViewGroup.MarginLayoutParams fromView(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }
}
