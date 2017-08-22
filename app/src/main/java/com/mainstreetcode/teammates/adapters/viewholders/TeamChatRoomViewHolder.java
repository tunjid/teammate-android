package com.mainstreetcode.teammates.adapters.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamChatRoomAdapter;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.model.TeamChatRoom;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

/**
 * Viewholder for a {@link Team}
 */
public class TeamChatRoomViewHolder extends BaseViewHolder<TeamChatRoomAdapter.TeamChatRoomAdapterListener>
        implements View.OnClickListener {

    private TeamChatRoom item;
    private ImageView teamLogo;
    private TextView teamName;
    private TextView teamLocation;

    public TeamChatRoomViewHolder(View itemView, TeamChatRoomAdapter.TeamChatRoomAdapterListener adapterListener) {
        super(itemView, adapterListener);
        teamLogo = itemView.findViewById(R.id.thumbnail);
        teamName = itemView.findViewById(R.id.item_title);
        teamLocation = itemView.findViewById(R.id.item_subtitle);
        itemView.setOnClickListener(this);
    }

    public void bind(TeamChatRoom item) {
        if (item.isEmpty()) return;
        
        this.item = item;
        Team team = item.getTeam();
        teamName.setText(team.getName());
        teamLocation.setText(team.getCity());

        Picasso.with(itemView.getContext())
                .load(item.getImageUrl())
                .fit()
                .centerInside()
                .into(teamLogo);
    }

    @Override
    public void onClick(View view) {
        adapterListener.onChatRoomClicked(item);
    }
}
