package com.mainstreetcode.teammate.baseclasses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.activities.MainActivity;
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

/**
 * Class for Fragments in {@link com.mainstreetcode.teammate.activities.MainActivity}
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    protected ScrollManager scrollManager;
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

    @Override
    public void onResume() {
        super.onResume();
        if (!restoredFromBackStack() && scrollManager != null) setFabExtended(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (scrollManager != null) scrollManager.clear();
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

    @Override
    protected void onKeyBoardChanged(boolean appeared) {
        super.onKeyBoardChanged(appeared);
        if (!appeared && isBottomSheetShowing()) hideBottomSheet();
    }

    @SuppressLint("CheckResult")
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void signOut() {
        teamViewModel.updateDefaultTeam(Team.empty());
        userViewModel.signOut().subscribe(
                success -> MainActivity.startRegistrationActivity(getActivity()),
                throwable -> MainActivity.startRegistrationActivity(getActivity())
        );
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

    protected void watchForRoleChanges(Team team, Runnable onChanged) {
        if (team.isEmpty()) return;
        User user = userViewModel.getCurrentUser();
        disposables.add(localRoleViewModel.watchRoleChanges(user, team).subscribe(object -> onChanged.run(), emptyErrorHandler));
    }
}
