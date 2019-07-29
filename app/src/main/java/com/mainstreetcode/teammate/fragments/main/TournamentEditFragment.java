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

package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TournamentEditAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.enums.Sport;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.TournamentGofer;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Edits a Team member
 */

public class TournamentEditFragment extends HeaderedFragment<Tournament>
        implements
        TournamentEditAdapter.AdapterListener {

    static final String ARG_TOURNAMENT = "tournament";
    private static final int[] EXCLUDED_VIEWS = {R.id.model_list};

    private boolean showingPrompt;
    private Tournament tournament;
    private TournamentGofer gofer;

    public static TournamentEditFragment newInstance(Tournament tournament) {
        TournamentEditFragment fragment = new TournamentEditFragment();
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
        Bundle arguments = getArguments();
        tournament = arguments.getParcelable(ARG_TOURNAMENT);
        gofer = tournamentViewModel.gofer(tournament);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.<BaseViewHolder>with(rootView.findViewById(R.id.model_list))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), this::refresh)
                .withAdapter(new TournamentEditAdapter(gofer.getItems(), this))
                .addScrollListener((dx, dy) -> updateFabForScrollState(dy))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withRecycledViewPool(inputRecycledViewPool())
                .onLayoutManager(this::setSpanSizeLookUp)
                .withGridLayoutManager(2)
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_rounds).setVisible(!tournament.isEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_rounds:
                showFragment(TournamentDetailFragment.newInstance(tournament));
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
    @StringRes
    protected int getFabStringResource() { return tournament.isEmpty() ? R.string.tournament_create : R.string.tournament_update; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    protected int getToolbarMenu() {
        return R.menu.fragment_tournament_edit;
    }

    @Override
    protected CharSequence getToolbarTitle() {
        return gofer.getToolbarTitle(this);
    }

    @Override
    public InsetFlags insetFlags() {return NO_TOP;}

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
        if (!tournament.isEmpty() && tournament.getNumCompetitors() == 0) promptForCompetitors();
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
                    showSnackbar(getString(stringRes, tournament.getName()));

                    if (wasEmpty) showFragment(TournamentDetailFragment.newInstance(tournament));
                    else onModelUpdated(diffResult);
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

    @Override
    public Sport getSport() { return tournament.getSport(); }

    private void promptForCompetitors() {
        if (showingPrompt) return;

        showingPrompt = true;
        showSnackbar(snackbar -> snackbar.setText(getString(R.string.add_tournament_competitors_prompt))
                .addCallback(new Snackbar.Callback() {
                    public void onDismissed(Snackbar bar, int event) { showingPrompt = false; }
                })
                .setAction(R.string.okay, view -> showFragment(CompetitorsFragment.newInstance(tournament))));
    }

    private void setSpanSizeLookUp(RecyclerView.LayoutManager layoutManager) {
        ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return gofer.getItems().get(position) instanceof Competitor ? 1 : 2;
            }
        });
    }
}
