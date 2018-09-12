package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class CompetitorsFragment extends MainActivityFragment
        implements
        TeamAdapter.TeamAdapterListener,
        ViewHolderUtil.SimpleAdapterListener<User> {

    private static final String ARG_TOURNAMENT = "tournament";

    private Tournament tournament;
    private List<Competitor> items;
    private Set<Competitive> set = new HashSet<>();
    AtomicReference<Pair<Long, Integer>> dragRef = new AtomicReference<>();

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
        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_bracket_white_24dp, R.string.add_tournament_competitors_detail))
                .withAdapter(new CompetitorAdapter(items, this::onDragStarted))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .withSwipeDragOptions(ScrollManager.swipeDragOptionsBuilder()
                        .setSwipeDragEndConsumer(this::onSwipeDragEnded)
                        .setLongPressDragEnabledSupplier(() -> false)
                        .setListSupplier(() -> items)
                        .build())
                .build();

        rootView.findViewById(R.id.add_competitor).setOnClickListener(view -> findCompetitor());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        scrollManager.notifyDataSetChanged();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.save_tournament_competitors; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_check_white_24dp; }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        updateFabIcon();
        setFabClickListener(this);
        setToolbarTitle(getString(R.string.add_tournament_competitors));
    }

    @Override
    public boolean showsFab() {
        return !items.isEmpty();
    }

    @Override
    public void onTeamClicked(Team item) {
        if (set.contains(item)) showSnackbar(getString(R.string.competitor_exists));
        else addCompetitor(item);
    }

    @Override
    public void onItemClicked(User item) {
        if (set.contains(item)) showSnackbar(getString(R.string.competitor_exists));
        else addCompetitor(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                addCompetitors();
                break;
        }
    }

    private void findCompetitor() {
        BaseFragment fragment = User.COMPETITOR_TYPE.equals(tournament.getRefPath())
                ? TeamMembersFragment.newInstance(tournament.getHost())
                : Team.COMPETITOR_TYPE.equals(tournament.getRefPath())
                ? TeamSearchFragment.newInstance(tournament)
                : null;

        if (fragment == null) return;
        fragment.setTargetFragment(this, R.id.request_competitor_pick);

        showBottomSheet(BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment)
                .build());
    }

    private void addCompetitor(Competitive item) {
        if (!tournament.getRefPath().equals(item.getRefType())) return;

        set.add(item);
        items.add(Competitor.empty(item));
        scrollManager.notifyDataSetChanged();
        scrollManager.getRecyclerView().postDelayed(this::hideBottomSheet, 200);
        hideKeyboard();
    }

    private void addCompetitors() {
        disposables.add(tournamentViewModel.addCompetitors(tournament, items).subscribe(added -> requireActivity().onBackPressed(), defaultErrorHandler));
    }

    private void onDragStarted(RecyclerView.ViewHolder viewHolder) {
        dragRef.set(new Pair<>(viewHolder.getItemId(), viewHolder.getAdapterPosition()));
        scrollManager.startDrag(viewHolder);
    }

    private void onSwipeDragEnded(RecyclerView.ViewHolder viewHolder) {
        Pair<Long, Integer> pair = dragRef.get();
        if (pair == null || pair.first != viewHolder.getItemId()) return;
        int from = pair.second;
        int to = viewHolder.getAdapterPosition();
        scrollManager.notifyItemRangeChanged(Math.min(from, to), 1+Math.abs(from - to));
        dragRef.set(null);
    }
}
