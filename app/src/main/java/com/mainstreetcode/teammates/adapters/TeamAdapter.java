package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammates.model.ContentAd;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.CONTENT_AD;
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
                ? new ContentAdViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_content_ad, viewGroup), adapterListener)
                : new TeamViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof ContentAd) ((ContentAdViewHolder) viewHolder).bind((ContentAd) item);
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
        return items.get(position) instanceof Team ? TEAM : CONTENT_AD;
    }

    public interface TeamAdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onTeamClicked(Team item);
    }

}
