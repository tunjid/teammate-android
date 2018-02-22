package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
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
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static android.support.v4.view.ViewCompat.setTransitionName;
import static com.github.florent37.picassopalette.PicassoPalette.Profile.MUTED_DARK;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.getTransitionName;


public abstract class MediaViewHolder<T extends View> extends BaseViewHolder<MediaAdapter.MediaAdapterListener>
        implements Callback {

    private static final int THUMBNAIL_SIZE = 200;
    private static final String UNITY_ASPECT_RATIO = "1";

    public Media media;

    T fullResView;
    private View border;
    public ImageView thumbnailView;

    MediaViewHolder(View itemView, @NonNull MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        border = itemView.findViewById(R.id.border);
        fullResView = itemView.findViewById(getFullViewId());
        thumbnailView = itemView.findViewById(getThumbnailId());

        View.OnClickListener clickListener = view -> adapterListener.onMediaClicked(media);

        itemView.setOnClickListener(clickListener);
        fullResView.setOnClickListener(clickListener);

        if (!adapterListener.isFullScreen()) {
            itemView.setOnLongClickListener(view -> performLongClick());
            thumbnailView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            setUnityAspectRatio();
        }
    }

    @IdRes
    public abstract int getThumbnailId();

    @IdRes
    public abstract int getFullViewId();

    public void bind(Media media) {
        this.media = media;
        setTransitionName(itemView, getTransitionName(media, R.id.fragment_media_background));
        setTransitionName(thumbnailView, getTransitionName(media, R.id.fragment_media_thumbnail));

        loadImage(media.getThumbnail(), false, thumbnailView);
        if (!adapterListener.isFullScreen())
            border.setVisibility(adapterListener.isSelected(media) ? View.VISIBLE : View.GONE);
    }

    public void fullBind(Media media) {
        bind(media);
    }

    public void unBind() {}

    public boolean performLongClick() {
        border.setVisibility(adapterListener.onMediaLongClicked(media) ? View.VISIBLE : View.GONE);
        return true;
    }

    void loadImage(String url, boolean fitToSize, ImageView destination) {
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

    private void setUnityAspectRatio() {
        ConstraintLayout constraintLayout = (ConstraintLayout) itemView;
        ConstraintSet set = new ConstraintSet();

        set.clone(constraintLayout);
        set.setDimensionRatio(thumbnailView.getId(), UNITY_ASPECT_RATIO);
        set.setDimensionRatio(fullResView.getId(), UNITY_ASPECT_RATIO);
        set.applyTo(constraintLayout);
    }

    @Override
    public void onSuccess() {}

    @Override
    public void onError() {}
}
