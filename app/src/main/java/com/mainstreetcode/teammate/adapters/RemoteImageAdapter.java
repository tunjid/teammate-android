package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.RemoteImageViewHolder;
import com.mainstreetcode.teammate.model.Event;
import com.mainstreetcode.teammate.model.RemoteImage;
import com.tunjid.androidbootstrap.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for {@link Event}
 */

public class RemoteImageAdapter<T extends RemoteImage>
        extends InteractiveAdapter<RemoteImageViewHolder<T>, RemoteImageAdapter.AdapterListener<T>> {

    private final List<T> items;

    public RemoteImageAdapter(List<T> items, AdapterListener<T> listener) {
        super(listener);
        this.items = items;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RemoteImageViewHolder<T> onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new RemoteImageViewHolder<>(getItemView(R.layout.bottom_nav_item, viewGroup), adapterListener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull RemoteImageViewHolder<T> viewHolder, int position) {
        viewHolder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    public interface AdapterListener<T extends RemoteImage> extends InteractiveAdapter.AdapterListener {
        void onImageClicked(T item);
    }

}
