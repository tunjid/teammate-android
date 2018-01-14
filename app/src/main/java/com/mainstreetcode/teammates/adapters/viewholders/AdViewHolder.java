package com.mainstreetcode.teammates.adapters.viewholders;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.ads.formats.NativeAd;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

/**
 * Viewholder for a {@link Team}
 */
abstract class AdViewHolder<T extends NativeAd> extends ModelCardViewHolder<Ad<T>, BaseRecyclerViewAdapter.AdapterListener> {

    AdViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
    }

    @Override
    public void bind(Ad<T> model) {
        this.model = model;
        setImageAspectRatio(getImageAspectRatio(model));

        Drawable drawable = model.getDrawable();
        if (drawable != null) thumbnail.setImageDrawable(drawable);
    }

    @NonNull
    @Override
    String getImageAspectRatio(Ad<T> model) {
        String aspectRatio = model.getImageAspectRatio();
        return aspectRatio == null ? super.getImageAspectRatio(model) : aspectRatio;
    }
}
