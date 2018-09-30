package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.adapters.DragDropAdapter;
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
import com.mainstreetcode.teammate.util.TransformingSequentialList;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_DRAG;
import static android.support.v7.widget.helper.ItemTouchHelper.ACTION_STATE_SWIPE;

public final class CompetitorsFragment extends MainActivityFragment
        implements
        UserAdapter.AdapterListener,
        TeamAdapter.AdapterListener {

    private static final int SWIPE_DELAY = 200;
    private static final int NO_SWIPE_OR_DRAG = 0;
    private static final String ARG_TOURNAMENT = "tournament";

    private boolean canMove = true;
    private Tournament tournament;
    private List<Competitive> entities;
    private List<Competitor> competitors;
    private AtomicReference<SwipeDragData<Integer>> dragRef = new AtomicReference<>();
    private AtomicReference<SwipeDragData<Integer>> swipeRef = new AtomicReference<>();

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
        competitors = new TransformingSequentialList<>(entities, Competitor::empty, Competitor::getEntity);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_competitors, container, false);
        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_bracket_white_24dp, R.string.add_tournament_competitors_detail))
                .withAdapter(new DragDropAdapter<>(new CompetitorAdapter(competitors, competitor -> {}), CompetitorViewHolder::getDragHandle, this::onDragStarted))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .withSwipeDragOptions(ScrollManager.swipeDragOptionsBuilder()
                        .setMovementFlagsSupplier(viewHolder -> getMovementFlags())
                        .setSwipeDragStartConsumerConsumer(this::onSwipeStarted)
                        .setSwipeDragEndConsumer(this::onSwipeDragEnded)
                        .setLongPressDragEnabledSupplier(() -> false)
                        .setItemViewSwipeSupplier(() -> true)
                        .setListSupplier(() -> competitors)
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
        return !competitors.isEmpty();
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
        scrollManager.getRecyclerView().postDelayed(this::hideBottomSheet, 200);
        hideKeyboard();
    }

    private void addCompetitors() {
        disposables.add(tournamentViewModel.addCompetitors(tournament, competitors).subscribe(added -> requireActivity().onBackPressed(), defaultErrorHandler));
    }

    private void onDragStarted(RecyclerView.ViewHolder viewHolder) {
        dragRef.set(new SwipeDragData<>(viewHolder.getItemId(), viewHolder.getAdapterPosition(), ACTION_STATE_DRAG, 0));
        scrollManager.startDrag(viewHolder);
    }

    private void onSwipeStarted(RecyclerView.ViewHolder viewHolder, int state) {
        if (state != ACTION_STATE_SWIPE) return;
        swipeRef.set(new SwipeDragData<>(viewHolder.getItemId(), viewHolder.getAdapterPosition(), ACTION_STATE_SWIPE, competitors.size()));
    }

    private void onSwipeDragEnded(RecyclerView.ViewHolder viewHolder) {
        long id = viewHolder.getItemId();
        SwipeDragData<Integer> dragData = dragRef.get();
        SwipeDragData<Integer> swipeData = swipeRef.get();

        if (dragData != null && id == dragData.id) completeDrag(viewHolder, dragData);
        else if (swipeData != null && id == swipeData.id) completeSwipe(swipeData);
    }

    private void completeDrag(RecyclerView.ViewHolder viewHolder, SwipeDragData<Integer> dragData) {
        int from = dragData.position;
        int to = viewHolder.getAdapterPosition();
        scrollManager.notifyItemRangeChanged(Math.min(from, to), 1 + Math.abs(from - to));
        dragRef.set(null);
    }

    private void completeSwipe(SwipeDragData<Integer> swipeData) {
        if (competitors.size() == swipeData.meta) return;
        swipeRef.set(null);
        canMove = false;
        scrollManager.getRecyclerView().postDelayed(() -> {
            canMove = true;
            scrollManager.notifyDataSetChanged();
        }, SWIPE_DELAY);
    }

    private int getMovementFlags() {
        return canMove ? ScrollManager.defaultMovements() : NO_SWIPE_OR_DRAG;
    }

    static class SwipeDragData<T> {
        long id;
        int position;
        int dragSwipeType;
        T meta;

        SwipeDragData(long id, int position, int dragSwipeType, T meta) {
            this.id = id;
            this.position = position;
            this.dragSwipeType = dragSwipeType;
            this.meta = meta;
        }
    }
}
