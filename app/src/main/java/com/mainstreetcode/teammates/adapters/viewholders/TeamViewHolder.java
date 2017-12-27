package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;

import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.model.Team;

/**
 * Viewholder for a {@link Team}
 */
public class TeamViewHolder extends ModelCardViewHolder<Team, TeamAdapter.TeamAdapterListener>
        implements View.OnClickListener {

    public TeamViewHolder(View itemView, TeamAdapter.TeamAdapterListener adapterListener) {
        super(itemView, adapterListener);
        itemView.setOnClickListener(this);
    }

    public void bind(Team model) {
        super.bind(model);
        if (model.isEmpty()) return;

        title.setText(model.getName());
        subtitle.setText(model.getCity());
    }

    @Override
    public void onClick(View view) {
        adapterListener.onTeamClicked(model);
    }
}
