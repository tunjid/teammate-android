package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.adapters.UserAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.baseclasses.BottomSheetController;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Competitive;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.model.User;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.functions.collections.Lists;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public final class CompetitorsFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener {

    private static final String ARG_TOURNAMENT = "tournament";

    private Tournament tournament;
    private List<Competitive> entities;
    private List<Competitor> competitors;
    private List<Differentiable> competitorDifferentiables;

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
        entities = new ArrayList<>();
        competitors = Lists.transform(entities, Competitor::empty, Competitor::getEntity);
        competitorDifferentiables = Lists.transform(competitors, identity -> identity, i -> (Competitor) i);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_competitors, container, false);
        scrollManager = ScrollManager.<CompetitorViewHolder>with(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(new EmptyViewHolder(rootView, R.drawable.ic_bracket_white_24dp, R.string.add_tournament_competitors_detail))
                .withAdapter(new CompetitorAdapter(competitorDifferentiables, competitor -> {}) {
                    @Override
                    public long getItemId(int position) { return entities.get(position).hashCode(); }

                    @NonNull @Override
                    public CompetitorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                        CompetitorViewHolder holder = super.onCreateViewHolder(viewGroup, viewType);
                        holder.getDragHandle().setVisibility(View.VISIBLE);
                        return holder;
                    }
                })
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .withSwipeDragOptions(ScrollManager.<CompetitorViewHolder>swipeDragOptionsBuilder()
                        .setMovementFlagsFunction(viewHolder -> ScrollManager.SWIPE_DRAG_ALL_DIRECTIONS)
                        .setSwipeConsumer((holder, position) -> removeCompetitor(holder))
                        .setDragHandleFunction(CompetitorViewHolder::getDragHandle)
                        .setLongPressDragEnabledSupplier(() -> false)
                        .setItemViewSwipeSupplier(() -> true)
                        .setDragConsumer(this::moveCompetitor)
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
    protected void onKeyBoardChanged(boolean appeared) {
        super.onKeyBoardChanged(appeared);
        if (!appeared && isBottomSheetShowing()) hideBottomSheet();
    }

    @Override
    public boolean showsFab() {
        return !isBottomSheetShowing() && !competitors.isEmpty();
    }

    @Override
    protected CharSequence getToolbarTitle() {
        return getString(R.string.add_tournament_competitors);
    }

    @Override
    public void onUserClicked(User item) {
        if (entities.contains(item)) showSnackbar(getString(R.string.competitor_exists));
        else addCompetitor(item);
    }

    @Override
    public void onTeamClicked(Team item) {
        if (entities.contains(item)) showSnackbar(getString(R.string.competitor_exists));
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
        boolean isBetweenUsers = User.COMPETITOR_TYPE.equals(tournament.getRefPath());
        BaseFragment fragment = isBetweenUsers
                ? TeamMembersFragment.newInstance(tournament.getHost())
                : Team.COMPETITOR_TYPE.equals(tournament.getRefPath())
                ? TeamSearchFragment.newInstance(tournament.getSport())
                : null;

        if (fragment == null) return;
        fragment.setTargetFragment(this, R.id.request_competitor_pick);

        BottomSheetController.Builder builder = BottomSheetController.Args.builder()
                .setMenuRes(R.menu.empty)
                .setFragment(fragment);

        if (isBetweenUsers) builder.setTitle(getString(R.string.add_competitor));
        showBottomSheet(builder.build());
    }

    private void addCompetitor(Competitive item) {
        if (!tournament.getRefPath().equals(item.getRefType())) return;

        entities.add(item);
        scrollManager.notifyDataSetChanged();
        hideKeyboard();
    }

    private void addCompetitors() {
        disposables.add(tournamentViewModel.addCompetitors(tournament, competitors).subscribe(added -> requireActivity().onBackPressed(), defaultErrorHandler));
    }

    private void moveCompetitor(CompetitorViewHolder start, CompetitorViewHolder end) {
        int from = start.getAdapterPosition();
        int to = end.getAdapterPosition();

        swap(from, to);
        scrollManager.notifyItemMoved(from, to);
        scrollManager.notifyItemChanged(from);
        scrollManager.notifyItemChanged(to);
    }

    private void removeCompetitor(CompetitorViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        Pair<Integer, Integer> minMax = remove(position);

        scrollManager.notifyItemRemoved(position);
        // Only necessary to rebind views lower so they have the right position
        scrollManager.notifyItemRangeChanged(minMax.first, minMax.second);
    }

    private void swap(int from, int to) {
        if (from < to)
            for (int i = from; i < to; i++) Collections.swap(competitorDifferentiables, i, i + 1);
        else for (int i = from; i > to; i--) Collections.swap(competitorDifferentiables, i, i - 1);
    }

    private Pair<Integer, Integer> remove(int position) {
        competitorDifferentiables.remove(position);

        int lastIndex = competitorDifferentiables.size() - 1;
        return new Pair<>(Math.min(position, lastIndex), lastIndex);
    }
}
