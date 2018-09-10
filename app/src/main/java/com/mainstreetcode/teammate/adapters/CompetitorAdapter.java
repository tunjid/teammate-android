package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.CompetitorViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Event}
 */

public class CompetitorAdapter extends BaseRecyclerViewAdapter<CompetitorViewHolder, CompetitorAdapter.AdapterListener> {

    private final List<Competitor> items;

    public CompetitorAdapter(List<Competitor> items, CompetitorAdapter.AdapterListener listener) {
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
       viewHolder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getEntity().hashCode();
    }

    public interface AdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onDragStarted(RecyclerView.ViewHolder viewHolder);
    }
}
