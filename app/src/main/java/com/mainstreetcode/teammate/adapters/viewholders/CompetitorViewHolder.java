package com.mainstreetcode.teammate.adapters.viewholders;

import android.annotation.SuppressLint;
import android.view.View;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.CompetitorAdapter;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Team;

/**
 * Viewholder for a {@link Team}
 */
public class CompetitorViewHolder extends ModelCardViewHolder<Competitor, CompetitorAdapter.AdapterListener> {

    @SuppressLint("ClickableViewAccessibility")
    public CompetitorViewHolder(View itemView, CompetitorAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(view -> adapterListener.onCompetitorClicked(model));
    }

    public void bind(Competitor model) {
        super.bind(model);
        if (model.isEmpty()) model.setSeed(getAdapterPosition());

        setTitle(model.getName());
        setSubTitle(model.getSeedText());
    }

    public CompetitorViewHolder hideSubtitle() {
        subtitle.setVisibility(View.INVISIBLE);
        return this;
    }

    public View getDragHandle() { return itemView.findViewById(R.id.drag_handle); }
}
