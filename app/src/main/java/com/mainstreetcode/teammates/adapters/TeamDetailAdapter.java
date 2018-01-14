package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.JoinRequestViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.RoleViewHolder;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.JoinRequest;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.JOIN_REQUEST;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.ROLE;

/**
 * Adapter for {@link Team}
 */

public class TeamDetailAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, TeamDetailAdapter.UserAdapterListener> {

    private final List<Identifiable> teamModels;

    public TeamDetailAdapter(List<Identifiable> teamModels, UserAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.teamModels = teamModels;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return viewType == AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_content_ad, viewGroup), adapterListener)
                : viewType == ROLE
                ? new RoleViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener)
                : new JoinRequestViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable item = teamModels.get(position);

        if (item instanceof Ad) ((ContentAdViewHolder) viewHolder).bind((Ad) item);
        else if (item instanceof Role) ((RoleViewHolder) viewHolder).bind((Role) item);
        else ((JoinRequestViewHolder) viewHolder).bind((JoinRequest) item);
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
        Identifiable item = teamModels.get(position);
        return item instanceof Ad ? AD : item instanceof Role ? ROLE : JOIN_REQUEST;
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onRoleClicked(Role role);

        void onJoinRequestClicked(JoinRequest request);
    }

}
