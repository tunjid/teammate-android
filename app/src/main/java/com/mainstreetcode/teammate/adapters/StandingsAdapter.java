package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.StandingRowViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Row;
import com.mainstreetcode.teammate.util.SyncedScrollView;
import com.mainstreetcode.teammate.util.ViewHolderUtil;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

/**
 * Adapter for {@link Event}
 */

public class StandingsAdapter extends BaseRecyclerViewAdapter<StandingRowViewHolder, StandingsAdapter.AdapterListener> {

    private final List<Row> items;

    public StandingsAdapter(List<Row> items, StandingsAdapter.AdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public StandingRowViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new StandingRowViewHolder(ViewHolderUtil.getItemView(R.layout.viewholder_standings_row, viewGroup), adapterListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StandingRowViewHolder viewHolder, int position) {
        Row row = items.get(position);
        viewHolder.bind(row);
        viewHolder.bindColumns(row.getColumns());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public interface AdapterListener extends BaseRecyclerViewAdapter.AdapterListener {
        void addScrollNotifier(SyncedScrollView notifier);

        void onCompetitorClicked(Competitor competitor);
    }

}
