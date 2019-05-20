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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.StatRankAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Event;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.enums.StatType;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

/**
 * Lists {@link Event tournaments}
 */

public final class StatRankFragment extends MainActivityFragment {

    private static final String ARG_TOURNAMENT = "team";

    private StatType type;
    private Tournament tournament;
    private List<Differentiable> statRanks;

    public static StatRankFragment newInstance(Tournament team) {
        StatRankFragment fragment = new StatRankFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TOURNAMENT, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        String superResult = super.getStableTag();
        Tournament tempTournament = getArguments().getParcelable(ARG_TOURNAMENT);

        return (tempTournament != null)
                ? superResult + "-" + tempTournament.hashCode()
                : superResult;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
        statRanks = tournamentViewModel.getStatRanks(tournament);
        type = tournament.getSport().statTypeFromCode("");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stat_rank, container, false);
        Spinner spinner = root.findViewById(R.id.spinner);

        scrollManager = ScrollManager.<InteractiveViewHolder>with(root.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(root, R.drawable.ic_medal_24dp, R.string.no_stat_ranks))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new StatRankAdapter(statRanks, statRank -> showFragment(UserEditFragment.newInstance(statRank.getUser()))))
                .withLinearLayoutManager()
                .build();

        scrollManager.setViewHolderColor(R.attr.alt_empty_view_holder_tint);

        StatType[] statTypes = tournament.getSport().getStats().toArray(new StatType[]{});
        ArrayAdapter<StatType> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, statTypes);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                type.update(statTypes[position]);
                fetchStandings();
            }

            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchStandings();
    }

    @Override
    public void togglePersistentUi() {/* Do nothing */}

    @Override
    public boolean showsFab() { return false; }

    private void fetchStandings() {
        toggleProgress(true);
        disposables.add(tournamentViewModel.getStatRank(tournament, type).subscribe(this::onTournamentsUpdated, defaultErrorHandler));
    }

    private void onTournamentsUpdated(DiffUtil.DiffResult diff) {
        scrollManager.onDiff(diff);
        toggleProgress(false);
    }
}
