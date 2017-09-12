package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.github.florent37.picassopalette.PicassoPalette;
import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.MediaAdapter;
import com.mainstreetcode.teammates.model.Media;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;


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

        displayImage(media.getThumbnail(), false);
    }

    @Override
    public void fullBind(Media media) {
        super.bind(media);

        displayImage(media.getUrl(), true);
    }

    private void displayImage(String url, boolean includeBackground) {

        if (TextUtils.isEmpty(url)) return;

        RequestCreator requestCreator = Picasso.with(itemView.getContext())
                .load(url)
                .fit()
                .centerInside();

        PicassoPalette callBack = PicassoPalette.with(url, thumbnail)
                .use(PicassoPalette.Profile.MUTED_DARK)
                .intoBackground(thumbnail);

        if (includeBackground) callBack.intoBackground(itemView);

        requestCreator.into(thumbnail, callBack);
    }

    @Override
    public int getThumbnailId() {
        return R.id.image_thumbnail;
    }
}
