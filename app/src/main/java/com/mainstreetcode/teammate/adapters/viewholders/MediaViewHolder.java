package com.mainstreetcode.teammate.adapters.viewholders;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import androidx.arch.core.util.Function;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.florent37.picassopalette.PicassoPalette;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.MediaAdapter;
import com.mainstreetcode.teammate.model.Media;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import static androidx.core.view.ViewCompat.setTransitionName;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.github.florent37.picassopalette.PicassoPalette.Profile.MUTED_DARK;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getLayoutParams;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.getTransitionName;


public abstract class MediaViewHolder<T extends View> extends InteractiveViewHolder<MediaAdapter.MediaAdapterListener>
        implements Callback {

    private static final String UNITY_ASPECT_RATIO = "1";
    private static final String SCALE_X_PROPERTY = "scaleX";
    private static final String SCALE_Y_PROPERTY = "scaleY";
    private static final float FULL_SCALE = 1F;
    private static final float FOUR_FIFTH_SCALE = 0.8F;
    private static final int DURATION = 200;

    public Media media;

    T fullResView;
    private View border;
    private ConstraintLayout container;
    public ImageView thumbnailView;

    MediaViewHolder(View itemView, @NonNull MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        border = itemView.findViewById(R.id.border);
        container = itemView.findViewById(R.id.container);
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
        else {
            getLayoutParams(container).height = MATCH_PARENT;
            ViewGroup.MarginLayoutParams params = getLayoutParams(itemView);
            params.height = MATCH_PARENT;
            params.leftMargin = params.topMargin = params.rightMargin = params.bottomMargin = 0;
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
        if (!adapterListener.isFullScreen()) highlightViewHolder(adapterListener::isSelected);
    }

    public void fullBind(Media media) {
        bind(media);
    }

    public void unBind() {}

    public boolean performLongClick() {
        highlightViewHolder(adapterListener::onMediaLongClicked);
        return true;
    }

    void loadImage(String url, boolean fitToSize, ImageView destination) {
        boolean isFullScreen = adapterListener.isFullScreen();
        if (TextUtils.isEmpty(url)) return;

        RequestCreator creator = Picasso.get().load(url);

        if (!isFullScreen) creator.placeholder(R.drawable.bg_image_placeholder);

        creator = fitToSize ? creator.fit() : creator.resize(ViewHolderUtil.THUMBNAIL_SIZE, ViewHolderUtil.THUMBNAIL_SIZE);
        creator = creator.centerInside();

        Callback callBack = isFullScreen && fitToSize
                ? this
                : isFullScreen
                ? PicassoPalette.with(url, destination).use(MUTED_DARK).intoBackground(itemView)
                : PicassoPalette.with(url, destination).use(MUTED_DARK).intoBackground(thumbnailView);

        creator.into(destination, callBack);
    }

    private void setUnityAspectRatio() {
        ConstraintSet set = new ConstraintSet();

        set.clone(container);
        set.setDimensionRatio(thumbnailView.getId(), UNITY_ASPECT_RATIO);
        set.setDimensionRatio(fullResView.getId(), UNITY_ASPECT_RATIO);
        set.applyTo(container);
    }

    private void highlightViewHolder(Function<Media, Boolean> selectionFunction) {
        boolean isSelected = selectionFunction.apply(media);
        border.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        scale(isSelected);
    }

    private void scale(boolean isSelected) {
        float end = isSelected ? FOUR_FIFTH_SCALE : FULL_SCALE;

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleDownX = animateProperty(SCALE_X_PROPERTY, container.getScaleX(), end);
        ObjectAnimator scaleDownY = animateProperty(SCALE_Y_PROPERTY, container.getScaleY(), end);

        set.playTogether(scaleDownX, scaleDownY);
        set.start();
    }

    @NonNull
    private ObjectAnimator animateProperty(String property, float start, float end) {
        return ObjectAnimator.ofFloat(container, property, start, end).setDuration(DURATION);
    }

    @Override
    public void onSuccess() {}

    @Override
    public void onError(Exception e) {}
}
