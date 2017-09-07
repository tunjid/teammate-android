package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammates.model.Media;
import com.mainstreetcode.teammates.model.User;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link User}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class MediaAdapter extends BaseRecyclerViewAdapter<MediaViewHolder, MediaAdapter.MediaAdapterListener> {

    private static final int IMAGE = 0;
    private static final int VIDEO = 1;

    private final List<Media> mediaList;

    public MediaAdapter(List<Media> mediaList, MediaAdapterListener listener) {
        super(listener);
        this.mediaList = mediaList;
        setHasStableIds(true);
    }

    @Override
    public MediaViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        @LayoutRes int resource = viewType == IMAGE ? R.layout.viewholder_image : R.layout.viewholder_video;
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(resource, viewGroup, false);
        return viewType == IMAGE
                ? new ImageMediaViewHolder(itemView, adapterListener)
                : new VideoMediaViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(MediaViewHolder viewHolder, int i) {
        viewHolder.bind(mediaList.get(i));
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mediaList.get(position).isImage() ? IMAGE : VIDEO;
    }

    @Override
    public long getItemId(int position) {
        return mediaList.get(position).hashCode();
    }

    public interface MediaAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onMediaClicked(Media item);
    }
}
