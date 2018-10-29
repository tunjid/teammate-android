package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.BlockedUserViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.BLOCKED_USER;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;

/**
 * Adapter for {@link Team}
 */

public class BlockedUserAdapter extends InteractiveAdapter<InteractiveViewHolder, BlockedUserAdapter.UserAdapterListener> {

    private final List<Identifiable> teamModels;

    public BlockedUserAdapter(List<Identifiable> items, UserAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.teamModels = items;
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : new BlockedUserViewHolder(getItemView(R.layout.viewholder_grid_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int position) {
        Identifiable item = teamModels.get(position);

        if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
        else if (item instanceof BlockedUser) ((BlockedUserViewHolder) viewHolder).bind((BlockedUser) item);
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
        return BLOCKED_USER;
    }

    public interface UserAdapterListener extends InteractiveAdapter.AdapterListener {
        void onBlockedUserClicked(BlockedUser blockedUser);
    }
}
