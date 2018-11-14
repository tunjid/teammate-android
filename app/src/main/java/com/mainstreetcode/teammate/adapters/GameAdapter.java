package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.AdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.ContentAdViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.GameViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InstallAdViewHolder;
import com.mainstreetcode.teammate.model.Ad;
import com.mainstreetcode.teammate.model.Game;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.CONTENT_AD;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.GAME;
import static com.mainstreetcode.teammate.util.ViewHolderUtil.INSTALL_AD;

/**
 * Adapter for {@link Team}
 */

public class GameAdapter extends InteractiveAdapter<InteractiveViewHolder, GameAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public GameAdapter(List<Identifiable> items, AdapterListener listener) {
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
                : new GameViewHolder(getItemView(R.layout.viewholder_game, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull InteractiveViewHolder viewHolder, int position) {
        Identifiable item = items.get(position);
        if (item instanceof Ad) ((AdViewHolder) viewHolder).bind((Ad) item);
        else if (item instanceof Game) ((GameViewHolder) viewHolder).bind((Game) item);
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
        return item instanceof Game ? GAME : ((Ad) item).getType();
    }

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void onGameClicked(Game game);
    }

}
