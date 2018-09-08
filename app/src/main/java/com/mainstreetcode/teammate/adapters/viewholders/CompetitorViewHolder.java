package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

/**
 * Viewholder for a {@link Team}
 */
public class CompetitorViewHolder extends ModelCardViewHolder<Competitor, BaseRecyclerViewAdapter.AdapterListener>{

    private View dragHandle;

    public CompetitorViewHolder(View itemView, BaseRecyclerViewAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        dragHandle = itemView.findViewById(R.id.drag_handle);
    }

    public void bind(Competitor model) {
        super.bind(model);
        title.setText(model.getName());
        dragHandle.setVisibility(model.isEmpty() ? View.VISIBLE : View.GONE);
        //subtitle.setText(model.getCity());
    }
}
