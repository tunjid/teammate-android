package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TournamentViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

/**
 * Lists {@link Event tournaments}
 */

public final class TournamentsFragment extends MainActivityFragment
        implements
        TournamentAdapter.TournamentAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Identifiable> items;

    public static TournamentsFragment newInstance(Team team) {
        TournamentsFragment fragment = new TournamentsFragment();
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
        setHasOptionsMenu(true);
        team = getArguments().getParcelable(ARG_TEAM);
        items = tournamentViewModel.getModelList(team);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);

        Runnable refreshAction = () -> disposables.add(tournamentViewModel.refresh(team).subscribe(TournamentsFragment.this::onTournamentsUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_trophy_white_24dp, R.string.no_tournaments))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScrollCallback(() -> fetchTournaments(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TournamentAdapter(items, this))
                .withLinearLayoutManager()
                .build();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchTournaments(true);
        User user = userViewModel.getCurrentUser();
        disposables.add(localRoleViewModel.getRoleInTeam(user, team).subscribe(() -> toggleFab(localRoleViewModel.hasPrivilegedRole()), emptyErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       // inflater.inflate(R.menu.fragment_tournaments, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                TeamPickerFragment.change(getActivity(), R.id.request_tournament_team_pick);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void togglePersistentUi() {
        updateFabIcon();
        setFabClickListener(this);
        setToolbarTitle(getString(R.string.tournaments));
        super.togglePersistentUi();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.tournament_add; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_add_white_24dp; }

    @Override
    public boolean showsFab() {
        return localRoleViewModel.hasPrivilegedRole();
    }

    @Override
    public void onTournamentClicked(Tournament tournament) {
        showFragment(TournamentDetailFragment.newInstance(tournament));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                Tournament tournament = Tournament.empty(team);
                showFragment(TournamentEditFragment.newInstance(tournament));
                break;
        }
    }

    @Nullable
    @Override
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        FragmentTransaction superResult = super.provideFragmentTransaction(fragmentTo);

        if (fragmentTo.getStableTag().contains(TournamentEditFragment.class.getSimpleName())) {
            Bundle args = fragmentTo.getArguments();
            if (args == null) return superResult;

            Tournament tournament = args.getParcelable(TournamentEditFragment.ARG_TOURNAMENT);
            if (tournament == null) return superResult;

            TournamentViewHolder viewHolder = (TournamentViewHolder) scrollManager.findViewHolderForItemId(tournament.hashCode());
            if (viewHolder == null) return superResult;

            return beginTransaction()
                    .addSharedElement(viewHolder.itemView, getTransitionName(tournament, R.id.fragment_header_background))
                    .addSharedElement(viewHolder.getImage(), getTransitionName(tournament, R.id.fragment_header_thumbnail));

        }
        return superResult;
    }

    void fetchTournaments(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(tournamentViewModel.getMany(team, fetchLatest).subscribe(this::onTournamentsUpdated, defaultErrorHandler));
    }

    private void onTournamentsUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        toggleProgress(false);
    }
}
