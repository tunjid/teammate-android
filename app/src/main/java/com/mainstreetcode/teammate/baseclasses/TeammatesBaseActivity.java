package com.mainstreetcode.teammate.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
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

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.LoadingBar;
import com.mainstreetcode.teammate.util.FabIconAnimator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.core.view.ViewHider;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
import static android.support.v4.view.ViewCompat.setOnApplyWindowInsetsListener;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
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

    private static final int TOP_INSET = 1;
    private static final int LEFT_INSET = 0;
    private static final int RIGHT_INSET = 2;

    public static int topInset;
    private int leftInset;
    private int rightInset;
    private int bottomInset;

    private boolean insetsApplied;

    private View bottomInsetView;
    private View keyboardPadding;
    private View topInsetView;

    private CoordinatorLayout coordinatorLayout;
    private ConstraintLayout constraintLayout;
    private FloatingActionButton fab;
    private LoadingBar loadingBar;
    private Toolbar toolbar;

    @Nullable private ViewHider fabHider;
    @Nullable private ViewHider toolbarHider;
    @Nullable private FabIconAnimator fabIconAnimator;

    final FragmentManager.FragmentLifecycleCallbacks fragmentViewCreatedCallback = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(FragmentManager fm, Fragment f, View v, Bundle savedInstanceState) {
            if (!isInMainFragmentContainer(v)) return;

            adjustSystemInsets(f);
            setOnApplyWindowInsetsListener(v, (view, insets) -> consumeFragmentInsets(insets));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentViewCreatedCallback, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent));
    }

    @Override
    @SuppressLint("WrongViewCast")
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        View keyboardPaddingWrapper = findViewById(R.id.keyboard_padding_wrapper);
        keyboardPadding = findViewById(R.id.keyboard_padding);
        coordinatorLayout = findViewById(R.id.coordinator);
        constraintLayout = findViewById(R.id.content_view);
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
            if (event.getAction() == ACTION_UP) setKeyboardPadding(bottomInset);
            return true;
        });

        setSupportActionBar(toolbar);
        getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> toggleToolbar((visibility & SYSTEM_UI_FLAG_FULLSCREEN) == 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSystemUI();
            setOnApplyWindowInsetsListener(constraintLayout, (view, insets) -> consumeSystemInsets(insets));
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
        if (show) (loadingBar = LoadingBar.make(coordinatorLayout, LENGTH_INDEFINITE)).show();
        else if (loadingBar != null && loadingBar.isShownOrQueued()) loadingBar.dismiss();
    }

    @Override
    public void toggleSystemUI(boolean show) {
        if (show) showSystemUI();
        else hideSystemUI();
    }

    @Override
    public void setFabIcon(@DrawableRes int icon) {
        if (fabIconAnimator != null) fabIconAnimator.setCurrentIcon(icon);
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
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, LENGTH_INDEFINITE)
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

        topInset = insets.getSystemWindowInsetTop();
        leftInset = insets.getSystemWindowInsetLeft();
        rightInset = insets.getSystemWindowInsetRight();
        bottomInset = insets.getSystemWindowInsetBottom();

        getLayoutParams(topInsetView).height = topInset;
        getLayoutParams(bottomInsetView).height = bottomInset;
        adjustSystemInsets(getCurrentFragment());

        insetsApplied = true;
        return insets;
    }

    private WindowInsetsCompat consumeFragmentInsets(WindowInsetsCompat insets) {
        int keyboardPadding = insets.getSystemWindowInsetBottom();
        setKeyboardPadding(keyboardPadding);

        TeammatesBaseFragment view = (TeammatesBaseFragment) getCurrentFragment();
        if (view != null) view.onKeyBoardChanged(keyboardPadding != bottomInset);
        return insets;
    }

    private void setKeyboardPadding(int padding) {
        initTransition();
        Fragment fragment = getCurrentFragment();

        padding -= bottomInset;
        if (fragment instanceof MainActivityFragment && padding != bottomInset)
            padding -= getResources().getDimensionPixelSize(R.dimen.action_bar_height);

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) keyboardPadding.getLayoutParams();
        params.height = padding;
    }

    private void adjustSystemInsets(Fragment fragment) {
        if (!(fragment instanceof TeammatesBaseFragment)) return;
        boolean[] insetState = ((TeammatesBaseFragment) fragment).insetState();

        getLayoutParams(toolbar).topMargin = insetState[TOP_INSET] ? topInset : 0;
        topInsetView.setVisibility(insetState[TOP_INSET] ? GONE : VISIBLE);
        constraintLayout.setPadding(insetState[LEFT_INSET] ? leftInset : 0, 0, insetState[RIGHT_INSET] ? rightInset : 0, 0);
    }

    private ViewGroup.MarginLayoutParams getLayoutParams(View view) {
        return (ViewGroup.MarginLayoutParams) view.getLayoutParams();
    }

    private void hideSystemUI() {
        int visibility = SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        visibility = visibility | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | SYSTEM_UI_FLAG_FULLSCREEN;
        if (isInLandscape()) visibility = visibility | SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        getDecorView().setSystemUiVisibility(visibility);
    }

    private void showSystemUI() {
        int visibility = SYSTEM_UI_FLAG_LAYOUT_STABLE | SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        getDecorView().setSystemUiVisibility(visibility);
    }

    private boolean isInLandscape() {
        return getResources().getConfiguration().orientation == ORIENTATION_LANDSCAPE;
    }

    private View getDecorView() {return getWindow().getDecorView();}
}
