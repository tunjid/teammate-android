package com.mainstreetcode.teammates.adapters.viewholders;

import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
abstract class AdViewHolder<T extends Ad> extends BaseViewHolder<BaseRecyclerViewAdapter.AdapterListener> {

    TextView title;
    TextView subtitle;
    ImageView thumbnail;

    AdViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        subtitle = itemView.findViewById(R.id.item_subtitle);
        thumbnail = itemView.findViewById(R.id.thumbnail);
    }

    public void bind(T ad) {
        setImageAspectRatio(ad);

        Drawable drawable = ad.getDrawable();
        if (drawable != null) thumbnail.setImageDrawable(drawable);
    }

    private void setImageAspectRatio(Ad ad) {
        String aspectRatio = ad.getImageAspectRatio();
        if (aspectRatio != null) {
            ((ConstraintLayout.LayoutParams) thumbnail.getLayoutParams()).dimensionRatio = aspectRatio;
        }
    }
}
