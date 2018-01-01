package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.View;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.LoadingSnackbar;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.mainstreetcode.teammates.util.Validator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Base Fragment for this app
 */

public class TeammatesBaseFragment extends BaseFragment {

    protected static final Validator validator = new Validator();

    private LoadingSnackbar loadingSnackbar;
    protected CompositeDisposable disposables = new CompositeDisposable();

    protected Consumer<Throwable> defaultErrorHandler;
    protected Consumer<Throwable> emptyErrorHandler = ErrorHandler.EMPTY;

    protected void setToolbarTitle(CharSequence charSequence) {
        getPersistentUiController().setToolbarTitle(charSequence);
    }

    @SuppressWarnings("ConstantConditions")
    protected FloatingActionButton getFab() {
        return ((TeammatesBaseActivity) getActivity()).getFab();
    }

    protected void showSnackbar(String message) {
        TeammatesBaseActivity activity = (TeammatesBaseActivity) getActivity();
        if (activity == null || getView() == null) return;

        toggleProgress(false);
        View coordinator = activity.getRootCoordinator();
        Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(this::handleErrorMessage)
                .build();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleToolbar(showsToolBar());
        toggleFab(showsFab());
        toggleBottombar(showsBottomNav());
    }

    @Override
    public void onPause() {
        disposables.clear();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        getFab().setOnClickListener(null);

        if (loadingSnackbar != null && loadingSnackbar.isShownOrQueued()) loadingSnackbar.dismiss();

        loadingSnackbar = null;
        disposables.clear();
        super.onDestroyView();
    }

    protected void toggleProgress(boolean show) {
        TeammatesBaseActivity activity = (TeammatesBaseActivity) getActivity();
        if (activity == null || getView() == null) return;

        View coordinator = activity.getRootCoordinator();

        if (show && loadingSnackbar != null && loadingSnackbar.isShown()) return;

        if (show && coordinator != null) {
            loadingSnackbar = LoadingSnackbar.make(coordinator, Snackbar.LENGTH_INDEFINITE);
            loadingSnackbar.show();
        }
        else if (loadingSnackbar != null && loadingSnackbar.isShownOrQueued()) {
            loadingSnackbar.dismiss();
        }
    }

    protected void handleErrorMessage(Message message) {
        showSnackbar(message.getMessage());
        toggleProgress(false);

        Activity activity = getActivity();
        if (activity != null && (message.isInvalidObject())) activity.onBackPressed();
    }

    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    protected final FragmentTransaction beginTransaction() {
        return getFragmentManager().beginTransaction();
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void setEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = new TransitionSet()
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);

            setEnterTransition(baseTransition);
            setExitTransition(baseTransition);
            setSharedElementEnterTransition(baseSharedTransition);
            setSharedElementReturnTransition(baseSharedTransition);
        }
    }

    protected void removeEnterExitTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterTransition(new Fade());
            setExitTransition(new Fade());
            setSharedElementEnterTransition(null);
            setSharedElementReturnTransition(null);
        }
    }

    public int[] staticViews() {
        return new int[]{};
    }

    public boolean drawsBehindStatusBar() {
        return false;
    }

    protected boolean showsFab() {
        return false;
    }

    protected boolean showsToolBar() {
        return true;
    }

    protected boolean showsBottomNav() {
        return true;
    }

    private PersistentUiController getPersistentUiController() {
        Activity activity = getActivity();
        return activity == null ? DUMMY : ((PersistentUiController) activity);
    }

    protected void setFabIcon(@DrawableRes int icon) {
        getPersistentUiController().setFabIcon(icon);
    }

    protected void toggleFab(boolean show) {
        getPersistentUiController().toggleFab(show);
    }

    protected void toggleToolbar(boolean show) {
        getPersistentUiController().toggleToolbar(show);
    }

    private void toggleBottombar(boolean show) {
        getPersistentUiController().toggleBottombar(show);
    }

    private static final PersistentUiController DUMMY = new PersistentUiController() {
        @Override
        public void toggleToolbar(boolean show) {

        }

        @Override
        public void toggleBottombar(boolean show) {

        }

        @Override
        public void toggleFab(boolean show) {

        }

        @Override
        public void setFabIcon(int icon) {

        }

        @Override
        public void setToolbarTitle(CharSequence charSequence) {

        }
    };
}
