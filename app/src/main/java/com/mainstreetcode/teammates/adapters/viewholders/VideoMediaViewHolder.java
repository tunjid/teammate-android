package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;

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

        if (adapterListener != null) {
            videoView.setOnClickListener(view -> adapterListener.onMediaClicked(media));
        }
    }

    @Override
    public void bind(Media media) {
        super.bind(media);

        videoView.getVideoControls().hide();
        String thumbnail = media.getThumbnail();

        if (TextUtils.isEmpty(thumbnail)) return;

        Picasso.with(itemView.getContext())
                .load(thumbnail)
                .fit()
                .centerInside()
                .into(videoView.getPreviewImageView());
    }

    @Override
    public void fullBind(Media media) {
        super.fullBind(media);

        videoView.getVideoControls().show();
        String videoUrl = media.getUrl();

        if (TextUtils.isEmpty(videoUrl)) return;

        videoView.setVideoPath(videoUrl);
        videoView.setOnPreparedListener(() -> videoView.start());
    }

    @Override
    public void unBind() {
        super.unBind();

        videoView.release();
    }

    @Override
    public int getThumbnailId() {
        return R.id.video_thumbnail;
    }
}
