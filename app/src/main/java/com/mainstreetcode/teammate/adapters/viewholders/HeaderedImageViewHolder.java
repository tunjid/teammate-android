package com.mainstreetcode.teammate.adapters.viewholders;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.HeaderedModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.io.File;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.FULL_RES_LOAD_DELAY;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.THUMBNAIL_SIZE;

public class HeaderedImageViewHolder extends BaseViewHolder<ImageWorkerFragment.ImagePickerListener>
        implements View.OnClickListener {

    private ImageView fullRes;
    private ImageView thumbnail;
    private HeaderedModel model;

    public HeaderedImageViewHolder(View itemView, ImageWorkerFragment.ImagePickerListener listener) {
        super(itemView, listener);
        fullRes = itemView.findViewById(R.id.image_full_res);
        thumbnail = itemView.findViewById(R.id.image);
        thumbnail.setOnClickListener(this);
    }

    public void bind(HeaderedModel model) {
        this.model = model;
        RequestCreator creator = getCreator();
        if (creator == null) return;

        creator.resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside().into(thumbnail);
        fullRes.postDelayed(new DeferredImageLoader(), FULL_RES_LOAD_DELAY);
    }

    @Override
    public void onClick(View view) {adapterListener.onImageClick();}

    public ImageView getThumbnail() {return thumbnail;}

    @Nullable
    private RequestCreator getCreator() {
        if (model == null) return null;
        String pathOrUrl = model.getHeaderItem().getValue().toString();

        if (TextUtils.isEmpty(pathOrUrl)) return null;

        File file = new File(pathOrUrl);
        Picasso picasso = Picasso.with(itemView.getContext());
        return file.exists() ? picasso.load(file) : picasso.load(pathOrUrl);
    }

    private final class DeferredImageLoader implements Runnable, Callback {
        @Override
        public void run() {
            RequestCreator delayed = getCreator();
            if (delayed != null) delayed.fit().centerCrop().into(fullRes, this);
        }

        @Override
        public void onSuccess() {fullRes.setVisibility(View.VISIBLE);}

        @Override
        public void onError() {}
    }
}
