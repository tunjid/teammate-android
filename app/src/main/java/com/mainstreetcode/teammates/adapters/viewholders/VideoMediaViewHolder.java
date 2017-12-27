package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.transition.Fade;
import android.support.transition.TransitionManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.squareup.picasso.Picasso;


public class VideoMediaViewHolder extends MediaViewHolder {

    private VideoView videoView;

    public VideoMediaViewHolder(View itemView, MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        videoView = itemView.findViewById(R.id.video_thumbnail);

        if (!adapterListener.isFullScreen()) {
            videoView.setOnClickListener(view -> adapterListener.onMediaClicked(media));
        }
    }

    @Override
    public void bind(Media media) {
        super.bind(media);

        String thumbnail = media.getThumbnail();

        if (TextUtils.isEmpty(thumbnail)) return;

        Picasso.with(itemView.getContext())
                .load(thumbnail)
                .fit()
                .centerInside()
                .into(thumbnailView);
    }

    @Override
    public void fullBind(Media media) {
        super.fullBind(media);

        itemView.setBackgroundResource(R.color.black);

        String videoUrl = media.getUrl();

        if (TextUtils.isEmpty(videoUrl)) return;

        videoView.setVideoPath(videoUrl);
        videoView.setOnPreparedListener(() -> {
            TransitionManager.beginDelayedTransition((ViewGroup)itemView, new Fade());
            videoView.setVisibility(View.VISIBLE);
            videoView.start();
        });
    }

    @Override
    public void unBind() {
        super.unBind();
        videoView.setVisibility(View.INVISIBLE);
        videoView.release();
    }

    @Override
    public int getThumbnailId() {
        return R.id.thumbnail;
    }
}
