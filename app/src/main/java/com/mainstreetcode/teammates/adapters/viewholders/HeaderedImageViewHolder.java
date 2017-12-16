package com.mainstreetcode.teammates.adapters.viewholders;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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

public class HeaderedImageViewHolder extends BaseViewHolder<ImageWorkerFragment.ImagePickerListener>
        implements View.OnClickListener {

    private ImageView picture;

    public HeaderedImageViewHolder(View itemView, ImageWorkerFragment.ImagePickerListener listener) {
        super(itemView, listener);
        picture = itemView.findViewById(R.id.image);
        picture.setOnClickListener(this);

        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);

        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        picture.setColorFilter(filter);
    }

    public void bind(HeaderedModel model) {
        String pathOrUrl = model.getHeaderItem().getValue();

        if (!TextUtils.isEmpty(pathOrUrl)) {
            File file = new File(pathOrUrl);
            Picasso picasso = Picasso.with(itemView.getContext());
            RequestCreator creator = file.exists() ? picasso.load(file) : picasso.load(pathOrUrl);

            creator.fit().centerCrop().noFade().into(picture);
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onImageClick();
    }

    public ImageView getPicture() {
        return picture;
    }
}
