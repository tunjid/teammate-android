package com.mainstreetcode.teammate.baseclasses;

import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import androidx.annotation.NonNull;

public abstract class BaseAdapter<VH extends BaseViewHolder, T extends InteractiveAdapter.AdapterListener>
        extends InteractiveAdapter<VH, T> {

    public BaseAdapter(T adapterListener) {
        super(adapterListener);
    }

    @SuppressWarnings("WeakerAccess")
    protected BaseAdapter() { super();}

    @Override public void onViewRecycled(@NonNull VH holder) {
        holder.clear();
        super.onViewRecycled(holder);
    }

    @Override public boolean onFailedToRecycleView(@NonNull VH holder) {
        holder.clear();
        return super.onFailedToRecycleView(holder);
    }
}
