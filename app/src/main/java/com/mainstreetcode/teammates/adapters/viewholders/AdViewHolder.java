package com.mainstreetcode.teammates.adapters.viewholders;

import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public abstract class AdViewHolder<T extends Ad> extends BaseViewHolder<BaseRecyclerViewAdapter.AdapterListener> {

    T ad;

    TextView title;
    TextView subtitle;
    ImageView thumbnail;

    AdViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        title = itemView.findViewById(R.id.item_title);
        subtitle = itemView.findViewById(R.id.item_subtitle);
        thumbnail = itemView.findViewById(R.id.thumbnail);
    }

    @Nullable
    abstract String getImageUrl();

    public void bind(T ad) {
        this.ad = ad;
        setImageAspectRatio(ad);

        thumbnail.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                thumbnail.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                String imageUrl = getImageUrl();

                if (TextUtils.isEmpty(imageUrl)) return;
                Picasso.with(itemView.getContext())
                        .load(imageUrl)
                        .fit()
                        .centerCrop()
                        .into(thumbnail);
            }
        });
    }

    private void setImageAspectRatio(Ad ad) {
        String aspectRatio = ad.getImageAspectRatio();
        if (aspectRatio != null) {
            ((ConstraintLayout.LayoutParams) thumbnail.getLayoutParams()).dimensionRatio = aspectRatio;
        }
    }
}
