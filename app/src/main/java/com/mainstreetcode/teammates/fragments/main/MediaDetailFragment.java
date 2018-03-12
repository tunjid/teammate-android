package com.mainstreetcode.teammates.fragments.main;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Media;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class MediaDetailFragment extends MainActivityFragment
        implements MediaAdapter.MediaAdapterListener {

    public static final String ARG_MEDIA = "media";

    private Media media;
    private MediaViewHolder mediaViewHolder;

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
        setToolbarTitle("");
        mediaViewModel.getMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler);
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
                .setPositiveButton(R.string.yes, (dialog, which) -> mediaViewModel.flagMedia(media).subscribe(this::checkMediaFlagged, defaultErrorHandler))
                .setNegativeButton(R.string.no, (dialog, which) -> dialog.dismiss())
                .show();

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean isImage = media.isImage();
        bindViewHolder((ConstraintLayout) getView(), media, isImage);
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
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    public boolean showsBottomNav() {
        return false;
    }

    @Override
    public boolean showsToolBar() {
        return true;
    }

    @Override
    public void onMediaClicked(Media item) {
        Activity activity = getActivity();
        if (activity == null) return;

        int visibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        boolean status = (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
        toggleSystemUI(status);
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

    private void bindViewHolder(ConstraintLayout rootView, Media media, boolean isImage) {
        ViewGroup.LayoutParams params = rootView.getLayoutParams();
        params.height = MATCH_PARENT;

        rootView.setPadding(0, 0, 0, 0);

        mediaViewHolder = isImage
                ? new ImageMediaViewHolder(rootView, this)
                : new VideoMediaViewHolder(rootView, this);

        mediaViewHolder.fullBind(media);
        onMediaClicked(media);
    }
}
