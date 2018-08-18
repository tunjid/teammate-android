package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.TournamentGofer;

/**
 * Edits a Team member
 */

public class TournamentEditFragment extends HeaderedFragment<Tournament>
        implements
        TournamentEditAdapter.AdapterListener {

    public static final String ARG_EVENT = "tournament";
    private static final int[] EXCLUDED_VIEWS = {R.id.model_list};

    private Tournament tournament;
    private TournamentGofer gofer;

    public static TournamentEditFragment newInstance(Tournament tournament) {
        TournamentEditFragment fragment = new TournamentEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_EVENT, tournament);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_EVENT));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        tournament = getArguments().getParcelable(ARG_EVENT);
        gofer = tournamentViewModel.gofer(tournament);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new TournamentEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_rounds).setVisible(!tournament.isEmpty());
        menu.findItem(R.id.action_delete).setVisible(gofer.canEditAfterCreation());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_tournament_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rounds:
                showFragment(GamesParentFragment.newInstance(tournament));
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
        tournamentViewModel.clearNotifications(tournament);
    }

    @Override
    public void togglePersistentUi() {
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(gofer.getToolbarTitle(this));
        super.togglePersistentUi();
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return gofer.canEditAfterCreation();}

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    protected Tournament getHeaderedModel() {return tournament;}

    @Override
    protected Gofer<Tournament> gofer() {return gofer;}

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());
        Activity activity;
        if ((activity = getActivity()) != null) activity.invalidateOptionsMenu();
    }

    @Override
    protected void onPrepComplete() {
        scrollManager.notifyDataSetChanged();
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                boolean wasEmpty = tournament.isEmpty();
                toggleProgress(true);
                disposables.add(gofer.save().subscribe(diffResult -> {
                    int stringRes = wasEmpty ? R.string.added_user : R.string.updated_user;
                    onModelUpdated(diffResult);
                    showSnackbar(getString(stringRes, tournament.getName()));
                }, defaultErrorHandler));
                break;
        }
    }

    @Override
    public boolean canEditBeforeCreation() {
        return gofer.canEditBeforeCreation();
    }

    @Override
    public boolean canEditAfterCreation() {
        return gofer.canEditAfterCreation();
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
}
