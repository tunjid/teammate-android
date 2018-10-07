package com.mainstreetcode.teammate.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.model.Team;

/**
 * Viewholder for a {@link Team}
 */
public class TeamViewHolder extends ModelCardViewHolder<Team, TeamAdapter.AdapterListener>
        implements View.OnClickListener {

    public TeamViewHolder(View itemView, TeamAdapter.AdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Team model) {
        super.bind(model);
        if (model.isEmpty()) return;

        setTitle(model.getSportAndName());
        setSubTitle(model.getCity());
    }

    @Override
    public void onClick(View view) {
        adapterListener.onTeamClicked(model);
    }
}
