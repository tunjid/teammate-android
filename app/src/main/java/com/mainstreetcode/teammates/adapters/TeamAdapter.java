package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 * <p>
 * Created by Shemanigans on 6/3/17.
 */

public class TeamAdapter extends BaseRecyclerViewAdapter<TeamViewHolder, TeamAdapter.TeamAdapterListener> {

    private final List<Team> items;

    public TeamAdapter(List<Team> items, TeamAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public TeamViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_grid_item, viewGroup, false);
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

}
