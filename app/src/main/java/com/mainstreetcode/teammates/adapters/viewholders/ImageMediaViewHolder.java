package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;


public class ImageMediaViewHolder extends MediaViewHolder<ImageView> {

    private static final int FULL_RES_LOAD_DELAY = 250;

    public ImageMediaViewHolder(View itemView, MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
    }

    @Override
    public void fullBind(Media media) {
        super.fullBind(media);
        itemView.postDelayed(() -> loadImage(media.getUrl(), true, fullResView), FULL_RES_LOAD_DELAY);
    }

    @Override
    public int getThumbnailId() {return R.id.image_thumbnail;}

    @Override
    public int getFullViewId() {return R.id.image_full_res;}

    @Override
    public void onSuccess() {fullResView.setVisibility(View.VISIBLE);}
}
