package com.mainstreetcode.teammate.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.MediaAdapter;
import com.mainstreetcode.teammate.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Media;
import com.tunjid.androidbootstrap.view.util.InsetFlags;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;


public class MediaDetailFragment extends MainActivityFragment
        implements MediaAdapter.MediaAdapterListener {

    static final String ARG_MEDIA = "media";

    private Media media;
    private MediaViewHolder mediaViewHolder;
    private AtomicBoolean systemUiStatus;

    public static MediaDetailFragment newInstance(Media media) {
        MediaDetailFragment fragment = new MediaDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_MEDIA, media);
        fragment.setArguments(args);
        fragment.setEnterExitTransitions();

        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_MEDIA);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        media = getArguments().getParcelable(ARG_MEDIA);
        systemUiStatus = new AtomicBoolean();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean isImage = media.isImage();
        int resource = isImage ? R.layout.viewholder_image : R.layout.viewholder_video;

        return inflater.inflate(resource, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disposables.add(mediaViewModel.getMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_media_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.action_flag_media) return super.onOptionsItemSelected(item);

        Activity activity = getActivity();
        if (activity == null) return true;

        new AlertDialog.Builder(activity)
                .setTitle(R.string.flag_media)
                .setMessage(R.string.flag_media_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> disposables.add(mediaViewModel.flagMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler)))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null) return;

        boolean isImage = media.isImage();
        bindViewHolder(root, media, isImage);
    }

    @Override
    public void onPause() {
        if (mediaViewHolder != null) mediaViewHolder.unBind();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        mediaViewHolder = null;
        super.onDestroyView();
    }

    @Override
    public InsetFlags insetFlags() { return NONE; }

    @Override
    public boolean showsFab() { return false; }

    @Override
    public boolean showsBottomNav() { return false; }

    @Override
    public boolean showsToolBar() { return true; }

    @Override
    protected boolean showsSystemUI() { return systemUiStatus.get(); }

    @Override
    public void onMediaClicked(Media item) {
        Activity activity = getActivity();
        if (activity == null) return;

        int visibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        boolean status = (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
        systemUiStatus.set(status);
        togglePersistentUi();
    }

    @Override
    public boolean onMediaLongClicked(Media media) {
        return false;
    }

    @Override
    public boolean isSelected(Media media) {
        return false;
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    private void checkMediaFlagged(Media media) {
        if (!media.isFlagged()) return;
        showSnackbar(getString(R.string.media_flagged));

        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }

    private void bindViewHolder(View rootView, Media media, boolean isImage) {
        mediaViewHolder = isImage
                ? new ImageMediaViewHolder(rootView, this)
                : new VideoMediaViewHolder(rootView, this);

        mediaViewHolder.fullBind(media);
        onMediaClicked(media);
    }
}
