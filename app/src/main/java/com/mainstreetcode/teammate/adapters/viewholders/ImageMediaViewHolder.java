package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.MediaAdapter;
import com.mainstreetcode.teammate.model.Media;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.FULL_RES_LOAD_DELAY;


public class ImageMediaViewHolder extends MediaViewHolder<ImageView> {

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
