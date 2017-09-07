package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.squareup.picasso.Picasso;


public class ImageMediaViewHolder extends MediaViewHolder {

    private ImageView thumbnail;

    public ImageMediaViewHolder(View itemView, MediaAdapter.MediaAdapterListener adapterListener) {
        super(itemView, adapterListener);
        thumbnail = itemView.findViewById(R.id.image_thumbnail);

        if (adapterListener != null) {
            thumbnail.setOnClickListener(view -> adapterListener.onMediaClicked(media));
        }
    }

    @Override
    public void bind(Media media) {
        super.bind(media);

        String imageUrl = media.getImageUrl();

        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(itemView.getContext())
                    .load(imageUrl)
                    .fit()
                    .centerInside()
                    .into(thumbnail);
        }
    }

    @Override
    public int getThumbnailId() {
        return R.id.image_thumbnail;
    }
}
