package com.mainstreetcode.teammate.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.ChoiceBar;
import com.mainstreetcode.teammate.adapters.viewholders.LoadingBar;
import com.mainstreetcode.teammate.util.FabInteractor;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseActivity;
import com.tunjid.androidbootstrap.view.animator.ViewHider;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.view.KeyEvent.ACTION_UP;
import static android.view.View.GONE;
import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener;
import static com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE;
import static com.google.android.material.snackbar.Snackbar.LENGTH_LONG;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.tunjid.androidbootstrap.view.animator.ViewHider.BOTTOM;
import static com.tunjid.androidbootstrap.view.animator.ViewHider.TOP;

/**
 * Base Activity for the app
 */

public abstract class TeammatesBaseActivity extends BaseActivity
        implements PersistentUiController {

    protected static final int HIDER_DURATION = 400;

    public static int topInset;
    private int leftInset;
    private int rightInset;
    public int bottomInset;

    private boolean insetsApplied;

    private View bottomInsetView;
    private View topInsetView;

    private CoordinatorLayout coordinatorLayout;
    private ConstraintLayout constraintLayout;
    private FrameLayout fragmentContainer;
    private LoadingBar loadingBar;
    private Toolbar toolbar;
    private View padding;

    private ViewHider fabHider;
    private ViewHider toolbarHider;
    private FabInteractor fabInteractor;

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

        MaterialButton fab = findViewById(R.id.fab);
        fragmentContainer = findViewById(R.id.main_fragment_container);
        coordinatorLayout = findViewById(R.id.coordinator);
        constraintLayout = findViewById(R.id.content_view);
        bottomInsetView = findViewById(R.id.bottom_inset);
        topInsetView = findViewById(R.id.top_inset);
        toolbar = findViewById(R.id.toolbar);
        padding = findViewById(R.id.padding);
        toolbarHider = ViewHider.of(toolbar).setDuration(HIDER_DURATION).setDirection(TOP).build();
        fabHider = ViewHider.of(fab).setDuration(HIDER_DURATION).setDirection(BOTTOM).build();
        fabInteractor = new FabInteractor(fab);

        //noinspection AndroidLintClickableViewAccessibility
        padding.setOnTouchListener((view, event) -> {
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
    @SuppressLint({"Range", "WrongConstant"})
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
        if (fabInteractor != null) fabInteractor.update(icon, title);
    }

    @Override
    public void setFabExtended(boolean expanded) {
        if (fabInteractor != null) fabInteractor.setExtended(expanded);
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
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, LENGTH_LONG);

        // Necessary to remove snackbar padding for keyboard on older versions of Android
        ViewCompat.setOnApplyWindowInsetsListener(snackbar.getView(), (view, insets) -> insets);
        snackbar.show();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void showSnackBar(ModelUtils.Consumer<Snackbar> consumer) {
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
        fabInteractor.setOnClickListener(clickListener);
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

    protected int adjustKeyboardPadding(int suggestion) {
        return suggestion - bottomInset;
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
        padding = adjustKeyboardPadding(padding);
        padding = Math.max(padding, 0);

        fragmentContainer.setPadding(0, 0, 0, padding);
        getLayoutParams(this.padding).height = padding == 0 ? 1 : padding; // 0 breaks animations
    }

    private void adjustSystemInsets(Fragment fragment) {
        if (!(fragment instanceof TeammatesBaseFragment)) return;
        InsetFlags insetFlags = ((TeammatesBaseFragment) fragment).insetFlags();

        getLayoutParams(toolbar).topMargin = insetFlags.hasTopInset() ? 0 : topInset;
        topInsetView.setVisibility(insetFlags.hasTopInset() ? VISIBLE : GONE);
        constraintLayout.setPadding(insetFlags.hasLeftInset() ? leftInset : 0, 0, insetFlags.hasRightInset() ? rightInset : 0, 0);
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
