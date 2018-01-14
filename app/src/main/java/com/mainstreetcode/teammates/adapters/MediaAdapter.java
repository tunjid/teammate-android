package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Media;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Media}
 */

public class MediaAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, MediaAdapter.MediaAdapterListener> {

    private static final int IMAGE = 7;
    private static final int VIDEO = 8;

    private final List<Identifiable> mediaList;

    public MediaAdapter(List<Identifiable> mediaList, MediaAdapterListener listener) {
        super(listener);
        this.mediaList = mediaList;
        setHasStableIds(true);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return viewType == AD
                ? new ContentAdViewHolder(getItemView(R.layout.viewholder_content_ad, viewGroup), adapterListener)
                : viewType == IMAGE
                ? new ImageMediaViewHolder(getItemView(R.layout.viewholder_image, viewGroup), adapterListener)
                : new VideoMediaViewHolder(getItemView(R.layout.viewholder_video, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable identifiable = mediaList.get(position);
        if (identifiable instanceof Media) {
            ((MediaViewHolder) viewHolder).bind((Media) identifiable);
        }
        else if (identifiable instanceof Ad) {
            ((ContentAdViewHolder) viewHolder).bind((Ad) identifiable);
        }
    }

    @Override
    public int getItemCount() {
        return mediaList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable identifiable = mediaList.get(position);
        return identifiable instanceof Media ? (((Media) identifiable).isImage() ? IMAGE : VIDEO) : AD;
    }

    @Override
    public long getItemId(int position) {
        return mediaList.get(position).hashCode();
    }

    public interface MediaAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onMediaClicked(Media item);

        boolean onMediaLongClicked(Media media);

        boolean isSelected(Media media);

        boolean isFullScreen();
    }
}
