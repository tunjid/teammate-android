package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DiffUtil;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.BlockedUserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.BlockedUserViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

/**
 * Lists {@link BlockedUser events}
 */

public final class BlockedUsersFragment extends MainActivityFragment
        implements
        BlockedUserAdapter.UserAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Differentiable> items;

    public static BlockedUsersFragment newInstance(Team team) {
        BlockedUsersFragment fragment = new BlockedUsersFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Team tempTeam = getArguments().getParcelable(ARG_TEAM);

        return (tempTeam != null)
                ? superResult + "-" + tempTeam.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        team = getArguments().getParcelable(ARG_TEAM);
        items = blockedUserViewModel.getModelList(team);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false);

        Runnable refreshAction = () -> disposables.add(blockedUserViewModel.refresh(team).subscribe(BlockedUsersFragment.this::onBlockedUsersUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.<InteractiveViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_block_white_24dp, R.string.no_blocked_users))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll(() -> fetchBlockedUsers(false))
                .addScrollListener((dx, dy) -> updateTopSpacerElevation())
                .withAdapter(new BlockedUserAdapter(items, this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withGridLayoutManager(2)
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchBlockedUsers(true);
    }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_events; }

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.blocked_users_title, team.getName());
    }

    @Override
    public void onBlockedUserClicked(BlockedUser blockedUser) {
        showFragment(BlockedUserViewFragment.newInstance(blockedUser));
    }

    @Nullable
    @Override
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        FragmentTransaction superResult = super.provideFragmentTransaction(fragmentTo);

        if (fragmentTo.getStableTag().contains(BlockedUserViewFragment.class.getSimpleName())) {
            Bundle args = fragmentTo.getArguments();
            if (args == null) return superResult;

            BlockedUser event = args.getParcelable(BlockedUserViewFragment.ARG_BLOCKED_USER);
            if (event == null) return superResult;

            BlockedUserViewHolder viewHolder = (BlockedUserViewHolder) scrollManager.findViewHolderForItemId(event.hashCode());
            if (viewHolder == null) return superResult;

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, getTransitionName(event, R.id.fragment_header_background))
                    .addSharedElement(viewHolder.thumbnail, getTransitionName(event, R.id.fragment_header_thumbnail));

        }
        return superResult;
    }

    private void fetchBlockedUsers(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(blockedUserViewModel.getMany(team, fetchLatest).subscribe(this::onBlockedUsersUpdated, defaultErrorHandler));
    }

    private void onBlockedUsersUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        toggleProgress(false);
    }
}
