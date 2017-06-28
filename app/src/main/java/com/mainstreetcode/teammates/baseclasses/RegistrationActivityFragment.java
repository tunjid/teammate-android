package com.mainstreetcode.teammates.baseclasses;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.util.Validator;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;

/**
 * Base Fragment for registration
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public abstract class RegistrationActivityFragment extends TeammatesBaseFragment {

    protected static final Validator validator = new Validator();
    protected UserViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(UserViewModel.class);
    }
}
