package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
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

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.components.KeyboardUtils;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Base Fragment for this app
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeammatesBaseFragment extends BaseFragment {

    // Needed because of the transparent status bar
    private KeyboardUtils keyboardUtils = new KeyboardUtils(this);
    protected CompositeDisposable disposables = new CompositeDisposable();

    protected void toggleProgress(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleProgress(show);
    }

    protected void toggleToolbar(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleToolbar(show);
    }

    protected void toggleFab(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleFab(show);
    }

    protected void setToolbarTitle(CharSequence charSequence) {
        ((TeammatesBaseActivity) getActivity()).setToolbarTitle(charSequence);
    }

    protected FloatingActionButton getFab() {
        return ((TeammatesBaseActivity) getActivity()).getFab();
    }

    protected void showErrorSnackbar(String message) {
        toggleProgress(false);
        View root = getView();
        if (root != null) Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        keyboardUtils.initialize();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getFab().setOnClickListener(null);
        keyboardUtils.stop();
        disposables.clear();
    }

    @Nullable
    @Override
    @SuppressLint("CommitTransaction")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        return getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                        android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void setDefaultSharedTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = getTransition();

            setEnterTransition(baseTransition);
            setExitTransition(baseTransition);
            setSharedElementEnterTransition(baseSharedTransition);
            setSharedElementReturnTransition(baseSharedTransition);
        }
    }

    public static android.transition.Transition getTransition() {
        TransitionSet result = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            result = new TransitionSet();

            result.setOrdering(TransitionSet.ORDERING_TOGETHER)
                    .addTransition(new ChangeBounds())
                    .addTransition(new ChangeTransform())
                    .addTransition(new ChangeImageTransform());
        }
        return result;
    }

}
