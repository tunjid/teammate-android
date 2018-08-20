package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CompetitorsFragment extends MainActivityFragment
        implements TeamAdapter.TeamAdapterListener {

    private static final String ARG_TOURNAMENT = "tournament";

    private boolean isEditing = true;
    private Tournament tournament;
    private List<Competitor> items;
    private Set<Model> set = new HashSet<>();

    public static CompetitorsFragment newInstance(Tournament tournament) {
        CompetitorsFragment fragment = new CompetitorsFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TOURNAMENT, tournament);
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
        setHasOptionsMenu(true);
        tournament = getArguments().getParcelable(ARG_TOURNAMENT);
        items = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_competitors, container, false);
        CheckBox editing = rootView.findViewById(R.id.editing);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_bracket_white_24dp, R.string.add_tournament_competitors_detail))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new CompetitorAdapter(items, new BaseRecyclerViewAdapter.AdapterListener() {}))
                .withLinearLayoutManager()
                .withSwipeDragOptions(ScrollManager.swipeDragOptionsBuilder()
                        .setListSupplier(() -> items)
                        .build())
                .build();

        editing.setChecked(isEditing);
        editing.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isEditing = isChecked;
            togglePersistentUi();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollManager.notifyDataSetChanged();
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(isEditing ? R.drawable.ic_add_white_24dp : R.drawable.ic_check_white_24dp);
        setToolbarTitle(getString(R.string.add_tournament_competitors));
    }

    @Override
    public boolean showsFab() {
        return true;
    }

    @Override
    public void onTeamClicked(Team item) {
        if (set.contains(item)) showSnackbar(getString(R.string.competitor_exists));
        else addCompetitor(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (isEditing) findCompetitor();
                else addCompetitors();
                break;
        }
    }

    private void findCompetitor() {
        BaseFragment fragment = User.COMPETITOR_TYPE.equals(tournament.getRefPath())
                ? TeamMembersFragment.newInstance(tournament.getHost())
                : Team.COMPETITOR_TYPE.equals(tournament.getRefPath())
                ? TeamSearchFragment.newInstance()
                : null;

        if (fragment == null) return;
        fragment.setTargetFragment(this, R.id.request_competitor_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                //.setTitle(getString(R.string.pick_team))
                .setFragment(fragment)
                .build());
    }

    private void addCompetitor(Team item) {
        set.add(item);
        items.add(Competitor.empty(item));
        scrollManager.notifyDataSetChanged();
        hideBottomSheet();
    }

    private void addCompetitors() {
        disposables.add(tournamentViewModel.addCompetitors(tournament, items).subscribe(added -> requireActivity().onBackPressed(), defaultErrorHandler));
    }
}
