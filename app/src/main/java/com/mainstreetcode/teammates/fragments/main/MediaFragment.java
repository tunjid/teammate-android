package com.mainstreetcode.teammates.fragments.main;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.Fade;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.MediaUploadIntentService;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.EndlessScroller;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.Date;
import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;

public class MediaFragment extends MainActivityFragment
        implements
        MediaAdapter.MediaAdapterListener,
        ImageWorkerFragment.MediaListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Media> mediaList;
    private Toolbar contextBar;
    private RecyclerView recyclerView;
    private EmptyViewHolder emptyViewHolder;

    public static MediaFragment newInstance(Team team) {
        MediaFragment fragment = new MediaFragment();
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
        mediaList = mediaViewModel.getMediaList(team);

        ImageWorkerFragment.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media, container, false);

        contextBar = rootView.findViewById(R.id.alt_toolbar);
        recyclerView = rootView.findViewById(R.id.team_media);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new MediaAdapter(mediaList, this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });
        recyclerView.addOnScrollListener(new EndlessScroller(layoutManager) {
            @Override
            public void onLoadMore(int oldCount) {
                toggleProgress(true);
                fetchMedia(getQueryDate());
            }
        });

        contextBar.inflateMenu(R.menu.fragment_media_context);
        contextBar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        emptyViewHolder = new EmptyViewHolder(rootView, R.drawable.ic_video_library_black_24dp, R.string.no_media);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.meda_title, team.getName()));
        setFabIcon(R.drawable.ic_add_white_24dp);
        getFab().setOnClickListener(view -> ImageWorkerFragment.requestMultipleMedia(this));
        fetchMedia(new Date());
    }

    void fetchMedia(Date date) {
        disposables.add(mediaViewModel.getTeamMedia(mediaList, team, date).subscribe(this::onMediaUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_media, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                teamViewModel.updateDefaultTeam(Team.empty());
                TeamPickerFragment.pick(getActivity(), R.id.request_media_team_pick);
                return true;
            case R.id.action_delete:
                mediaViewModel.deleteMedia(team).subscribe(diffResult -> {
                    diffResult.dispatchUpdatesTo(recyclerView.getAdapter());
                    toggleContextMenu(false);
                }, defaultErrorHandler);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        emptyViewHolder = null;
    }

    @Override
    public boolean handledBackPress() {
        if (!mediaViewModel.hasSelections(team)) return false;
        mediaViewModel.clearSelections(team);
        recyclerView.getAdapter().notifyDataSetChanged();
        toggleContextMenu(false);
        return true;
    }

    @Override
    protected boolean showsFab() {
        return true;
    }

    @Override
    protected boolean showsBottomNav() {
        return false;
    }

    @Override
    @Nullable
    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (fragmentTo.getStableTag().contains(MediaDetailFragment.class.getSimpleName())) {
            Media media = fragmentTo.getArguments().getParcelable(MediaDetailFragment.ARG_MEDIA);

            if (media == null) return null;
            MediaViewHolder holder = (MediaViewHolder) recyclerView.findViewHolderForItemId(media.hashCode());

            holder.bind(media); // Rebind, to make sure transition names remain.
            return beginTransaction()
                    .addSharedElement(holder.itemView, getTransitionName(media, R.id.fragment_media_background))
                    .addSharedElement(holder.thumbnailView, getTransitionName(media, R.id.fragment_media_thumbnail));
        }
        return null;
    }

    @Override
    public void onMediaClicked(Media item) {
        if (mediaViewModel.hasSelections(team)) longClickMedia(item);
        else showFragment(MediaDetailFragment.newInstance(item));
    }

    @Override
    public boolean onMediaLongClicked(Media media) {
        boolean result = mediaViewModel.select(media);
        boolean hasSelections = mediaViewModel.hasSelections(team);

        toggleContextMenu(hasSelections);
        return result;
    }

    @Override
    public boolean isSelected(Media media) {
        return mediaViewModel.isSelected(media);
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void onFilesSelected(List<Uri> uris) {
        MediaUploadIntentService.startActionUpload(getContext(), userViewModel.getCurrentUser(), team, uris);
    }

    private Date getQueryDate() {
        return mediaList.isEmpty() ? new Date() : mediaList.get(mediaList.size() - 1).getCreated();
    }

    private void onMediaUpdated(DiffUtil.DiffResult result) {
        result.dispatchUpdatesTo(recyclerView.getAdapter());
        toggleProgress(false);
        emptyViewHolder.toggle(mediaList.isEmpty());
    }

    private void toggleContextMenu(boolean show) {
        boolean current = contextBar.getVisibility() == View.VISIBLE;
        ViewGroup root = (ViewGroup) getView();
        if (current == show || root == null) return;

        Transition transition = new Fade();
        transition.addTarget(contextBar);

        TransitionManager.beginDelayedTransition(root, transition);
        contextBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        getTeammatesActivity().toggleToolbar(!show);
    }

    private void longClickMedia(Media media) {
        MediaViewHolder holder = (MediaViewHolder) recyclerView.findViewHolderForItemId(media.hashCode());
        if (holder == null) return;

        holder.performLongClick();

    }
}
