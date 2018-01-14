package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.JoinRequestViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ModelCardViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleViewHolder;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Model;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Team}
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<ModelCardViewHolder, TeamDetailAdapter.UserAdapterListener> {

    private final int AD = 0;

    private final List<? extends Model> teamModels;

    public TeamDetailAdapter(List<? extends Model> teamModels, UserAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.teamModels = teamModels;
    }

    @Override
    public ModelCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return viewType == AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_content_ad, viewGroup), adapterListener)
                : viewType == R.id.viewholder_role
                ? new RoleViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener)
                : new JoinRequestViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(ModelCardViewHolder baseTeamViewHolder, int position) {
        Model model = teamModels.get(position);

        if (model instanceof Ad) ((ContentAdViewHolder) baseTeamViewHolder).bind((Ad) model);
        else if (model instanceof Role) ((RoleViewHolder) baseTeamViewHolder).bind((Role) model);
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
        Model model = teamModels.get(position);
        return model instanceof Ad ? AD : model instanceof Role ? R.id.viewholder_role : R.id.viewholder_join_request;
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onRoleClicked(Role role);

        void onJoinRequestClicked(JoinRequest request);
    }

}
