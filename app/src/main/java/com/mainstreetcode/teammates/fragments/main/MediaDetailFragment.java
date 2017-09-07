package com.mainstreetcode.teammates.fragments.main;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.transition.Fade;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ImageMediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MediaViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.VideoMediaViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Media;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Arrays;

import jp.wasabeef.picasso.transformations.BlurTransformation;
import jp.wasabeef.picasso.transformations.ColorFilterTransformation;

import static android.support.constraint.ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
import static android.support.constraint.ConstraintLayout.LayoutParams.PARENT_ID;


public class MediaDetailFragment extends MainActivityFragment {

    public static final String ARG_MEDIA = "media";

    public static MediaDetailFragment newInstance(Media media) {
        MediaDetailFragment fragment = new MediaDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_MEDIA, media);
        fragment.setArguments(args);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition baseTransition = new Fade();
            Transition baseSharedTransition = getTransition();

            baseTransition.excludeTarget(R.id.image, true);

            fragment.setEnterTransition(baseTransition);
            fragment.setExitTransition(baseTransition);
            fragment.setSharedElementEnterTransition(baseSharedTransition);
            fragment.setSharedElementReturnTransition(baseSharedTransition);
        }
        return fragment;
    }

    @Override
    public String getStableTag() {
        return super.getStableTag() + "-" + getArguments().getParcelable(ARG_MEDIA);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.fragment_media_detail, container, false);
        ImageView blurredBackground = rootView.findViewById(R.id.blurred_background);

        Media media = getArguments().getParcelable(ARG_MEDIA);

        if (media == null) return rootView;

        boolean isImage = media.isImage();
        bindViewHolder(inflater, rootView, media, isImage);

        if (isImage) {
            Picasso.with(inflater.getContext())
                    .load(media.getImageUrl())
                    .fit()
                    .centerCrop()
                    .transform(Arrays.asList(new Transformation[]{
                            new BlurTransformation(inflater.getContext(), 20),
                            new ColorFilterTransformation(Color.parseColor("#C8000000"))
                    }))
                    .into(blurredBackground);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toggleToolbar(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        toggleToolbar(true);
    }

    private void bindViewHolder(LayoutInflater inflater, ConstraintLayout rootView, Media media, boolean isImage) {
        int resource = isImage ? R.layout.viewholder_image : R.layout.viewholder_video;

        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(MATCH_CONSTRAINT, MATCH_CONSTRAINT);
        params.leftToLeft = PARENT_ID;
        params.topToTop = PARENT_ID;
        params.rightToRight = PARENT_ID;
        params.bottomToBottom = PARENT_ID;

        View itemView = inflater.inflate(resource, rootView, false);
        rootView.addView(itemView, params);

        MediaViewHolder mediaViewHolder = isImage
                ? new ImageMediaViewHolder(itemView, null)
                : new VideoMediaViewHolder(itemView, null);

        mediaViewHolder.bind(media);
    }
}
