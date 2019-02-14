package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.BlockedUserViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Team;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.BLOCKED_USER;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;

/**
 * Adapter for {@link Team}
 */

public class BlockedUserAdapter extends InteractiveAdapter<InteractiveViewHolder, BlockedUserAdapter.UserAdapterListener> {

    private final List<Differentiable> teamModels;

    public BlockedUserAdapter(List<Differentiable> items, UserAdapterListener listener) {
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
        Differentiable item = teamModels.get(position);

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
        Differentiable item = teamModels.get(position);
        if (item instanceof Ad) return ((Ad) item).getType();
        return BLOCKED_USER;
    }

    public interface UserAdapterListener extends InteractiveAdapter.AdapterListener {
        void onBlockedUserClicked(BlockedUser blockedUser);
    }
}
