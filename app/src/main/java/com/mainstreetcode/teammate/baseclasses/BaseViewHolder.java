package com.mainstreetcode.teammate.baseclasses;

import android.view.View;

import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveViewHolder;
import io.reactivex.disposables.CompositeDisposable;

public class BaseViewHolder<T extends InteractiveAdapter.AdapterListener> extends InteractiveViewHolder<T> {

    protected CompositeDisposable disposables = new CompositeDisposable();

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public BaseViewHolder(View itemView, T adapterListener) {
        super(itemView, adapterListener);
    }

    protected void clear() {
        disposables.clear();
        adapterListener = null;
    }

    protected void onDetached() { }

    void updateAdapterListener(T adapterListener) {
        this.adapterListener = adapterListener;
    }
}
