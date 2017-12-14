package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
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
        ((TeammatesBaseActivity) getActivity()).setToolbarTitle(charSequence);
    }

    protected FloatingActionButton getFab() {
        return ((TeammatesBaseActivity) getActivity()).getFab();
    }

    protected void showSnackbar(String message) {
        if (getView() == null) return;
        toggleProgress(false);
        View coordinator = getActivity().findViewById(R.id.coordinator);
        if (coordinator != null) Snackbar.make(coordinator, message, Snackbar.LENGTH_LONG).show();
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (view == null) return; // In case of headless Fragments

        toggleToolbar(showsToolBar());
        toggleFab(showsFab());
        toggleBottombar(showsBottomNav());
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
        if (getView() == null) return;

        View coordinator = getActivity().findViewById(R.id.coordinator);

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
        if (message.isInvalidObject()) getActivity().onBackPressed();
    }

    @SuppressLint("CommitTransaction")
    protected final FragmentTransaction beginTransaction() {
        return getActivity().getSupportFragmentManager().beginTransaction();
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Nullable
    protected Pair<Transition, Transition> setDefaultSharedTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = getTransition();

            setEnterTransition(baseTransition);
            setExitTransition(baseTransition);
            setSharedElementEnterTransition(baseSharedTransition);
            setSharedElementReturnTransition(baseSharedTransition);

            return new Pair<>(baseTransition, baseSharedTransition);
        }
        return null;
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

    protected void setFabIcon(@DrawableRes int icon) {
        ((TeammatesBaseActivity) getActivity()).setFabIcon(icon);
    }

    protected void toggleFab(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleFab(show);
    }

    private void toggleToolbar(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleToolbar(show);
    }

    private void toggleBottombar(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleBottombar(show);
    }

    public static android.transition.Transition getTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new TransitionSet()
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform())
                    .setOrdering(TransitionSet.ORDERING_TOGETHER);
        }
        return null;
    }

}
