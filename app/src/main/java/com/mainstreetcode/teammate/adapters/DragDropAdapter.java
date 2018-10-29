package com.mainstreetcode.teammate.adapters;

import androidx.arch.core.util.Function;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.adapters.viewholders.DragDropViewHolder;
import com.mainstreetcode.teammate.model.Event;


/**
 * Adapter for {@link Event}
 */

public class DragDropAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<DragDropViewHolder<VH>> {

    private RecyclerView.Adapter<VH> adapter;
    private Function<VH, View> dragHandleFunction;
    private DragDropViewHolder.DragListener dragListener;

    public DragDropAdapter(RecyclerView.Adapter<VH> adapter,
                           Function<VH, View> dragHandleFunction,
                           DragDropViewHolder.DragListener dragListener) {
        this.adapter = adapter;
        this.dragHandleFunction = dragHandleFunction;
        this.dragListener = dragListener;
    }

    @NonNull
    @Override
    public DragDropViewHolder<VH> onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new DragDropViewHolder<>(adapter.createViewHolder(viewGroup, viewType), dragHandleFunction, dragListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull DragDropViewHolder<VH> viewHolder, int position) {
        adapter.onBindViewHolder(viewHolder.getHolder(), position);
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount();
    }
}
