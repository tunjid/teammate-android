package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;

import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;


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

        String videoUrl = media.getImageUrl();

        if (TextUtils.isEmpty(videoUrl)) return;

        videoView.setVideoPath(videoUrl);

        if (adapterListener == null) videoView.setOnPreparedListener(() -> videoView.start());
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
