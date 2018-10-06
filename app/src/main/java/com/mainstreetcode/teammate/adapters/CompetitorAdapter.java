package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.model.Item.COMPETITOR;

/**
 * Adapter for {@link Team}
 */

public class CompetitorAdapter extends BaseRecyclerViewAdapter<CompetitorViewHolder, CompetitorAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public CompetitorAdapter(List<Identifiable> items, AdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public CompetitorViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new CompetitorViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_competitor, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull CompetitorViewHolder viewHolder, int position) {
        Identifiable identifiable = items.get(position);
        if (identifiable instanceof Competitor) viewHolder.bind((Competitor) identifiable);
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
        return COMPETITOR;
    }

    public interface AdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onCompetitorClicked(Competitor competitor);
    }
}