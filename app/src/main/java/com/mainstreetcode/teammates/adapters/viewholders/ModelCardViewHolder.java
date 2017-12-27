package com.mainstreetcode.teammates.adapters.viewholders;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Model;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;


public class ModelCardViewHolder<H extends Model<H>, T extends BaseRecyclerViewAdapter.AdapterListener> extends BaseViewHolder<T> {

    protected H model;

    TextView title;
    TextView subtitle;
    ImageView thumbnail;

    ModelCardViewHolder(View itemView, T adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        subtitle = itemView.findViewById(R.id.item_subtitle);
        thumbnail = itemView.findViewById(R.id.thumbnail);
    }

    public void bind(H model) {
        this.model = model;

        String imageUrl = model.getImageUrl();

        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(itemView.getContext())
                    .load(imageUrl)
                    .fit()
                    .centerCrop()
                    .into(thumbnail);
        }
    }

    public ImageView getThumbnail() {
        return thumbnail;
    }
}
