package com.mainstreetcode.teammate.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.input.InputViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.input.TextInputStyle;
import com.mainstreetcode.teammate.baseclasses.BaseAdapter;
import com.mainstreetcode.teammate.baseclasses.BaseViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Guest;
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable;
import com.mainstreetcode.teammate.model.Item;

import java.util.List;

import androidx.annotation.NonNull;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.ITEM;

/**
 * Adapter for {@link Guest}
 */

public class GuestAdapter extends BaseAdapter<InputViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private final List<Differentiable> items;
    private final Chooser chooser;

    public GuestAdapter(List<Differentiable> items, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.items = items;
        this.chooser = new Chooser();
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
    }

    @SuppressWarnings("unchecked")
    @Override protected <S extends AdapterListener> S updateListener(BaseViewHolder<S> viewHolder) {
        return (S) adapterListener;
    }

    @Override
    public void onBindViewHolder(@NonNull InputViewHolder viewHolder, int i) {
        super.onBindViewHolder(viewHolder, i);
        Differentiable item = items.get(i);
        if (item instanceof Item) viewHolder.bind(chooser.get((Item) item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM;
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
