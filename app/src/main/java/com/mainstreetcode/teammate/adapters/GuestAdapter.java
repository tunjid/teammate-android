package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.input.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Guest;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.view.recyclerview.InteractiveAdapter;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Adapter for {@link Guest}
 */

public class GuestAdapter extends InteractiveAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private final List<Identifiable> items;
    private final Chooser chooser;

    public GuestAdapter(List<Identifiable> items, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.items = items;
        this.chooser = new Chooser();
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int i) {
        Identifiable item = items.get(i);
        if (item instanceof Item) viewHolder.bind(chooser.get((Item) item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return Item.INPUT;
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    private static class Chooser extends TextInputStyle.InputChooser {
        @Override public TextInputStyle apply(Item input) {
            return new TextInputStyle(Item.NO_CLICK, Item.NO_CLICK, Item.FALSE, Item.ALL_INPUT_VALID, Item.NO_ICON);
        }
    }
}
