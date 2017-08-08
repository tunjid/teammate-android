package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

/**
 * Base Fragment for this app
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeammatesBaseFragment extends BaseFragment {

    protected CompositeDisposable disposables = new CompositeDisposable();

    protected Consumer<Throwable> defaultErrorHandler;
    protected Consumer<Throwable> emptyErrorHandler;

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

    protected void showSnackbar(String message) {
        toggleProgress(false);
        View root = getView();
        if (root != null) Snackbar.make(root, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        defaultErrorHandler = ErrorHandler.builder()
                .defaultMessage(getString(R.string.default_error))
                .add(this::showSnackbar)
                .add(unused -> toggleProgress(false))
                .build();

        emptyErrorHandler = throwable -> {};
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getFab().setOnClickListener(null);
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
