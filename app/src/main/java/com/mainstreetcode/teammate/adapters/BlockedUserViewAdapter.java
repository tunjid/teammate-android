package com.mainstreetcode.teammate.adapters;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.model.BlockedUser;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Item;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.util.List;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link BlockedUser}
 */

public class BlockedUserViewAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, BaseRecyclerViewAdapter.AdapterListener> {

    private final List<Identifiable> items;

    public BlockedUserViewAdapter(List<Identifiable> items) {
        super(new AdapterListener() {});
        this.items = items;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
            default:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder baseItemViewHolder, int i) {
        Identifiable item = items.get(i);
        if (item instanceof Item) baseItemViewHolder.bind((Item) item);
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
}
