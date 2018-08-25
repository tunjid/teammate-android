package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.GameRoundAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.UserViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;

import static android.support.design.widget.TabLayout.MODE_FIXED;
import static android.support.design.widget.TabLayout.MODE_SCROLLABLE;

public class GamesParentFragment extends MainActivityFragment {

    public static final String ARG_TOURNAMENT = "role";

    private Tournament tournament;

    public static GamesParentFragment newInstance(Tournament tournament) {
        GamesParentFragment fragment = new GamesParentFragment();
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
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_games_parent, container, false);
        ViewPager viewPager = root.findViewById(R.id.view_pager);
        TabLayout tabLayout = root.findViewById(R.id.tab_layout);
        EmptyViewHolder viewHolder = new EmptyViewHolder(root, R.drawable.ic_score_white_24dp, R.string.tournament_games_desc);

        boolean hasCompetitors = tournament.getNumCompetitors() > 0;
        viewPager.setAdapter(new GameRoundAdapter(tournament, getChildFragmentManager()));
        viewPager.setCurrentItem(tournament.getCurrentRound());
        tabLayout.setTabMode(tournament.getNumRounds() > 4 ? MODE_SCROLLABLE : MODE_FIXED);
        tabLayout.setVisibility(hasCompetitors ? View.VISIBLE : View.GONE);
        tabLayout.setupWithViewPager(viewPager);
        viewHolder.setColor(R.color.dark_grey);
        viewHolder.toggle(!hasCompetitors);

        setUpWinner(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        User user = userViewModel.getCurrentUser();
        Team team = tournament.getHost();
        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(() -> toggleFab(showsFab()), emptyErrorHandler));
        disposables.add(tournamentViewModel.onWinnerChanged(tournament).subscribe(changed -> setUpWinner(getView()), defaultErrorHandler));
    }

    @Override
    public void togglePersistentUi() {
        setToolbarTitle(getString(R.string.tournament_fixtures));
        setFabIcon(R.drawable.ic_group_add_white_24dp);
        super.togglePersistentUi();
    }

    @Override
    public boolean showsFab() {
        return localRoleViewModel.hasPrivilegedRole() && tournament.getNumCompetitors() == 0;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab) showFragment(CompetitorsFragment.newInstance(tournament));
    }

    @SuppressWarnings("unchecked")
    private void setUpWinner(@Nullable View root) {
        if (root == null) return;

        Competitor winner = tournament.getWinner();
        if (winner.isEmpty()) return;

        Competitive competitive = winner.getEntity();

        View winnerText = root.findViewById(R.id.winner);
        ViewGroup itemView = root.findViewById(R.id.item_container);

        ModelCardViewHolder viewHolder = competitive instanceof User
                ? new UserViewHolder(itemView, user -> {})
                : competitive instanceof Team
                ? new TeamViewHolder(itemView, team -> {})
                : null;

        if (viewHolder == null) return;
        viewHolder.bind((Model) competitive);

        winnerText.setVisibility(View.VISIBLE);
        itemView.setVisibility(View.VISIBLE);
    }
}
