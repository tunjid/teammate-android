package com.mainstreetcode.teammate.baseclasses;

import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

public abstract class BaseAdapter<VH extends BaseViewHolder, T extends InteractiveAdapter.AdapterListener>
        extends InteractiveAdapter<VH, T> {

    public BaseAdapter(T adapterListener) {
        super(adapterListener);
    }

    @SuppressWarnings("WeakerAccess")
    protected BaseAdapter() { super();}

    protected abstract <S extends InteractiveAdapter.AdapterListener> S updateListener(BaseViewHolder<S> viewHolder);

    @SuppressWarnings("unchecked")
    @Override public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.updateAdapterListener(updateListener(holder));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override public void onViewRecycled(@NonNull VH holder) {
        holder.clear();
        super.onViewRecycled(holder);
    }

    @Override public boolean onFailedToRecycleView(@NonNull VH holder) {
        holder.clear();
        return super.onFailedToRecycleView(holder);
    }
}
