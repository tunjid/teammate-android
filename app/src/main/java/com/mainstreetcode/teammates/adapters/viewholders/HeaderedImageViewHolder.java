package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.HeaderedModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.io.File;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.FULL_RES_LOAD_DELAY;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.THUMBNAIL_SIZE;

public class HeaderedImageViewHolder extends BaseViewHolder<ImageWorkerFragment.ImagePickerListener>
        implements View.OnClickListener {

    private ImageView picture;
    private HeaderedModel model;

    public HeaderedImageViewHolder(View itemView, ImageWorkerFragment.ImagePickerListener listener) {
        super(itemView, listener);
        picture = itemView.findViewById(R.id.image);
        picture.setOnClickListener(this);
    }

    public void bind(HeaderedModel model) {
        this.model = model;
        RequestCreator creator = getCreator();

        if (creator == null) return;

        creator.resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(picture);

        picture.postDelayed(() -> {
                    RequestCreator delayed = getCreator();
                    if (delayed != null) delayed.fit().centerCrop().into(picture);
                }, FULL_RES_LOAD_DELAY);

    }

    @Override
    public void onClick(View view) {
        adapterListener.onImageClick();
    }

    public ImageView getPicture() {
        return picture;
    }

    @Nullable
    private RequestCreator getCreator() {
        if (model == null) return null;
        String pathOrUrl = model.getHeaderItem().getValue();

        if (TextUtils.isEmpty(pathOrUrl)) return null;

        File file = new File(pathOrUrl);
        Picasso picasso = Picasso.with(itemView.getContext());
        return file.exists() ? picasso.load(file) : picasso.load(pathOrUrl);
    }
}
