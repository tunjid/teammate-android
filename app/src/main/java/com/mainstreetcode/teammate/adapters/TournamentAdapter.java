package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.TournamentViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.Event;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Tournament;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.TOURNAMENT;

/**
 * Adapter for {@link Event}
 */

public class TournamentAdapter extends InteractiveAdapter<InteractiveViewHolder, TournamentAdapter.TournamentAdapterListener> {

    private final List<Differentiable> items;

    public TournamentAdapter(List<Differentiable> items, TournamentAdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public InteractiveViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return viewType == CONTENT_AD
                ? new ContentAdViewHolder(getItemView(R.layout.viewholder_grid_content_ad, viewGroup), adapterListener)
                : viewType == INSTALL_AD
                ? new InstallAdViewHolder(getItemView(R.layout.viewholder_grid_install_ad, viewGroup), adapterListener)
                : new TournamentViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int position) {
        Differentiable item = items.get(position);
        if (item instanceof Tournament) ((TournamentViewHolder) viewHolder).bind((Tournament) item);
        else if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
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
        Differentiable item = items.get(position);
        return item instanceof Tournament ? TOURNAMENT : ((Ad) item).getType();
    }

    public interface TournamentAdapterListener extends InteractiveAdapter.AdapterListener {
        void onTournamentClicked(Tournament item);
    }

}
