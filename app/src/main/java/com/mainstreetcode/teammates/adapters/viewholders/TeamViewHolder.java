package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class TeamViewHolder extends BaseViewHolder<TeamAdapter.TeamAdapterListener>
        implements View.OnClickListener {

    private Team item;
    private ImageView teamLogo;
    private TextView teamName;
    private TextView teamLocation;

    public TeamViewHolder(View itemView, TeamAdapter.TeamAdapterListener adapterListener) {
        super(itemView, adapterListener);
        teamLogo = itemView.findViewById(R.id.thumbnail);
        teamName = itemView.findViewById(R.id.team_name);
        teamLocation = itemView.findViewById(R.id.team_location);
        itemView.setOnClickListener(this);
    }

    public void bind(Team item) {
        if (item.isEmpty()) return;
        
        this.item = item;
        teamName.setText(item.getName());
        teamLocation.setText(item.getCity());

        Picasso.with(itemView.getContext())
                .load(item.getImageUrl())
                .fit()
                .centerInside()
                .into(teamLogo);
    }

    @Override
    public void onClick(View view) {
        adapterListener.onTeamClicked(item);
    }
}
