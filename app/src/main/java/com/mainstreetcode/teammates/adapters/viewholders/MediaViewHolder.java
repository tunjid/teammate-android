package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;


public abstract class MediaViewHolder extends BaseViewHolder<MediaAdapter.MediaAdapterListener> {

    static final String UNITY_ASPECT_RATIO = "1";

    public Media media;
    private View border;
    public ImageView thumbnailView;

    MediaViewHolder(View itemView, @NonNull MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        border = itemView.findViewById(R.id.border);
        thumbnailView = itemView.findViewById(getThumbnailId());

        if (!adapterListener.isFullScreen()) {
            itemView.setOnClickListener(view -> adapterListener.onMediaClicked(media));
            itemView.setOnLongClickListener(view -> {
                boolean consumed = adapterListener.onMediaLongClicked(media);
                border.setVisibility(consumed ? View.VISIBLE : View.GONE);
                return true;
            });
        }
    }

    public void bind(Media media) {
        this.media = media;
        setTransitionName(itemView, getTransitionName(media, R.id.fragment_media_background));
        setTransitionName(thumbnailView, getTransitionName(media, R.id.fragment_media_thumbnail));

        if (!adapterListener.isFullScreen()) {
            border.setVisibility(adapterListener.isSelected(media) ? View.VISIBLE : View.GONE);
        }
    }

    public void fullBind(Media media) {
        bind(media);
    }

    public void unBind() {}

    @IdRes
    public abstract int getThumbnailId();
}
