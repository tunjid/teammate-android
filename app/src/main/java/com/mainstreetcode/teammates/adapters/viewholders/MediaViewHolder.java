package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.annotation.IdRes;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;


public abstract class MediaViewHolder extends BaseViewHolder<MediaAdapter.MediaAdapterListener> {

    public Media media;
    public ImageView thumbnailView;

    MediaViewHolder(View itemView, MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        thumbnailView = itemView.findViewById(getThumbnailId());
    }

    public void bind(Media media) {
        this.media = media;
        setTransitionName(itemView, getTransitionName(media, R.id.fragment_media_background));
        setTransitionName(thumbnailView, getTransitionName(media, R.id.fragment_media_thumbnail));
    }

    public void fullBind(Media media) {
       bind(media);
    }

    public void unBind() {

    }

    @IdRes
    public abstract int getThumbnailId();
}
