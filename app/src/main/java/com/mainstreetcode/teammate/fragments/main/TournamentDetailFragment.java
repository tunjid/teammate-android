package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentRoundAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ModelUtils;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;

import static android.support.design.widget.TabLayout.MODE_FIXED;
import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;

public class TournamentDetailFragment extends MainActivityFragment {

    public static final String ARG_TOURNAMENT = "role";

    private Tournament tournament;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private EmptyViewHolder viewHolder;

    public static TournamentDetailFragment newInstance(Tournament tournament) {
        TournamentDetailFragment fragment = new TournamentDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TOURNAMENT, tournament);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_TOURNAMENT));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_games_parent, container, false);
        viewPager = root.findViewById(R.id.view_pager);
        tabLayout = root.findViewById(R.id.tab_layout);
        viewHolder = new EmptyViewHolder(root, R.drawable.ic_score_white_24dp, R.string.tournament_games_desc);

        viewPager.setAdapter(new TournamentRoundAdapter(tournament, getChildFragmentManager()));
        viewPager.setCurrentItem(tournament.getCurrentRound());

        setUpWinner((ViewGroup) root, tournament.getNumRounds());

        return root;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean hasPrivilegedRole = localRoleViewModel.hasPrivilegedRole();
        menu.findItem(R.id.action_edit).setVisible(hasPrivilegedRole);
        menu.findItem(R.id.action_delete).setVisible(hasPrivilegedRole);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_tournament_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                showFragment(TournamentEditFragment.newInstance(tournament));
                break;
            case R.id.action_standings:
                showFragment(StatDetailFragment.newInstance(tournament));
                break;
            case R.id.action_delete:
                Context context = getContext();
                if (context == null) return true;
                new AlertDialog.Builder(context).setTitle(getString(R.string.delete_tournament_prompt))
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteTournament())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        int rounds = tournament.getNumRounds();
        User user = userViewModel.getCurrentUser();
        Team team = tournament.getHost();
        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(this::togglePersistentUi, emptyErrorHandler));
        disposables.add(tournamentViewModel.checkForWinner(tournament).subscribe(changed -> setUpWinner((ViewGroup) getView(), rounds), defaultErrorHandler));
    }

    @Override
    public void togglePersistentUi() {
        updateFabIcon();
        setToolbarTitle(getString(R.string.tournament_fixtures));
        requireActivity().invalidateOptionsMenu();
        super.togglePersistentUi();
    }

    @Override
    public void onDestroyView() {
        viewPager = null;
        tabLayout = null;
        viewHolder = null;
        super.onDestroyView();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.add_tournament_competitors; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_group_add_white_24dp; }

    @Override
    public boolean showsFab() {
        return localRoleViewModel.hasPrivilegedRole() && !tournament.hasCompetitors();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) showFragment(CompetitorsFragment.newInstance(tournament));
    }

    private void deleteTournament() {
        disposables.add(tournamentViewModel.delete(tournament).subscribe(this::onTournamentDeleted, defaultErrorHandler));
    }

    private void onTournamentDeleted(Tournament deleted) {
        showSnackbar(getString(R.string.deleted_team, deleted.getName()));
        removeEnterExitTransitions();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        activity.onBackPressed();
    }

    @SuppressWarnings("unchecked")
    private void setUpWinner(@Nullable ViewGroup root, int prevAdapterCount) {
        if (root == null) return;

        TextView winnerText = root.findViewById(R.id.winner);
        ViewGroup winnerView = root.findViewById(R.id.item_container);
        PagerAdapter adapter = root.<ViewPager>findViewById(R.id.view_pager).getAdapter();

        if (prevAdapterCount != tournament.getNumRounds() && adapter != null)
            adapter.notifyDataSetChanged();

        TransitionManager.beginDelayedTransition(root, new AutoTransition()
                .addTarget(tabLayout)
                .addTarget(viewPager)
                .addTarget(winnerView)
                .addTarget(winnerText));

        boolean hasCompetitors = tournament.getNumCompetitors() > 0;
        tabLayout.setTabMode(tournament.getNumRounds() > 4 ? MODE_SCROLLABLE : MODE_FIXED);
        tabLayout.setVisibility(hasCompetitors ? View.VISIBLE : View.GONE);
        tabLayout.setupWithViewPager(viewPager);
        viewHolder.setColor(R.color.dark_grey);
        viewHolder.toggle(!hasCompetitors);

        Competitor winner = tournament.getWinner();
        if (winner.isEmpty()) return;

        Competitive competitive = winner.getEntity();

        ModelCardViewHolder viewHolder = competitive instanceof User
                ? new UserViewHolder(winnerView, user -> {})
                : competitive instanceof Team
                ? new TeamViewHolder(winnerView, team -> {})
                : null;

        if (viewHolder == null) return;
        viewHolder.bind(competitive);

        winnerText.setVisibility(View.VISIBLE);
        winnerView.setVisibility(View.VISIBLE);

        winnerText.setText(ModelUtils.processString(getString(R.string.tournament_winner)));
    }
}
