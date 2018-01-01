package com.mainstreetcode.teammates.baseclasses;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammates.activities.MainActivity;
import com.mainstreetcode.teammates.model.Message;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.viewmodel.ChatViewModel;
import com.mainstreetcode.teammates.viewmodel.EventViewModel;
import com.mainstreetcode.teammates.viewmodel.FeedViewModel;
import com.mainstreetcode.teammates.viewmodel.LocalRoleViewModel;
import com.mainstreetcode.teammates.viewmodel.LocationViewModel;
import com.mainstreetcode.teammates.viewmodel.MediaViewModel;
import com.mainstreetcode.teammates.viewmodel.RoleViewModel;
import com.mainstreetcode.teammates.viewmodel.TeamViewModel;
import com.mainstreetcode.teammates.viewmodel.UserViewModel;

/**
 * Class for Fragments in {@link com.mainstreetcode.teammates.activities.MainActivity}
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    protected FeedViewModel feedViewModel;
    protected RoleViewModel roleViewModel;
    protected UserViewModel userViewModel;
    protected TeamViewModel teamViewModel;
    protected EventViewModel eventViewModel;
    protected MediaViewModel mediaViewModel;
    protected ChatViewModel chatViewModel;
    protected LocationViewModel locationViewModel;
    protected LocalRoleViewModel localRoleViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localRoleViewModel = ViewModelProviders.of(this).get(LocalRoleViewModel.class);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ViewModelProvider provider = ViewModelProviders.of(getActivity());
        feedViewModel = provider.get(FeedViewModel.class);
        roleViewModel = provider.get(RoleViewModel.class);
        userViewModel = provider.get(UserViewModel.class);
        teamViewModel = provider.get(TeamViewModel.class);
        eventViewModel = provider.get(EventViewModel.class);
        mediaViewModel = provider.get(MediaViewModel.class);
        chatViewModel = provider.get(ChatViewModel.class);
        locationViewModel = provider.get(LocationViewModel.class);
    }

    @Override
    protected void handleErrorMessage(Message message) {
        if (message.isUnauthorizedUser()) signOut();
        else if (message.isIllegalTeamMember()) {
            teamViewModel.updateDefaultTeam(Team.empty());
            getActivity().onBackPressed();
        }
        else super.handleErrorMessage(message);
    }

    protected void signOut() {
        userViewModel.signOut().subscribe(
                success -> MainActivity.startRegistrationActivity(getActivity()),
                throwable -> MainActivity.startRegistrationActivity(getActivity())
        );
    }
}
