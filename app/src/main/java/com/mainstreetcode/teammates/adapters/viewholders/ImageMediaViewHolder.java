package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.github.florent37.picassopalette.PicassoPalette;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import static com.github.florent37.picassopalette.PicassoPalette.Profile.MUTED_DARK;


public class ImageMediaViewHolder extends MediaViewHolder
        implements Callback {

    private static final int FULL_RES_LOAD_DELAY = 400;
    private static final int THUMBNAIL_SIZE = 200;

    private ImageView fullResView;

    public ImageMediaViewHolder(View itemView, MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);

        fullResView = itemView.findViewById(R.id.image_full_res);

        if (!adapterListener.isFullScreen()) {
            ConstraintLayout constraintLayout = (ConstraintLayout) itemView;
            ConstraintSet set = new ConstraintSet();

            set.clone(constraintLayout);
            set.setDimensionRatio(thumbnailView.getId(), UNITY_ASPECT_RATIO);
            set.setDimensionRatio(fullResView.getId(), UNITY_ASPECT_RATIO);
            set.applyTo(constraintLayout);
        }
    }

    @Override
    public void bind(Media media) {
        super.bind(media);

        loadImage(media.getThumbnail(), false, thumbnailView);
    }

    @Override
    public void fullBind(Media media) {
        super.fullBind(media);
        itemView.postDelayed(() -> loadImage(media.getUrl(), true, fullResView), FULL_RES_LOAD_DELAY);
    }

    private void loadImage(String url, boolean fitToSize, ImageView destination) {
        boolean isFullScreen = adapterListener.isFullScreen();
        if (TextUtils.isEmpty(url)) return;

        RequestCreator creator = Picasso.with(itemView.getContext()).load(url);

        if (!isFullScreen) creator.placeholder(R.drawable.bg_image_placeholder);

        creator = fitToSize ? creator.fit() : creator.resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE);
        creator = creator.centerInside();

        Callback callBack = isFullScreen && fitToSize
                ? this
                : isFullScreen
                ? PicassoPalette.with(url, destination).use(MUTED_DARK).intoBackground(itemView)
                : PicassoPalette.with(url, destination).use(MUTED_DARK).intoBackground(thumbnailView);

        creator.into(destination, callBack);
    }

    @Override
    public int getThumbnailId() {
        return R.id.image_thumbnail;
    }

    @Override
    public void onSuccess() {
        fullResView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {

    }
}
