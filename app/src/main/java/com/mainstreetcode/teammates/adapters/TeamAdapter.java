package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.model.Team;
import com.squareup.picasso.Picasso;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamAdapter extends BaseRecyclerViewAdapter<TeamAdapter.TeamViewHolder, TeamAdapter.TeamAdapterListener> {

    private final List<Team> items;

    public TeamAdapter(List<Team> items, TeamAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public TeamViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_team_search, viewGroup, false);
        return new TeamViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(TeamViewHolder teamViewHolder, int i) {
        teamViewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public interface TeamAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onTeamClicked(Team item);
    }

    static class TeamViewHolder extends BaseViewHolder<TeamAdapterListener>
            implements View.OnClickListener {

        private Team item;
        private ImageView teamLogo;
        private TextView teamName;
        private TextView teamLocation;

        TeamViewHolder(View itemView, TeamAdapterListener adapterListener) {
            super(itemView, adapterListener);
            teamLogo = itemView.findViewById(R.id.thumbnail);
            teamName = itemView.findViewById(R.id.team_name);
            teamLocation = itemView.findViewById(R.id.team_location);
            itemView.setOnClickListener(this);
        }

        void bind(Team item) {
            this.item = item;
            teamName.setText(item.getName());
            teamLocation.setText(item.getCity());

            Picasso.with(itemView.getContext())
                    .load(item.getLogoUrl())
                    .fit()
                    .centerInside()
                    .into(teamLogo);
        }

        @Override
        public void onClick(View view) {
            adapterListener.onTeamClicked(item);
        }
    }
}
