package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

/**
 * Viewholder for a {@link Team}
 */
public class CompetitorViewHolder extends ModelCardViewHolder<Competitor, BaseRecyclerViewAdapter.AdapterListener>{

    public CompetitorViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
    }

    public void bind(Competitor model) {
        super.bind(model);
        title.setText(model.getName());
        //subtitle.setText(model.getCity());
    }
}
