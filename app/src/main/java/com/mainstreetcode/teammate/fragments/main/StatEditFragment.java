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
import com.mainstreetcode.teammate.adapters.StatEditAdapter;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer;

/**
 * Edits a Team member
 */

public class StatEditFragment extends HeaderedFragment<Stat>
        implements
        StatEditAdapter.AdapterListener,
        ViewHolderUtil.SimpleAdapterListener<User> {

    public static final String ARG_STAT = "stat";
    private static final int[] EXCLUDED_VIEWS = {R.id.model_list};

    private Stat stat;
    private StatGofer gofer;

    public static StatEditFragment newInstance(Stat stat) {
        StatEditFragment fragment = new StatEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_STAT, stat);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_STAT));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        stat = getArguments().getParcelable(ARG_STAT);
        gofer = statViewModel.gofer(stat);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new StatEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(gofer.canEdit());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_stat_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                Context context = getContext();
                if (context == null) return true;
                new AlertDialog.Builder(context).setTitle(getString(R.string.delete_stat_prompt))
                        .setPositiveButton(R.string.yes, (dialog, which) -> deleteStat())
                        .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        statViewModel.clearNotifications(stat);
    }

    @Override
    public void togglePersistentUi() {
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.game_stats));
        super.togglePersistentUi();
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {return gofer.canEdit();}

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    protected Stat getHeaderedModel() {return stat;}

    @Override
    protected Gofer<Stat> gofer() {return gofer;}

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());
        Activity activity;
        if ((activity = getActivity()) != null) activity.invalidateOptionsMenu();
    }

    @Override
    public void onItemClicked(User item) {
        disposables.add(gofer.chooseUser(item).subscribe(this::onModelUpdated, defaultErrorHandler));
        hideBottomSheet();
    }

    @Override
    public void onUserClicked() {
        TeamMembersFragment fragment = TeamMembersFragment.newInstance(stat.getTeam());
        fragment.setTargetFragment(this, R.id.request_stat_edit_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setTitle(getString(R.string.pick_team))
                .setFragment(fragment)
                .build());    }

    @Override
    public void onTeamClicked() {
        disposables.add(gofer.switchTeams().subscribe(this::onModelUpdated, defaultErrorHandler));
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
                boolean wasEmpty = stat.isEmpty();
                toggleProgress(true);
                disposables.add(gofer.save().subscribe(diffResult -> {
                    int stringRes = wasEmpty ? R.string.added_user : R.string.updated_user;
                    onModelUpdated(diffResult);
                    showSnackbar(getString(stringRes, stat.getStatType()));
                }, defaultErrorHandler));
                break;
        }
    }

    private void deleteStat() {
        disposables.add(gofer.remove().subscribe(this::onStatDeleted, defaultErrorHandler));
    }

    private void onStatDeleted() {
        showSnackbar(getString(R.string.deleted_team, stat.getStatType()));
        removeEnterExitTransitions();

        Activity activity;
        if ((activity = getActivity()) == null) return;

        activity.onBackPressed();
    }
}
