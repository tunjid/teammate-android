package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.view.View;

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.components.KeyboardUtils;

/**
 * Base Fragment for this app
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeammatesBaseFragment extends BaseFragment {

    // Needed because of the transparent status bar
    private KeyboardUtils keyboardUtils = new KeyboardUtils(this);

    public void toggleProgress(boolean show){
        ((TeammatesBaseActivity) getActivity()).toggleProgress(show);
    }
    public void toggleToolbar(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleToolbar(show);
    }

    public void toggleFab(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleFab(show);
    }

    public void setToolbarTitle(CharSequence charSequence) {
        ((TeammatesBaseActivity) getActivity()).setToolbarTitle(charSequence);
    }

    public FloatingActionButton getFab() {
        return ((TeammatesBaseActivity) getActivity()).getFab();
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
