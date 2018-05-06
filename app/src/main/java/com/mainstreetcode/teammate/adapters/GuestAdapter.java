package com.mainstreetcode.teammate.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammate.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammate.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammate.model.Item;
import com.mainstreetcode.teammate.model.Guest;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import static com.mainstreetcode.teammate.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link Guest}
 */

public class GuestAdapter extends BaseRecyclerViewAdapter<BaseItemViewHolder, ImageWorkerFragment.ImagePickerListener> {

    private final Guest guest;

    public GuestAdapter(Guest guest, ImageWorkerFragment.ImagePickerListener listener) {
        super(listener);
        this.guest = guest;
    }

    @NonNull
    @Override
    public BaseItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.viewholder_simple_input, viewGroup, false);

        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), Item.FALSE, Item.FALSE);
            default:
                return new BaseItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseItemViewHolder viewHolder, int i) {
        viewHolder.bind(guest.asItems().get(i));
    }

    @Override
    public int getItemCount() {
        return guest.asItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        return guest.asItems().get(position).getItemType();
    }

    @Override
    public long getItemId(int position) {
        return guest.asItems().get(position).hashCode();
    }

}
