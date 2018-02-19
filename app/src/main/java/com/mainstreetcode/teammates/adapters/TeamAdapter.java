package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammates.model.Ad;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.INSTALL_AD;
import static com.mainstreetcode.teammates.util.ViewHolderUtil.TEAM;

/**
 * Adapter for {@link Team}
 */

public class TeamAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, TeamAdapter.TeamAdapterListener> {

    private final List<Identifiable> items;

    public TeamAdapter(List<Identifiable> items, TeamAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : new TeamViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof Role) ((TeamViewHolder) viewHolder).bind(((Role) item).getTeam());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable item = items.get(position);
        return item instanceof Team ? TEAM : item instanceof Role ? TEAM : ((Ad) item).getType();
    }

    public interface TeamAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onTeamClicked(Team item);
    }

}
