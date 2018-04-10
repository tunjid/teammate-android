package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.JoinRequestViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.RoleViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.JoinRequest;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.JOIN_REQUEST;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ROLE;

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

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : viewType == ROLE
                ? new RoleViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener)
                : new JoinRequestViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull BaseViewHolder viewHolder, int position) {
        Identifiable item = teamModels.get(position);

        if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
        else if (item instanceof Role) ((RoleViewHolder) viewHolder).bind((Role) item);
        else if (item instanceof JoinRequest)
            ((JoinRequestViewHolder) viewHolder).bind((JoinRequest) item);
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
        return item instanceof Ad ? ((Ad) item).getType() : item instanceof Role ? ROLE : JOIN_REQUEST;
    }

    public interface UserAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onRoleClicked(Role role);

        void onJoinRequestClicked(JoinRequest request);
    }

}
