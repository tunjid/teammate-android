package com.mainstreetcode.teammates.baseclasses;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

/**
 * Base Fragment for this app
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeammatesBaseFragment extends BaseFragment {

    public void toggleToolbar(boolean show) {
        ((TeammatesBaseActivity) getActivity()).toggleToolbar(show);
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
}
