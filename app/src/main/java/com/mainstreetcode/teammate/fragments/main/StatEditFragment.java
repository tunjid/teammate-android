package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatEditAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Stat;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.StatGofer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;

/**
 * Edits a Team member
 */

public class StatEditFragment extends HeaderedFragment<Stat>
        implements
        UserAdapter.AdapterListener,
        StatEditAdapter.AdapterListener {

    private static final String ARG_STAT = "stat";
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
        stat = getArguments().getParcelable(ARG_STAT);
        gofer = statViewModel.gofer(stat);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.<BaseViewHolder>with(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new StatEditAdapter(gofer.getItems(), this))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(gofer.canEdit() && !stat.getGame().isEnded() && !stat.isEmpty());
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
    @StringRes
    protected int getFabStringResource() { return stat.isEmpty() ? R.string.stat_create : R.string.stat_update; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    protected int getToolbarMenu() { return R.menu.fragment_stat_edit; }

    @Override protected CharSequence getToolbarTitle() {
        return getString(stat.isEmpty() ? R.string.stat_add : R.string.stat_edit);
    }

    @Override
    public InsetFlags insetFlags() {return VERTICAL;}

    @Override
    public boolean showsFab() { return !isBottomSheetShowing() && gofer.canEdit(); }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    protected Stat getHeaderedModel() {return stat;}

    @Override
    protected Gofer<Stat> gofer() {return gofer;}

    @Override
    protected boolean canExpandAppBar() { return false; }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        toggleProgress(false);
        scrollManager.onDiff(result);
        viewHolder.bind(getHeaderedModel());
        Activity activity;
        if ((activity = getActivity()) != null) activity.invalidateOptionsMenu();
    }

    @Override
    public void onUserClicked(User item) {
        disposables.add(gofer.chooseUser(item).subscribe(this::onModelUpdated, defaultErrorHandler));
        hideBottomSheet();
    }

    @Override
    public void onUserClicked() {
        if (stat.getGame().isEnded()) showSnackbar(getString(R.string.stat_game_ended));
        else if (!stat.isEmpty()) showSnackbar(getString(R.string.stat_already_added));
        else pickStatUser();
    }

    @Override
    public void onTeamClicked() {
        if (stat.getGame().isEnded()) showSnackbar(getString(R.string.stat_game_ended));
        else if (!stat.isEmpty()) showSnackbar(getString(R.string.stat_already_added));
        else switchStatTeam();
    }

    @Override
    public boolean canChangeStat() { return stat.isEmpty(); }

    @Override
    public Stat getStat() { return stat; }

    @Override
    protected void onPrepComplete() {
        scrollManager.notifyDataSetChanged();
        requireActivity().invalidateOptionsMenu();
        super.onPrepComplete();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.fab) return;

        toggleProgress(true);
        disposables.add(gofer.save().subscribe(diffResult -> requireActivity().onBackPressed(), defaultErrorHandler));
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

    private void pickStatUser() {
        TeamMembersFragment fragment = TeamMembersFragment.newInstance(stat.getTeam());
        fragment.setTargetFragment(this, R.id.request_stat_edit_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setTitle(getString(R.string.pick_user))
                .setFragment(fragment)
                .build());
    }

    private void switchStatTeam() {
        disposables.add(gofer.switchTeams().subscribe(this::onModelUpdated, defaultErrorHandler));
    }
}
