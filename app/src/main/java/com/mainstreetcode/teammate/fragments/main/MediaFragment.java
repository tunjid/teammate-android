package com.mainstreetcode.teammate.fragments.main;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.MediaTransferIntentService;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.MediaAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ErrorHandler;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;

public class MediaFragment extends MainActivityFragment
        implements
        MediaAdapter.MediaAdapterListener,
        ImageWorkerFragment.MediaListener,
        ImageWorkerFragment.DownloadRequester {

    private static final int MEDIA_DELETE_SNACKBAR_DELAY = 350;
    private static final String ARG_TEAM = "team";

    private Team team;
    private List<Identifiable> items;
    private AtomicBoolean bottomBarState;

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
        items = mediaViewModel.getModelList(team);
        bottomBarState = new AtomicBoolean();

        ImageWorkerFragment.attach(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_media, container, false);

        Runnable refreshAction = () -> disposables.add(mediaViewModel.refresh(team).subscribe(MediaFragment.this::onMediaUpdated, defaultErrorHandler));

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_media))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_video_library_black_24dp, R.string.no_media))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScrollCallback(() -> fetchMedia(false))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new MediaAdapter(items, this))
                .addStateListener(this::updateFabForScrollState)
                .addScrollListener(this::updateFabOnScroll)
                .withGridLayoutManager(4)
                .build();

        bottomBarState.set(true);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchMedia(true);
        toggleContextMenu(mediaViewModel.hasSelections(team));
        disposables.add(localRoleViewModel.getRoleInTeam(userViewModel.getCurrentUser(), team)
                .subscribe(() -> {}, ErrorHandler.EMPTY));
    }

    @Override
    public void togglePersistentUi() {
        updateFabIcon();
        setFabClickListener(this);
        setAltToolbarMenu(R.menu.fragment_media_context);
        setToolbarTitle(getString(R.string.media_title, team.getName()));
        super.togglePersistentUi();
    }

    @Override
    @StringRes
    protected int getFabStringResource() { return R.string.media_add; }

    @Override
    @DrawableRes
    protected int getFabIconResource() { return R.drawable.ic_add_white_24dp; }

    private void fetchMedia(boolean fetchLatest) {
        if (fetchLatest) scrollManager.setRefreshing();
        else toggleProgress(true);

        disposables.add(mediaViewModel.getMany(team, fetchLatest).subscribe(this::onMediaUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_media, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_team:
                TeamPickerFragment.change(getActivity(), R.id.request_media_team_pick);
                return true;
            case R.id.action_delete:
                mediaViewModel.deleteMedia(team, localRoleViewModel.hasPrivilegedRole())
                        .subscribe(this::onMediaDeleted, defaultErrorHandler);
                return true;
            case R.id.action_download:
                if (ImageWorkerFragment.requestDownload(this, team))
                    scrollManager.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handledBackPress() {
        if (!mediaViewModel.hasSelections(team)) return false;
        mediaViewModel.clearSelections(team);
        scrollManager.notifyDataSetChanged();
        toggleContextMenu(false);
        return true;
    }

    @Override
    public boolean showsFab() {return true;}

    @Override
    public boolean showsBottomNav() {return bottomBarState.get();}

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                ImageWorkerFragment.requestMultipleMedia(this);
                break;
        }
    }

    @Override
    @Nullable
    @SuppressLint("CommitTransaction")
    @SuppressWarnings("ConstantConditions")
    public FragmentTransaction provideFragmentTransaction(BaseFragment fragmentTo) {
        if (fragmentTo.getStableTag().contains(MediaDetailFragment.class.getSimpleName())) {
            Media media = fragmentTo.getArguments().getParcelable(MediaDetailFragment.ARG_MEDIA);

            if (media == null) return null;
            MediaViewHolder holder = (MediaViewHolder) scrollManager.findViewHolderForItemId(media.hashCode());
            if (holder == null) return null;

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
        else {
            bottomBarState.set(false);
            toggleBottombar(false);
            showFragment(MediaDetailFragment.newInstance(item));
        }
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
        MediaTransferIntentService.startActionUpload(getContext(), userViewModel.getCurrentUser(), team, uris);
    }

    @Override
    public Team requestedTeam() {
        return team;
    }

    @Override
    public void startedDownLoad(boolean started) {
        toggleContextMenu(!started);
        if (started) scrollManager.notifyDataSetChanged();
    }

    private void onMediaUpdated(DiffUtil.DiffResult result) {
        scrollManager.onDiff(result);
        toggleProgress(false);
    }

    private void toggleContextMenu(boolean show) {
        setAltToolbarTitle(getString(R.string.multi_select, mediaViewModel.getNumSelected(team)));
        toggleAltToolbar(show);
    }

    private void longClickMedia(Media media) {
        MediaViewHolder holder = (MediaViewHolder) scrollManager.findViewHolderForItemId(media.hashCode());
        if (holder == null) return;

        holder.performLongClick();
    }

    private void onMediaDeleted(Pair<Boolean, DiffUtil.DiffResult> pair) {
        toggleContextMenu(false);

        boolean partialDelete = pair.first == null ? false : pair.first;
        DiffUtil.DiffResult diffResult = pair.second;

        if (diffResult != null) scrollManager.onDiff(diffResult);
        if (!partialDelete) return;

        scrollManager.notifyDataSetChanged();
        scrollManager.getRecyclerView().postDelayed(() -> showSnackbar(getString(R.string.partial_delete_message)), MEDIA_DELETE_SNACKBAR_DELAY);
    }
}
