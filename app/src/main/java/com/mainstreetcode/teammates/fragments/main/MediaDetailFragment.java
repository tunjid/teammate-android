package com.mainstreetcode.teammates.fragments.main;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.util.Pair;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Media;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;


public class MediaDetailFragment extends MainActivityFragment {

    public static final String ARG_MEDIA = "media";

    private Media media;
    private MediaViewHolder mediaViewHolder;

    public static MediaDetailFragment newInstance(Media media) {
        MediaDetailFragment fragment = new MediaDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_MEDIA, media);
        fragment.setArguments(args);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Pair<Transition, Transition> pair = fragment.setDefaultSharedTransitions();

            if (pair != null) pair.first.excludeTarget(R.id.image, true);
        }

        return fragment;
    }

    @Override
    public String getStableTag() {
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_MEDIA);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        media = getArguments().getParcelable(ARG_MEDIA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        boolean isImage = media.isImage();
        int resource = isImage ? R.layout.viewholder_image : R.layout.viewholder_video;

        ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(resource, container, false);
        bindViewHolder(rootView, media, isImage);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mediaViewModel.getMedia(media).subscribe(updated -> {}, defaultErrorHandler);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mediaViewHolder != null) mediaViewHolder.unBind();

        mediaViewHolder = null;
    }

    @Override
    public boolean drawsBehindStatusBar() {
        return true;
    }

    @Override
    protected boolean showsFab() {
        return false;
    }

    @Override
    protected boolean showsBottomNav() {
        return false;
    }

    @Override
    protected boolean showsToolBar() {
        return false;
    }

    private void bindViewHolder(ConstraintLayout rootView, Media media, boolean isImage) {
        ViewGroup.LayoutParams params = rootView.getLayoutParams();
        params.height = MATCH_PARENT;

        rootView.setPadding(0, 0, 0, 0);

        mediaViewHolder = isImage
                ? new ImageMediaViewHolder(rootView, null)
                : new VideoMediaViewHolder(rootView, null);

        mediaViewHolder.fullBind(media);
    }
}
