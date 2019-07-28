/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.baseclasses;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.fragments.main.AddressPickerFragment;
import com.mainstreetcode.teammate.fragments.main.JoinRequestFragment;
import com.mainstreetcode.teammate.fragments.main.UserEditFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.BlockedUserViewModel;
import com.mainstreetcode.teammate.viewmodel.ChatViewModel;
import com.mainstreetcode.teammate.viewmodel.CompetitorViewModel;
import com.mainstreetcode.teammate.viewmodel.EventViewModel;
import com.mainstreetcode.teammate.viewmodel.FeedViewModel;
import com.mainstreetcode.teammate.viewmodel.GameViewModel;
import com.mainstreetcode.teammate.viewmodel.LocalRoleViewModel;
import com.mainstreetcode.teammate.viewmodel.LocationViewModel;
import com.mainstreetcode.teammate.viewmodel.MediaViewModel;
import com.mainstreetcode.teammate.viewmodel.PrefsViewModel;
import com.mainstreetcode.teammate.viewmodel.RoleViewModel;
import com.mainstreetcode.teammate.viewmodel.StatViewModel;
import com.mainstreetcode.teammate.viewmodel.TeamMemberViewModel;
import com.mainstreetcode.teammate.viewmodel.TeamViewModel;
import com.mainstreetcode.teammate.viewmodel.TournamentViewModel;
import com.mainstreetcode.teammate.viewmodel.UserViewModel;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Class for Fragments in {@link com.mainstreetcode.teammate.activities.MainActivity}
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    protected FeedViewModel feedViewModel;
    protected RoleViewModel roleViewModel;
    protected UserViewModel userViewModel;
    protected TeamViewModel teamViewModel;
    protected ChatViewModel chatViewModel;
    protected GameViewModel gameViewModel;
    protected StatViewModel statViewModel;
    protected PrefsViewModel prefsViewModel;
    protected EventViewModel eventViewModel;
    protected MediaViewModel mediaViewModel;
    protected LocationViewModel locationViewModel;
    protected LocalRoleViewModel localRoleViewModel;
    protected TeamMemberViewModel teamMemberViewModel;
    protected CompetitorViewModel competitorViewModel;
    protected TournamentViewModel tournamentViewModel;
    protected BlockedUserViewModel blockedUserViewModel;

    @Nullable
    private View spacer;
    protected ScrollManager<? extends InteractiveViewHolder> scrollManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localRoleViewModel = ViewModelProviders.of(this).get(LocalRoleViewModel.class);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onAttach(Context context) {
        super.onAttach(context);
        ViewModelProvider provider = ViewModelProviders.of(getActivity());
        feedViewModel = provider.get(FeedViewModel.class);
        roleViewModel = provider.get(RoleViewModel.class);
        userViewModel = provider.get(UserViewModel.class);
        teamViewModel = provider.get(TeamViewModel.class);
        chatViewModel = provider.get(ChatViewModel.class);
        gameViewModel = provider.get(GameViewModel.class);
        statViewModel = provider.get(StatViewModel.class);
        prefsViewModel = provider.get(PrefsViewModel.class);
        eventViewModel = provider.get(EventViewModel.class);
        mediaViewModel = provider.get(MediaViewModel.class);
        locationViewModel = provider.get(LocationViewModel.class);
        teamMemberViewModel = provider.get(TeamMemberViewModel.class);
        competitorViewModel = provider.get(CompetitorViewModel.class);
        tournamentViewModel = provider.get(TournamentViewModel.class);
        blockedUserViewModel = provider.get(BlockedUserViewModel.class);

        defaultErrorHandler.addAction(() -> {if (scrollManager != null) scrollManager.reset();});
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spacer = view.findViewById(R.id.spacer_toolbar);
        if (spacer == null || ((View) view.getParent()).getId() != R.id.bottom_sheet_view) return;

        spacer.setBackgroundResource(R.drawable.bg_round_top_toolbar);
        spacer.setClipToOutline(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!restoredFromBackStack() && scrollManager != null) setFabExtended(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (scrollManager != null) scrollManager.clear();
        spacer = null;
    }

    @Override
    protected void handleErrorMessage(Message message) {
        if (message.isUnauthorizedUser()) signOut();
        else super.handleErrorMessage(message);

        boolean isIllegalTeamMember = message.isIllegalTeamMember();
        boolean shouldGoBack = isIllegalTeamMember || message.isInvalidObject();

        if (isIllegalTeamMember) teamViewModel.updateDefaultTeam(Team.empty());

        Activity activity = getActivity();
        if (activity == null) return;

        if (shouldGoBack) activity.onBackPressed();
    }

    protected RecyclerView.RecycledViewPool inputRecycledViewPool() {
        return ((MainActivity) requireActivity()).getInputRecycledPool();
    }

    protected boolean isBottomSheetShowing() {
        PersistentUiController controller = getPersistentUiController();
        if (controller instanceof BottomSheetController)
            return ((BottomSheetController) controller).isBottomSheetShowing();
        return false;
    }

    protected void hideBottomSheet() {
        PersistentUiController controller = getPersistentUiController();
        if (controller instanceof BottomSheetController)
            ((BottomSheetController) controller).hideBottomSheet();
    }

    protected void showBottomSheet(BottomSheetController.Args args) {
        PersistentUiController controller = getPersistentUiController();
        if (controller instanceof BottomSheetController)
            ((BottomSheetController) controller).showBottomSheet(args);
    }

    protected void onInconsistencyDetected(IndexOutOfBoundsException exception) {
        Logger.log(getStableTag(), "Inconsistent Recyclerview", exception);
        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }

    protected void updateFabForScrollState(int dy) {
        if (Math.abs(dy) < 9) return;
        setFabExtended(dy < 0);
    }

    protected void updateTopSpacerElevation() {
        if (spacer == null || scrollManager == null) return;
        spacer.setSelected(scrollManager.getRecyclerView().canScrollVertically(-1));
    }

    protected void signOut() {
        teamViewModel.updateDefaultTeam(Team.empty());
        disposables.add(userViewModel.signOut().subscribe(
                success -> MainActivity.startRegistrationActivity(getActivity()),
                throwable -> MainActivity.startRegistrationActivity(getActivity())
        ));
    }

    protected void showCompetitor(Competitor competitor) {
        Competitive entity = competitor.getEntity();
        BaseFragment fragment = entity instanceof Team
                ? JoinRequestFragment.joinInstance((Team) entity, userViewModel.getCurrentUser())
                : entity instanceof User
                ? UserEditFragment.newInstance((User) entity)
                : null;
        if (fragment != null) showFragment(fragment);
    }

    protected void pickPlace() {
        AddressPickerFragment picker = AddressPickerFragment.newInstance();
        picker.setTargetFragment(this, R.id.request_place_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(picker)
                .setTitle("")
                .build());
    }

    protected void watchForRoleChanges(Team team, Runnable onChanged) {
        if (team.isEmpty()) return;
        User user = userViewModel.getCurrentUser();
        disposables.add(localRoleViewModel.watchRoleChanges(user, team).subscribe(object -> onChanged.run(), emptyErrorHandler));
    }
}
