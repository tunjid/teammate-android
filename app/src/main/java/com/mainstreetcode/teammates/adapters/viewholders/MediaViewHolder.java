package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.annotation.IdRes;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public abstract class MediaViewHolder extends BaseViewHolder<MediaAdapter.MediaAdapterListener> {

    public Media media;
    public ImageView thumbnailView;

    MediaViewHolder(View itemView, MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        thumbnailView = itemView.findViewById(getThumbnailId());
    }

    public void bind(Media media) {
        this.media = media;
        ViewCompat.setTransitionName(thumbnailView, media.hashCode() + "-" + thumbnailView.getId());
    }

    public void fullBind(Media media) {
       bind(media);
    }

    public void unBind() {

    }

    @IdRes
    public abstract int getThumbnailId();
}
