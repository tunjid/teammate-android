package com.mainstreetcode.teammate.baseclasses;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;

import com.mainstreetcode.teammate.viewmodel.UserViewModel;

/**
 * Base Fragment for registration
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public abstract class RegistrationActivityFragment extends TeammatesBaseFragment {

    private static final int GRASS_COLOR = 0xFF93BB51;

    protected UserViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(UserViewModel.class);
    }

    @Override
    protected boolean hasLightNavBar() {
        return false;
    }

    @Override
    protected int getNavBarColor() {
        return GRASS_COLOR;
    }
}
