package com.mainstreetcode.teammates.baseclasses;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.viewmodel.UserViewModel;

/**
 * Class for Fragments in {@link com.mainstreetcode.teammates.activities.MainActivity}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    protected UserViewModel userViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
    }
}
