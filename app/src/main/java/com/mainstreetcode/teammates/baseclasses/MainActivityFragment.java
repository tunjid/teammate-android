package com.mainstreetcode.teammates.baseclasses;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.viewmodel.EventViewModel;
import com.mainstreetcode.teammates.viewmodel.RoleViewModel;
import com.mainstreetcode.teammates.viewmodel.TeamChatViewModel;
import com.mainstreetcode.teammates.viewmodel.TeamViewModel;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;

/**
 * Class for Fragments in {@link com.mainstreetcode.teammates.activities.MainActivity}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    //private boolean hasPrefetchedRoles;

    protected RoleViewModel roleViewModel;
    protected UserViewModel userViewModel;
    protected TeamViewModel teamViewModel;
    protected EventViewModel eventViewModel;
    protected TeamChatViewModel teamChatViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ViewModelProvider provider = ViewModelProviders.of(getActivity());
        roleViewModel = provider.get(RoleViewModel.class);
        userViewModel = provider.get(UserViewModel.class);
        teamViewModel = provider.get(TeamViewModel.class);
        eventViewModel = provider.get(EventViewModel.class);
        teamChatViewModel = provider.get(TeamChatViewModel.class);
    }
}
