package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
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
import com.mainstreetcode.teammate.model.Model;
import com.mainstreetcode.teammate.model.Role;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.model.TeamMember;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.JOIN_REQUEST;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.ROLE;

/**
 * Adapter for {@link Team}
 */

public class TeamMemberAdapter extends InteractiveAdapter<InteractiveViewHolder, TeamMemberAdapter.UserAdapterListener> {

    private final List<Identifiable> teamModels;

    public TeamMemberAdapter(List<Identifiable> teamModels, UserAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.teamModels = teamModels;
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : viewType == ROLE
                ? new RoleViewHolder(getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener)
                : new JoinRequestViewHolder(getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int position) {
        Identifiable item = teamModels.get(position);

        if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
        if (!(item instanceof TeamMember)) return;

        Model<?> wrapped = ((TeamMember) item).getWrappedModel();

        if (wrapped instanceof Role) ((RoleViewHolder) viewHolder).bind((Role) wrapped);
        else if (wrapped instanceof JoinRequest)
            ((JoinRequestViewHolder) viewHolder).bind((JoinRequest) wrapped);
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
        if (item instanceof Ad) return ((Ad) item).getType();
        if (!(item instanceof TeamMember)) return JOIN_REQUEST;

        Model<?> wrapped = ((TeamMember) item).getWrappedModel();
        return wrapped instanceof Role ? ROLE : JOIN_REQUEST;
    }

    public interface UserAdapterListener extends InteractiveAdapter.AdapterListener {
        void onRoleClicked(Role role);

        void onJoinRequestClicked(JoinRequest request);
    }

}
