package com.mainstreetcode.teammate.baseclasses;

import android.view.View;

import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;

public class BaseViewHolder<T extends InteractiveAdapter.AdapterListener> extends InteractiveViewHolder<T> {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public BaseViewHolder(View itemView, T adapterListener) {
        super(itemView, adapterListener);
    }

    protected void clear() { adapterListener = null; }

    void updateAdapterListener(T adapterListener) {
        this.adapterListener = adapterListener;
    }
}
