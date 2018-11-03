package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.StandingRowViewHolder;
import com.mainstreetcode.teammate.model.Competitor;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.Row;
import com.mainstreetcode.teammate.util.SyncedScrollView;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for {@link Event}
 */

public class StandingsAdapter extends InteractiveAdapter<StandingRowViewHolder, StandingsAdapter.AdapterListener> {

    private final List<Row> items;

    public StandingsAdapter(List<Row> items, StandingsAdapter.AdapterListener listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public StandingRowViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new StandingRowViewHolder(getItemView(R.layout.viewholder_standings_row, viewGroup), adapterListener);
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

    public interface AdapterListener extends InteractiveAdapter.AdapterListener {
        void addScrollNotifier(SyncedScrollView notifier);

        void onCompetitorClicked(Competitor competitor);
    }

}
