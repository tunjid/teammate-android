package com.mainstreetcode.teammates.baseclasses;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.viewmodel.UserViewModel;
import com.mainstreetcode.teammates.viewmodel.RoleViewModel;

/**
 * Class for Fragments in {@link com.mainstreetcode.teammates.activities.MainActivity}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    private boolean hasPrefetchedRoles;

    protected UserViewModel userViewModel;
    protected RoleViewModel roleViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        userViewModel = ViewModelProviders.of(getActivity()).get(UserViewModel.class);
        roleViewModel = ViewModelProviders.of(getActivity()).get(RoleViewModel.class);

        // Prefetch all roles
//        if (!hasPrefetchedRoles) {
//            roleViewModel.getRoles();
//            hasPrefetchedRoles = true;
//        }
    }
}
