package com.mainstreetcode.teammates.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.JoinRequestViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<ModelCardViewHolder, TeamDetailAdapter.UserAdapterListener> {
    private final List<? extends Model> teamModels;

    public TeamDetailAdapter(List<? extends Model> teamModels, UserAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.teamModels = teamModels;
    }

    @Override
    public ModelCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        @LayoutRes int layoutRes = R.layout.viewholder_grid_item;
        View itemView = LayoutInflater.from(context).inflate(layoutRes, viewGroup, false);

        return viewType == R.id.viewholder_role
                ? new RoleViewHolder(itemView, adapterListener)
                : new JoinRequestViewHolder(itemView, adapterListener);
    }

    @Override
    public void onBindViewHolder(ModelCardViewHolder baseTeamViewHolder, int position) {
        Model model = teamModels.get(position);

        if (model instanceof Role) ((RoleViewHolder) baseTeamViewHolder).bind((Role) model);
        else ((JoinRequestViewHolder) baseTeamViewHolder).bind((JoinRequest) model);
    }

    @Override
    public long getItemId(int position) {
        return teamModels.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return teamModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return teamModels.get(position) instanceof Role ? R.id.viewholder_role : R.id.viewholder_join_request;
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onRoleClicked(Role role);

        void onJoinRequestClicked(JoinRequest request);
    }

}
