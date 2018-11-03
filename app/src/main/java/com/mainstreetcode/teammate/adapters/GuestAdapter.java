package com.mainstreetcode.teammate.adapters;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

/**
 * Adapter for {@link Guest}
 */

public class GuestAdapter extends InteractiveAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private final List<Identifiable> items;

    public GuestAdapter(List<Identifiable> items, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE, Item.ALL_INPUT_VALID);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int i) {
        Identifiable item = items.get(i);
        if (item instanceof Item) viewHolder.bind((Item) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        Identifiable item = items.get(position);
        return item instanceof Item ? ((Item) item).getItemType() : Item.INPUT;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

}
