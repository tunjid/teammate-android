package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

/**
 * Viewholder for a {@link Team}
 */
public abstract class AdViewHolder extends ModelCardViewHolder<Ad, BaseRecyclerViewAdapter.AdapterListener> {

    AdViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
    }

    @Override
    public void bind(Ad model) {
        super.bind(model);
    }

}
