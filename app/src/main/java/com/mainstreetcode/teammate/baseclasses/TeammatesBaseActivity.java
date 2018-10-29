package com.mainstreetcode.teammate.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar;
import com.mainstreetcode.teammate.adapters.viewholders.LoadingBar;
import com.mainstreetcode.teammate.util.FabIconAnimator;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.view.animator.ViewHider;

import java.util.ArrayList;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;
import static androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.VISIBLE;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.tunjid.androidbootstrap.view.animator.ViewHider.BOTTOM;
import static com.tunjid.androidbootstrap.view.animator.ViewHider.TOP;

/**
 * Base Activity for the app
 */

public abstract class TeammatesBaseActivity extends BaseActivity
        implements PersistentUiController {

    protected static final int TOP_INSET = 1;
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
    private LoadingBar loadingBar;
    private Toolbar toolbar;

    private FabIconAnimator fabIconAnimator;
    @Nullable private ViewHider fabHider;
    @Nullable private ViewHider toolbarHider;

    private final List<BaseTransientBottomBar> transientBottomBars = new ArrayList<>();

    private final BaseTransientBottomBar.BaseCallback callback = new BaseTransientBottomBar.BaseCallback() {
        @SuppressWarnings("SuspiciousMethodCalls")
        public void onDismissed(Object bar, int event) { transientBottomBars.remove(bar); }
    };

    final FragmentManager.FragmentLifecycleCallbacks fragmentViewCreatedCallback = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, Bundle savedInstanceState) {
            if (isNotInMainFragmentContainer(v)) return;

            clearTransientBars();
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
    protected void onPause() {
        clearTransientBars();
        super.onPause();
    }

    @Override
    @SuppressLint("WrongViewCast")
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        ConstraintLayout extendedFabContainer = findViewById(R.id.extend_fab_container);
        keyboardPadding = findViewById(R.id.keyboard_padding);
        coordinatorLayout = findViewById(R.id.coordinator);
        constraintLayout = findViewById(R.id.content_view);
        bottomInsetView = findViewById(R.id.bottom_inset);
        topInsetView = findViewById(R.id.top_inset);
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) toolbarHider = ViewHider.of(toolbar).setDirection(TOP).build();

        fabHider = ViewHider.of(extendedFabContainer).setDirection(BOTTOM).build();
        fabIconAnimator = new FabIconAnimator(extendedFabContainer);

        //noinspection AndroidLintClickableViewAccessibility
        keyboardPadding.setOnTouchListener((view, event) -> {
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
    public TeammatesBaseFragment getCurrentFragment() {
        return (TeammatesBaseFragment) super.getCurrentFragment();
    }

    @Override
    public void toggleToolbar(boolean show) {
        if (toolbarHider == null) return;
        if (show) toolbarHider.show();
        else toolbarHider.hide();
    }

    @Override
    public void toggleAltToolbar(boolean show) {}

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
    public void setFabIcon(@DrawableRes int icon, @StringRes int title) {
        if (fabIconAnimator != null) fabIconAnimator.update(icon, title);
    }

    @Override
    public void setFabExtended(boolean expanded) {
        if (fabIconAnimator != null) fabIconAnimator.setExtended(expanded);
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(title);
    }

    @Override
    public void setAltToolbarTitle(CharSequence title) {}

    @Override
    public void setAltToolbarMenu(int menu) {}

    @Override
    public void showSnackBar(CharSequence message) {
        toggleProgress(false);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, LENGTH_LONG);

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.getView(), (view, insets) -> insets);
        snackbar.show();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void showSnackBar(ModelUtils.Consumer<Snackbar> consumer) {
        toggleProgress(false);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "", LENGTH_INDEFINITE).addCallback(callback);

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.getView(), (view, insets) -> insets);
        consumer.accept(snackbar);
        transientBottomBars.add(snackbar);
        snackbar.show();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void showChoices(ModelUtils.Consumer<ChoiceBar> consumer) {
        ChoiceBar bar = ChoiceBar.make(coordinatorLayout, LENGTH_INDEFINITE).addCallback(callback);
        consumer.accept(bar);
        transientBottomBars.add(bar);
        bar.show();
    }

    @Override
    public void setFabClickListener(@Nullable View.OnClickListener clickListener) {
        fabIconAnimator.setOnClickListener(clickListener);
    }

    public void onDialogDismissed() {
        TeammatesBaseFragment fragment = getCurrentFragment();
        boolean showFab = fragment != null && fragment.showsFab();

        if (showFab) toggleFab(true);
    }

    protected boolean isNotInMainFragmentContainer(View view) {
        View parent = (View) view.getParent();
        return parent == null || parent.getId() != R.id.main_fragment_container;
    }

    protected void clearTransientBars() {
        for (BaseTransientBottomBar bar : transientBottomBars)
            if (bar instanceof ChoiceBar) ((ChoiceBar) bar).dismissAsTimeout();
            else bar.dismiss();
        transientBottomBars.clear();
    }

    protected void initTransition() {
        Transition transition = new AutoTransition();
        transition.setDuration(200);

        TeammatesBaseFragment view = getCurrentFragment();
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

        TeammatesBaseFragment view = getCurrentFragment();
        if (view != null) view.onKeyBoardChanged(keyboardPadding != bottomInset);
        return insets;
    }

    private void setKeyboardPadding(int padding) {
        initTransition();
        Fragment fragment = getCurrentFragment();

        padding -= bottomInset;
        if (fragment instanceof MainActivityFragment && padding != bottomInset)
            padding -= getResources().getDimensionPixelSize(R.dimen.action_bar_height);

        getLayoutParams(keyboardPadding).height = padding;
    }

    private void adjustSystemInsets(Fragment fragment) {
        if (!(fragment instanceof TeammatesBaseFragment)) return;
        boolean[] insetState = ((TeammatesBaseFragment) fragment).insetState();

        getLayoutParams(toolbar).topMargin = insetState[TOP_INSET] ? topInset : 0;
        topInsetView.setVisibility(insetState[TOP_INSET] ? GONE : VISIBLE);
        constraintLayout.setPadding(insetState[LEFT_INSET] ? leftInset : 0, 0, insetState[RIGHT_INSET] ? rightInset : 0, 0);
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
