package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.github.florent37.picassopalette.PicassoPalette;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.fragments.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Item;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;

/**
 * Viewholder for {@link Item}.
 */
public class ImageViewHolder extends BaseItemViewHolder
        implements View.OnClickListener {

    private ImageView picture;

    public ImageViewHolder(View itemView, ImageWorkerFragment.ImagePickerListener listener) {
        super(itemView, listener);
        picture = itemView.findViewById(R.id.image);
        picture.setOnClickListener(this);
    }

    @Override
    public void bind(Item item) {
        String pathOrUrl = item.getValue();

        if (!TextUtils.isEmpty(pathOrUrl)) {
            File file = new File(pathOrUrl);
            Picasso picasso = Picasso.with(itemView.getContext());
            RequestCreator creator = file.exists() ? picasso.load(file) : picasso.load(pathOrUrl);

            creator.fit().centerCrop().into(picture,
                    PicassoPalette.with(pathOrUrl, picture)
                            .use(PicassoPalette.Profile.MUTED_DARK)
                            .intoBackground(itemView));
        }
    }

    @Override
    public void onClick(View view) {
        adapterListener.onImageClick();
    }
}
