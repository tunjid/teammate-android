package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Model;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.THUMBNAIL_SIZE;


public class ModelCardViewHolder<H extends Model, T extends BaseRecyclerViewAdapter.AdapterListener> extends BaseViewHolder<T> {

    protected H model;

    TextView title;
    TextView subtitle;
    public ImageView thumbnail;

    ModelCardViewHolder(View itemView, T adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        subtitle = itemView.findViewById(R.id.item_subtitle);
        thumbnail = itemView.findViewById(R.id.thumbnail);

        if (isThumbnail()) thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    public void bind(H model) {
        this.model = model;
        String imageUrl = model.getImageUrl();

        if (TextUtils.isEmpty(imageUrl)) {
            thumbnail.setImageResource(R.color.dark_grey);
            return;
        }

        RequestCreator creator = Picasso.with(itemView.getContext()).load(imageUrl);

        if (isThumbnail()) creator.resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE).centerInside();
        else creator.fit().centerCrop();

        creator.into(thumbnail);
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }

    public boolean isThumbnail() {return false;}
}
